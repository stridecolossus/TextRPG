package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.sarge.lib.collection.Pair;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.Util;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.LoaderException;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.entity.DamageEffect;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.*;
import org.sarge.textrpg.object.Food.Type;
import org.sarge.textrpg.object.ObjectDescriptor.Equipment;
import org.sarge.textrpg.object.ObjectDescriptor.EquipmentBuilder;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.object.Readable;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TableConverter;
import org.sarge.textrpg.world.Route;

/**
 * Loader for objects.
 * @author Sarge
 */
public class ObjectLoader {
	private static final Converter<DamageType> DAMAGE_CONVERTER = Converter.enumeration(DamageType.class);
	private static final Converter<DeploymentSlot> SLOT_CONVERTER = Converter.enumeration(DeploymentSlot.class);
	private static final Converter<Container.Placement> PLACEMENT = Converter.enumeration(Container.Placement.class);
	private static final Converter<Interaction> INTERACTION = Converter.enumeration(Interaction.class);

	private static final ValueLoader VALUE_LOADER = new ValueLoader();
	private static final EffectLoader EFFECT_LOADER = new EffectLoader();

	private Converter<Percentile> difficultyConverter = Percentile.CONVERTER;
	private Converter<Integer> durabilityConverter = Converter.INTEGER;

	private final ObjectDescriptorLoader descriptorLoader;
	private final ScriptLoader scriptLoader;
	private final LiquidLoader liquidLoader;

	/**
	 * Constructor.
	 * @param descriptorLoader		Descriptor loader
	 * @param scriptLoader			Script loader
	 * @param liquidLoader			Liquid loader
	 */
	public ObjectLoader(ObjectDescriptorLoader descriptorLoader, ScriptLoader scriptLoader, LiquidLoader liquidLoader) {
		Check.notNull(descriptorLoader);
		Check.notNull(scriptLoader);
		Check.notNull(liquidLoader);
		this.descriptorLoader = descriptorLoader;
		this.scriptLoader = scriptLoader;
		this.liquidLoader = liquidLoader;
	}

	public void setDifficultyConverter(Converter<Percentile> difficultyConverter) {
		Check.notNull(difficultyConverter);
		this.difficultyConverter = difficultyConverter;
	}

	public void setDurabilityConverter(Converter<Integer> durabilityConverter) {
		Check.notNull(difficultyConverter);
		this.durabilityConverter = durabilityConverter;
	}

	public ScriptLoader getScriptLoader() {
		return scriptLoader;
	}

	/**
	 * Loaders for object sub-classes.
	 */
	public static enum Loader {
		OBJECT {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return descriptor;
			}
		},

