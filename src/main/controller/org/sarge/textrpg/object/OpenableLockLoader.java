package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.EffectLoader;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Trap;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Loader for the lock descriptor.
 * @author Sarge
 * @see Openable.Lock
 */
@Component
public class OpenableLockLoader {
	private final Map<String, ObjectDescriptor> keys = new StrictMap<>();

	private final EffectLoader loader;

	private Converter<Percentile> converter = Percentile.CONVERTER;

	/**
	 * Constructor.
	 * @param loader Loader for trap effects
	 */
	public OpenableLockLoader(EffectLoader loader) {
		this.loader = notNull(loader);
	}

	/**
	 * Sets the trap/pick difficulty converter.
	 * @param converter Difficulty converter
	 */
	@Autowired(required=false)
	public void setConverter(@Qualifier("converter.difficulty") Converter<Percentile> converter) {
		this.converter = notNull(converter);
	}

	/**
	 * Looks up or loads a key descriptor.
	 * @param name Key name
	 * @return Key
	 */
	public ObjectDescriptor key(String name) {
		return keys.computeIfAbsent(name, ignored -> new ObjectDescriptor.Builder(name).slot(Slot.KEYRING).build());
	}

	/**
	 * Loads a lock descriptor.
	 * @param xml XML
	 * @return Lock
	 */
	public Openable.Lock load(Element xml) {
		final String type = xml.attribute("type").toText();
		switch(type) {
		case "default":
			return Openable.Lock.DEFAULT;

		case "latch":
			return Openable.Lock.LATCH;

		case "lock":
			final ObjectDescriptor key = key(xml.attribute("key").toText());
			final Trap trap = xml.find("trap").map(this::loadTrap).orElse(null);
			final Percentile pick = xml.attribute("pick").toValue(converter);
			return new Openable.Lock(key, pick, trap);

		default:
			throw xml.exception("Invalid lock type: " + type);
		}
	}

	/**
	 * Loads a trap.
	 * @param xml XML
	 * @return Trap descriptor
	 */
	private Trap loadTrap(Element xml) {
		final Effect effect = loader.load(xml.child());
		final Percentile diff = xml.attribute("diff").toValue(converter);
		return new Trap(effect, diff);
	}
}
