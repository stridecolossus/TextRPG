package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.Pair;
import org.sarge.lib.util.Util;
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
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.object.Readable;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TableConverter;
import org.sarge.textrpg.util.TextNode;
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
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return descriptor;
			}
		},
		
		RECEPTACLE {
			private final Converter<Integer> LEVEL_CONVERTER = new TableConverter<>(Converter.INTEGER, "infinite", Receptacle.INFINITE);
			
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Liquid liquid = loader.liquidLoader.load(node);
				final int max = node.getAttribute("max", null, LEVEL_CONVERTER);
				final boolean potion = node.getBoolean("potion", false);
				return new Receptacle.Descriptor(descriptor, liquid, max, potion);
			}
		},
		
		LIGHT {
			private final Converter<Light.Type> LIGHT_TYPE = Converter.enumeration(Light.Type.class);
			
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final long lifetime = node.getAttribute("lifetime", null, Converter.DURATION).toMillis();
				final Light.Type type = node.getAttribute("type", Light.Type.GENERAL, LIGHT_TYPE);
				return new Light.Descriptor(descriptor, lifetime, type);
			}
		},
		
		CONTAINER {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return loader.loadContainer(node, descriptor);
			}
		},
		
		KEY {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return new ObjectDescriptor.Builder(descriptor.getName()).category("key").slot(DeploymentSlot.KEYRING).build();
			}
		},
		
		DURABLE {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final int durability = node.getAttribute("durability", null, loader.durabilityConverter);
				return new DurableObject.Descriptor(descriptor, durability);
			}
		},
		
		CONTROL {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Interaction op = node.getAttribute("op", Interaction.PUSH, INTERACTION);
				final Script open = loader.scriptLoader.load(node.child("open"));
				final Script close = node.optionalChild("close").map(TextNode::child).map(loader.scriptLoader::load).orElse(Script.NONE);
				return new Control.ControlDescriptor(descriptor, op, open, close);
			}
		},
		
		FOOD {
			private final Converter<Food.Type> FOOD_TYPE = Converter.enumeration(Food.Type.class);
			
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Food.Type type = node.getAttribute("food-type", Type.COOKED, FOOD_TYPE);
				final int level = node.getInteger("nutrition", null);
				final long lifetime = node.getAttribute("lifetime", null, Converter.DURATION).toMillis();
				return new Food.Descriptor(descriptor, type, level, lifetime);
			}
		},
		
		READABLE {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final String lang = node.getString("lang", null);
				final List<Readable.Chapter> chapters = node.children().map(this::loadChapter).collect(toList());
				return new Readable.Descriptor(descriptor, lang, chapters);
			}
			
			private Readable.Chapter loadChapter(TextNode node) {
				final String title = node.getString("title", null);
				final String text = node.getString("text", null);
				return new Readable.Chapter(title, text);
			}
		},
		
		FIXTURE {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return new Fixture(descriptor);
			}
		},
		
		FURNITURE {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Set<Stance> stances = LoaderHelper.loadEnumeration(node, "stances", LoaderHelper.STANCE);
				final Map<TrackedContents.Limit, String> limits = loadLimits(node);
				if(stances.isEmpty()) throw node.exception("Expected one-or-more stances");
				if(limits.isEmpty()) throw node.exception("Expected one-or-more limits");
				return new Furniture.Descriptor(descriptor, stances, limits);
			}
		},
		
		REVEAL {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Interaction action = node.getAttribute("action", Interaction.EXAMINE, INTERACTION);
				final ObjectDescriptor delegate = loader.load(node.child());
				final boolean replaces = node.getBoolean("replaces", true);
				return new RevealObject.Descriptor(descriptor, Collections.singleton(action), delegate, replaces);
			}
		},
		
		INTERACT {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final Set<Interaction> interactions = LoaderHelper.loadEnumeration(node, "interactions", INTERACTION);
				final int str = node.getInteger("str", 0);
				final boolean removes = node.getBoolean("removes", true);
				if(interactions.isEmpty()) throw node.exception("Expected one-or-more interactions");
				return new InteractObject.Descriptor(descriptor, interactions, str, removes);
			}
		},
		
		WEAPON {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				return loadWeaponDescriptor(node, descriptor, loader.durabilityConverter);
			}
		},
		
		ROPE {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final int durability = node.getAttribute("durability", null, loader.durabilityConverter);
				final int len = node.getInteger("length", null);
				final boolean magical = node.getBoolean("magical", false);
				return new Rope.Descriptor(descriptor, durability, len, magical);
			}
		},
		
		VEHICLE {
			@Override
			protected ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader) {
				final float mod = node.getFloat("mod", null);
				final String type = node.getString("type", "vehicle");
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
		
		protected abstract ObjectDescriptor load(TextNode node, ObjectDescriptor descriptor, ObjectLoader loader);
	}

	/**
	 * Loads a custom object descriptor.
	 * @param xml XML
	 * @return Object descriptor
	 */
	public ObjectDescriptor load(TextNode node) {
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
		catch(Exception e) {
			throw node.exception(e);
		}
	}
	
	/**
	 * Helper - Loads an openable descriptor.
	 * @param def Whether default is openable
	 */
	protected Openable.Lock loadLock(TextNode node, boolean def) {
		if(node.getBoolean("openable", def)) {
			final String key = node.getValue("key");
			if(key == null) {
				return Openable.UNLOCKABLE;
			}
			else
			if(key.equals("fixed")) {
				return Openable.FIXED;
			}
			else {
				final Percentile pick = node.getAttribute("pick", null, difficultyConverter);
				return new Openable.Lock(key, pick);
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
	public static Weapon loadWeaponDescriptor(TextNode node, ObjectDescriptor descriptor, Converter<Integer> durabilityConverter) {
		final int durability;
		if(durabilityConverter == null) {
			durability = Integer.MAX_VALUE;
		}
		else {
			durability = node.getAttribute("durability", null, durabilityConverter);
		}
		final int speed = node.getInteger("speed", null);
		final DamageEffect damage = loadDamage(node.child("damage"));
		final Effect.Descriptor effect = node.optionalChild("effect").map(EFFECT_LOADER::load).orElse(null);
		final String ammo = node.getValue("ammo");
		return new Weapon(descriptor, durability, speed, damage, effect, ammo);
	}

	/**
	 * Loads a damage effect.
	 */
	private static DamageEffect loadDamage(TextNode node) {
		final DamageType type = node.getAttribute("damage", DamageType.PIERCING, DAMAGE_CONVERTER);
		final Value amount = VALUE_LOADER.load(node, "amount");
		final boolean wound = node.getBoolean("wound", false);
		return new DamageEffect(type, amount, wound);
	}

	/**
	 * Loads a container.
	 */
	public Container.Descriptor loadContainer(TextNode node, ObjectDescriptor descriptor) {
		final Openable.Lock lock = loadLock(node, false);
		final Container.Placement placement = node.getAttribute("place", Container.Placement.IN, PLACEMENT);
		final DeploymentSlot slot = loadSlot(node);
		return new Container.Descriptor(descriptor, placement, lock, loadLimits(node), slot);
	}

	/**
	 * Loads the optional deployment slot.
	 */
	private static DeploymentSlot loadSlot(TextNode node) {
		final String slot = node.getValue("contents-slot");
		if(slot == null) {
			return null;
		}
		else {
			return SLOT_CONVERTER.convert(slot);
		}
	}

	/**
	 * Loads tracked contents limits.
	 * TODO - move to loader-helper
	 */
	private static Map<TrackedContents.Limit, String> loadLimits(TextNode node) {
		final Function<TextNode, Pair<TrackedContents.Limit, String>> mapper = child -> {
			final TrackedContents.Limit limit = loadLimit(child);
			final String reason = child.getString("reason", null);
			return new Pair<>(limit, reason);
		};
		return node.children("limit").map(mapper).collect(Pair.toMap());
	}

	/**
	 * Loads a container limit.
	 */
	private static TrackedContents.Limit loadLimit(TextNode node) {
		final String type = node.getString("type", null);
		switch(type) {
		case "size":
			return loadIntegerLimit(node, TrackedContents.Limit::number);
			
		case "weight":
			return loadIntegerLimit(node, TrackedContents.Limit::number);
			
		case "bulk":
			return loadIntegerLimit(node, TrackedContents.Limit::number);
			
		case "category":
			final String name = node.getValue("category");
			final Set<String> cats;
			if(name == null) {
				cats = node.children().map(TextNode::name).collect(toSet());
				if(cats.isEmpty()) throw node.exception("Empty categories");
			}
			else {
				cats = Collections.singleton(name);
			}
			return Container.categoryLimit(cats);
			
		default:
			throw node.exception("Unknown limit: " + type);
		}
	}
	
	/**
	 * Loads an integer-based limit.
	 */
	private static TrackedContents.Limit loadIntegerLimit(TextNode node, Function<Integer, TrackedContents.Limit> mapper) {
		final int max = node.getInteger("max", null);
		return mapper.apply(max);
	}
}
