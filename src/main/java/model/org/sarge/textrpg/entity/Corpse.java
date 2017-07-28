package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.Collection;

import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Description.Builder;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.TrackedContents;
import org.sarge.textrpg.object.WorldObject;

/**
 * Corpse.
 * @author Sarge
 */
public class Corpse extends WorldObject implements Parent {
	/**
	 * Immutable contents.
	 */
	private final TrackedContents contents = new TrackedContents() {
		@Override
		public String getReason(Thing obj) {
			return "corpse.contents.immutable";
		}
	};

	private final Race race;

	private boolean butchered;

	/**
	 * Constructor.
	 * @param race			Race of this corpse
	 * @param contents		Contents of this corpse
	 */
	public Corpse(ObjectDescriptor descriptor, Race race, Collection<Thing> contents) {
		super(descriptor);
		this.race = race;
		contents.stream().forEach(this.contents::add);
	}

	@Override
	public int getWeight() {
		return super.getWeight() + contents.getWeight();
	}

	@Override
	public Contents getContents() {
		return contents;
	}

	@Override
	protected void describe(Builder builder) {
		if(butchered) {
			builder.add("butchered", "{butchered}");
		}
	}

	/**
	 * @return Whether this corpse has been butchered
	 */
	public boolean isButchered() {
		return butchered;
	}

	/**
	 * Butchers this corpse.
	 * @param actor Actor
	 * @throws ActionException if this corpse cannot be butchered
	 * @throws IllegalStateException if this corpse has already been butchered
	 */
	protected void butcher(Entity actor) throws ActionException {
		// Check can be butchered
		if(butchered) throw new IllegalStateException("Already butchered");
		final LootFactory factory = race.getKillDescriptor().getButcherFactory().orElseThrow(() -> new ActionException("corpse.cannot.butcher"));

		// Butcher and add loot
		final Collection<WorldObject> loot = factory.generate(actor).collect(toList());
		// TODO - shouldn't this be setParent?
		loot.stream().forEach(this.contents::add);
		butchered = true;
	}
}
