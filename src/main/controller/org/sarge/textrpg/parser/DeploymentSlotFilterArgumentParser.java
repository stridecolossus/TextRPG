package org.sarge.textrpg.parser;

import java.util.EnumSet;
import java.util.Set;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Filter;
import org.sarge.textrpg.util.WordCursor;
import org.sarge.textrpg.object.Slot;
import org.springframework.stereotype.Component;

/**
 * Argument parser for an {@link ObjectDescriptor.Filter} filtering on {@link Slot}.
 * @author Sarge
 */
@Component
public class DeploymentSlotFilterArgumentParser implements ArgumentParser<ObjectDescriptor.Filter> {
	private final ArgumentParser<Slot> slot;

	/**
	 * Constructor.
	 */
	public DeploymentSlotFilterArgumentParser() {
		slot = new EnumArgumentParser<>("slot", slots());
	}

	/**
	 * @return Deployment slots
	 */
	private static Set<Slot> slots() {
		final var none = EnumSet.of(Slot.NONE);
		return EnumSet.complementOf(none);
	}

	// TODO
	@Override
	public Filter parse(WordCursor cursor) {
		// Parse literal
		final var literal = new StringArgumentParser("slot.worn");
		if(literal.parse(cursor) == null) return null;

		// Parse deployment slot
		final Slot slot = this.slot.parse(cursor);
		if(slot == null) return null;

		// Create filter
		return ObjectDescriptor.Filter.slot(slot);
	}

	@Override
	public int count() {
		return 2;
	}
}