		RECEPTACLE {
			private final Converter<Integer> LEVEL_CONVERTER = new TableConverter<>(Converter.INTEGER, "infinite", Receptacle.INFINITE);

			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Liquid liquid = loader.liquidLoader.load(node);
				final int max = node.attributes().toValue("max", null, LEVEL_CONVERTER);
				final boolean potion = node.attributes().toBoolean("potion", false);
				return new Receptacle.Descriptor(descriptor, liquid, max, potion);
			}
		},

		LIGHT {
			private final Converter<Light.Type> LIGHT_TYPE = Converter.enumeration(Light.Type.class);

			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final long lifetime = node.attributes().toValue("lifetime", null, Converter.DURATION).toMillis();
				final Light.Type type = node.attributes().toValue("type", Light.Type.GENERAL, LIGHT_TYPE);
				return new Light.Descriptor(descriptor, lifetime, type);
			}
		},

		CONTAINER {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return loader.loadContainer(node, descriptor);
			}
		},

		KEY {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Equipment key = new EquipmentBuilder().slot(DeploymentSlot.KEYRING).build();
				return new ObjectDescriptor.Builder(descriptor.getName()).category("key").equipment(key).build();
			}
		},

		DURABLE {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final int durability = node.attributes().toValue("durability", null, loader.durabilityConverter);
				return new DurableObject.Descriptor(descriptor, durability);
			}
		},

		CONTROL {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Interaction op = node.attributes().toValue("op", Interaction.PUSH, INTERACTION);
				final Script open = loader.scriptLoader.load(node.child("open"));
				final Script close = node.optionalChild("close").map(Element::child).map(loader.scriptLoader::load).orElse(Script.NONE);
				return new Control.ControlDescriptor(descriptor, op, open, close);
			}
		},

		FOOD {
			private final Converter<Food.Type> FOOD_TYPE = Converter.enumeration(Food.Type.class);

			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Food.Type type = node.attributes().toValue("food-type", Type.COOKED, FOOD_TYPE);
				final int level = node.attributes().toInteger("nutrition", null);
				final long lifetime = node.attributes().toValue("lifetime", null, Converter.DURATION).toMillis();
				return new Food.Descriptor(descriptor, type, level, lifetime);
			}
		},

		READABLE {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final String lang = node.attributes().toString("lang", null);
				final List<Readable.Chapter> chapters = node.children().map(this::loadChapter).collect(toList());
				return new Readable.Descriptor(descriptor, lang, chapters);
			}

			private Readable.Chapter loadChapter(Element node) {
				final String title = node.attributes().toString("title", null);
				final String text = node.attributes().toString("text", null);
				return new Readable.Chapter(title, text);
			}
		},

		FIXTURE {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return new Fixture(descriptor);
			}
		},

		FURNITURE {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Set<Stance> stances = LoaderHelper.loadEnumeration(node, "stances", LoaderHelper.STANCE);
				final Map<TrackedContents.Limit, String> limits = loadLimits(node);
				if(stances.isEmpty()) throw node.exception("Expected one-or-more stances");
				if(limits.isEmpty()) throw node.exception("Expected one-or-more limits");
				return new Furniture.Descriptor(descriptor, stances, limits);
			}
		},

		REVEAL {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Interaction action = node.attributes().toValue("action", Interaction.EXAMINE, INTERACTION);
				final ObjectDescriptor delegate = loader.load(node.child());
				final boolean replaces = node.attributes().toBoolean("replaces", true);
				return new RevealObject.Descriptor(descriptor, Collections.singleton(action), delegate, replaces);
			}
		},

		INTERACT {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Set<Interaction> interactions = LoaderHelper.loadEnumeration(node, "interactions", INTERACTION);
				final int str = node.attributes().toInteger("str", 0);
				final boolean removes = node.attributes().toBoolean("removes", true);
				if(interactions.isEmpty()) throw node.exception("Expected one-or-more interactions");
				return new InteractObject.Descriptor(descriptor, interactions, str, removes);
			}
		},

		WEAPON {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return loadWeaponDescriptor(node, descriptor, loader.durabilityConverter);
			}
		},

		ROPE {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final int durability = node.attributes().toValue("durability", null, loader.durabilityConverter);
				final int len = node.attributes().toInteger("length", null);
				final boolean magical = node.attributes().toBoolean("magical", false);
				return new Rope.Descriptor(descriptor, durability, len, magical);
			}
		},

		VEHICLE {
			@Override
			protected ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final float mod = node.attributes().toFloat("mod", null);
				final String type = node.attributes().toString("type", "vehicle");
				switch(type) {
				case "vehicle":
					final Set<Route> routes = LoaderHelper.loadEnumeration(node, "route", LoaderHelper.ROUTE);
					return new Vehicle.Descriptor(descriptor, loadLimits(node), routes, mod);

				case "boat":
				case "raft":
					return new Boat.Descriptor(descriptor, loadLimits(node), mod, type.equals("raft"));

				default:
					throw node.exception("Invalid vehicle type: " + type);
				}
			}
		};

		protected abstract ObjectDescriptor load(Element node, ObjectDescriptor descriptor, ObjectLoader loader);
	}

	/**
	 * Loads a custom object descriptor.
	 * @param xml XML
	 * @return Object descriptor
	 */
	public ObjectDescriptor load(Element node) {
		// Special case for money
		if(node.name().equals("money")) {
			return Money.DESCRIPTOR;
		}

		// Load underlying descriptor
		final ObjectDescriptor descriptor = descriptorLoader.load(node);

		// Delegate to sub-class loader
		final Loader loader = Util.getEnumConstant(node.name(), Loader.class, () -> node.exception("Unknown object class: " + node.name()));
		try {
			return loader.load(node, descriptor, this);
		}
		catch(LoaderException e) {
			throw e;
		}
		catch(final Exception e) {
			throw node.exception(e);
		}
	}

	/**
	 * Helper - Loads an openable descriptor.
	 * @param def Whether default is openable
	 */
	protected Openable.Lock loadLock(Element node, boolean def) {
		if(node.attributes().toBoolean("openable", def)) {
			final Optional<String> key = node.attributes().getOptional("key", Converter.STRING);
			if(key.isPresent()) {
				final String name = key.get();
				if(key.equals("fixed")) {
					return Openable.FIXED;
				}
				else {
					final Percentile pick = node.attributes().toValue("pick", null, difficultyConverter);
					return new Openable.Lock(name, pick);
				}
			}
			else {
				return Openable.UNLOCKABLE;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Helper - Loads a weapon descriptor.
	 * @param xml						XML
	 * @param descriptor				Object descriptor
	 * @param durabilityConverter		Converter for durability or <tt>null</tt> to ignore
	 * @return Weapon descriptor
	 */
	public static Weapon loadWeaponDescriptor(Element node, ObjectDescriptor descriptor, Converter<Integer> durabilityConverter) {
		final int durability;
		if(durabilityConverter == null) {
			durability = Integer.MAX_VALUE;
		}
		else {
			durability = node.attributes().toValue("durability", null, durabilityConverter);
		}
		final int speed = node.attributes().toInteger("speed", null);
		final DamageEffect damage = loadDamage(node.child("damage"));
		final Effect.Descriptor effect = node.optionalChild("effect").map(EFFECT_LOADER::load).orElse(null);
		final String ammo = node.attributes().getOptional("ammo", Converter.STRING).orElse(null); // TODO - nasty
		return new Weapon(descriptor, durability, speed, damage, effect, ammo);
	}

	/**
	 * Loads a damage effect.
	 */
	private static DamageEffect loadDamage(Element node) {
		final DamageType type = node.attributes().toValue("damage", DamageType.PIERCING, DAMAGE_CONVERTER);
		final Value amount = VALUE_LOADER.load(node, "amount");
		final boolean wound = node.attributes().toBoolean("wound", false);
		return new DamageEffect(type, amount, wound);
	}

	/**
	 * Loads a container.
	 */
	public Container.Descriptor loadContainer(Element node, ObjectDescriptor descriptor) {
		final Openable.Lock lock = loadLock(node, false);
		final Container.Placement placement = node.attributes().toValue("place", Container.Placement.IN, PLACEMENT);
		final DeploymentSlot slot = loadSlot(node);
		return new Container.Descriptor(descriptor, placement, lock, loadLimits(node), slot);
	}

	/**
	 * Loads the optional deployment slot.
	 */
	private static DeploymentSlot loadSlot(Element node) {
		return node.attributes().getOptional("contents-slot", SLOT_CONVERTER).orElse(null);
	}

	/**
	 * Loads tracked contents limits.
	 * TODO - move to loader-helper
	 */
	private static Map<TrackedContents.Limit, String> loadLimits(Element node) {
		final Function<Element, Pair<TrackedContents.Limit, String>> mapper = child -> {
			final TrackedContents.Limit limit = loadLimit(child);
			final String reason = child.attributes().toString("reason");
			return new Pair<>(limit, reason);
		};
		return node.children("limit").map(mapper).collect(Pair.toMap());
	}

	/**
	 * Loads a container limit.
	 */
	private static TrackedContents.Limit loadLimit(Element node) {
		final String type = node.attributes().toString("type", null);
		switch(type) {
		case "size":
			return loadIntegerLimit(node, TrackedContents.Limit::number);

		case "weight":
			return loadIntegerLimit(node, TrackedContents.Limit::number);

		case "bulk":
			return loadIntegerLimit(node, TrackedContents.Limit::number);

		case "category":
			final Set<String> cats;
			final Optional<String> name = node.attributes().getOptional("category", Converter.STRING);
			if(name.isPresent()) {
				cats = Collections.singleton(name.get());
			}
			else {
				cats = node.children().map(Element::name).collect(toSet());
				if(cats.isEmpty()) throw node.exception("Empty categories");
			}
			return Container.categoryLimit(cats);

		default:
			throw node.exception("Unknown limit: " + type);
		}
	}

	/**
	 * Loads an integer-based limit.
	 */
	private static TrackedContents.Limit loadIntegerLimit(Element node, Function<Integer, TrackedContents.Limit> mapper) {
		final int max = node.attributes().toInteger("max", null);
		return mapper.apply(max);
	}
}
