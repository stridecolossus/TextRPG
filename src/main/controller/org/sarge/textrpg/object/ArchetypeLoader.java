package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.Element.Attribute;
import org.sarge.lib.xml.Element.ElementException;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.EffectLoader;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.View;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.contents.LimitsMapLoader;
import org.sarge.textrpg.util.ConverterAdapter;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.util.RegistryLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Loader for archetype descriptors.
 * @author Sarge
 */
@Service
public class ArchetypeLoader {
	// Type converters
	private static final Converter<Light.Type> LIGHT_TYPE = Converter.enumeration(Light.Type.class);
	private static final Converter<Ammo.Type> AMMO = Converter.enumeration(Ammo.Type.class);
	private static final Converter<Integer> RECEPTACLE_LEVEL = new ConverterAdapter<>(Map.of("infinite", Receptacle.INFINITE), Converter.INTEGER);
	private static final Converter<Loader> LOADER = Converter.enumeration(Loader.class);

	private final LimitsMapLoader limitsLoader = new LimitsMapLoader();

	// Loaders
	private ObjectDescriptorLoader descriptorLoader;
	private OpenableLockLoader lockLoader;
	private RegistryLoader<Liquid> liquidLoader;
	private EffectLoader effectLoader;

	// Data registers
	private Registry<Skill> skills;

	// Configuration
	private Converter<Integer> durabilityConverter = Converter.INTEGER;
	private Converter<Percentile> intensityConverter = Percentile.CONVERTER;

	@Autowired
	public void setDescriptorLoader(@Qualifier("base.loader") ObjectDescriptorLoader descriptorLoader) {
		this.descriptorLoader = descriptorLoader;
	}

	@Autowired
	public void setLockLoader(OpenableLockLoader lockLoader) {
		this.lockLoader = notNull(lockLoader);
	}

	@Autowired
	public void setLiquidLoader(RegistryLoader<Liquid> liquidLoader) {
		this.liquidLoader = notNull(liquidLoader);
	}

	@Autowired
	public void setEffectLoader(EffectLoader effectLoader) {
		this.effectLoader = notNull(effectLoader);
	}

	@Autowired
	public void setSkillsRegistry(Registry<Skill> skills) {
		this.skills = notNull(skills);
	}

	@Autowired(required=false)
	public void setDurabilityConverter(@Qualifier("converter.durability") Converter<Integer> durabilityConverter) {
		this.durabilityConverter = notNull(durabilityConverter);
	}

	@Autowired(required=false)
	public void setIntensityConverter(@Qualifier("converter.intensity") Converter<Percentile> intensityConverter) {
		this.intensityConverter = notNull(intensityConverter);
	}

	/**
	 * Loads an archetype.
	 * @param xml			XML
	 * @param policy		Fixture policy
	 * @return Archetype descriptor
	 */
	public ObjectDescriptor load(Element xml, ObjectDescriptorLoader.Policy policy) {
		try {
			final ObjectDescriptor descriptor = descriptorLoader.load(xml, policy);
			return LOADER.apply(xml.name()).load(xml, descriptor, this);
		}
		catch(ElementException e) {
			throw e;
		}
		catch(Exception e) {
			throw xml.exception(e);
		}
	}

	/**
	 * Sub-class loaders.
	 */
	private enum Loader {
		OBJECT {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				return descriptor;
			}
		},

