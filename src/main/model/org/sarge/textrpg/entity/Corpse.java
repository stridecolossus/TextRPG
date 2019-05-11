package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.ImmutableContents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;

/**
 * Corpse.
 * @author Sarge
 */
public class Corpse extends WorldObject implements Parent {
	private final Optional<LootFactory> butcher;
	private final Contents contents;

	private boolean butchered;

	/**
	 * Constructor.
	 * @param corpse		Corpse descriptor
	 * @param butcher		Loot-factory when butchering this corpse
	 * @param contents		Corpse contents
	 */
	public Corpse(ObjectDescriptor corpse, Optional<LootFactory> butcher, List<WorldObject> contents) {
		super(corpse);
		this.butcher = notNull(butcher);
		this.contents = new ImmutableContents(true, contents);
	}

	@Override
	public int weight() {
		return super.descriptor().properties().weight() + contents.weight();
	}

	@Override
	public Contents contents() {
		return contents;
	}

	@Override
	protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
		if(butchered) {
			builder.add(KEY_STATE, "corpse.butchered");
		}
		super.describe(carried, builder, formatters);
	}

	/**
	 * Butchers this corpse.
	 * @param actor Actor
	 * @return Loot
	 * @throws ActionException if this corpse cannot be butchered
	 */
	Stream<WorldObject> butcher(Actor actor) throws ActionException {
		final LootFactory factory = butcher.orElseThrow(() -> ActionException.of("corpse.cannot.butcher"));
		if(butchered) throw ActionException.of("corpse.already.butchered");
		butchered = true;
		return factory.generate(actor);
	}

	@Override
	public void destroy() {
		super.destroy();
	}
}