		RECEPTACLE {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final Liquid liquid = loader.liquidLoader.load(xml);
				final int level = xml.attribute("level").toValue(RECEPTACLE_LEVEL);
				return new Receptacle.Descriptor(descriptor, liquid, level);
			}
		},

		LIGHT {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final Light.Type type = xml.attribute("type").toValue(Light.Type.DEFAULT, LIGHT_TYPE);
				final Duration lifetime = xml.attribute("lifetime").toValue(DurationConverter.CONVERTER);
				final Percentile light = xml.attribute("light").toValue(loader.intensityConverter);
				final Percentile smoke = xml.attribute("smoke").toValue(Percentile.ZERO, loader.intensityConverter);
				return new Light.Descriptor(descriptor, type, lifetime, light, smoke);
			}
		},

		PORTAL {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final var lock = xml.find("lock").map(loader.lockLoader::load).orElse(Openable.Lock.DEFAULT);
				return new Portal.Descriptor(descriptor, lock);
			}
		},

		LAMP {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				if(!descriptor.isFixture()) throw xml.exception("Lamps must be fixtures");
				return descriptor;
			}
		},

		READABLE {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final Skill lang = xml.attribute("lang").toValue(loader.skills::get);
				return new Readable.Descriptor(descriptor, lang);
			}
		},

		BOOK {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final Skill lang = xml.attribute("lang").toValue(loader.skills::get);
				final Function<Element, Readable.Section> mapper = e -> new Readable.Section(e.attribute("title").toText(), e.attribute("text").toText(), false);
				final var chapters = xml.children().map(mapper).collect(toList());
				return new Readable.Descriptor(descriptor, true, lang, chapters);
			}
		},

		DURABLE {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				return loader.loadDurableDescriptor(xml, descriptor);
			}
		},

		ROPE {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final int length = xml.attribute("length").toInteger();
				final boolean magical = xml.attribute("magical").toBoolean(false);
				return new Rope.Descriptor(loader.loadDurableDescriptor(xml, descriptor), length, magical);
			}
		},

		CONTAINER {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final String prep = xml.attribute("prep").toText("in");
				final LimitsMap limits = xml.find("limits").map(loader.limitsLoader::load).orElse(LimitsMap.EMPTY);
				final var lock = xml.find("lock").map(loader.lockLoader::load);
				if(lock.isPresent()) {
					return new OpenableContainer.Descriptor(descriptor, prep, limits, lock.get());
				}
				else {
					return new Container.Descriptor(descriptor, prep, limits);
				}
			}
		},

		UTENSIL {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final int capacity = xml.attribute("capacity").toInteger();
				return new Utensil.Descriptor(descriptor, capacity);
			}
		},

		FOOD {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final int nutrition = xml.attribute("nutrition").toInteger();
				final boolean meat = xml.attribute("meat").toBoolean(false);
				final Attribute cook = xml.attribute("cook");
				if(cook.isPresent()) {
					return new Food.Descriptor(descriptor, meat, nutrition, cook.toInteger());
				}
				else {
					return new Food.Descriptor(descriptor, nutrition);
				}
			}
		},

		WEAPON {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final DurableObject.Descriptor durable = loader.loadDurableDescriptor(xml, descriptor);
				final Skill skill = xml.attribute("skill").optional().map(loader.skills::get).orElse(Skill.NONE);
				final int speed = xml.attribute("speed").toInteger();
				final Damage damage = loader.effectLoader.loadDamage(xml.child("damage"));
				final Ammo.Type ammo = xml.attribute("ammo").optional(AMMO).orElse(null);
				final String enemy = xml.attribute("enemy").optional().orElse(null);
				return new Weapon.Descriptor(durable, skill, speed, damage, ammo, enemy);
			}
		},

		SHEATH {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final String cat = xml.attribute("sheath-category").toText();
				return new Sheath.Descriptor(descriptor, cat);
			}
		},

		VEHICLE {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				// TODO
				return null;
			}
		},

		FERRY {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				// TODO
				return null;
			}
		},

		FURNITURE {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				// TODO
				return null;
			}
		},

		WINDOW {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final String drape = xml.attribute("drape").toText("curtains");
				final View view = View.load(xml);
				return new Window.Descriptor(descriptor, drape, view);
			}
		},

		NODE {
			@Override
			protected ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader) {
				final String res = xml.attribute("res").toText();
				return new Node.Descriptor(descriptor, res);
			}
		};

		/**
		 * Loads an archetype sub-class.
		 * @param xml				XML
		 * @param descriptor		Underlying object descriptor
		 * @param loader			Loader context
		 * @return Archetype descriptor
		 */
		protected abstract ObjectDescriptor load(Element xml, ObjectDescriptor descriptor, ArchetypeLoader loader);
	}

	/**
	 * Helper - Loads a descriptor for a durable object.
	 * @param xml				XML
	 * @param descriptor		Object descriptor
	 * @return Durable descriptor
	 */
	private DurableObject.Descriptor loadDurableDescriptor(Element xml, ObjectDescriptor descriptor) {
		final int max = xml.attribute("durability").toValue(durabilityConverter);
		return new DurableObject.Descriptor(descriptor, max);
	}
}
