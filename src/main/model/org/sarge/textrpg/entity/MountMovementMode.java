package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Trail;

/**
 * Movement mode for a mounted entity.
 * @author Sarge
 * TODO - dismount chance
 */
public class MountMovementMode extends AbstractEqualsObject implements MovementMode {
	private final Entity actor;
	private final Mount mount;

	/**
	 * Constructor.
	 * @param actor Actor
	 * @param mount Mount
	 */
	public MountMovementMode(Entity actor, Mount mount) {
		this.actor = notNull(actor);
		this.mount = notNull(mount);
	}

	@Override
	public Mount mover() {
		return mount;
	}

	// TODO - scale, also check if tx too small
	@Override
	public List<Transaction> transactions(int cost) {
		return List.of(
			MovementMode.transaction(mount, cost, "mount.insufficient.stamina"),
			MovementMode.transaction(actor, (int) (cost * mount.modifier()), "mounted.insufficient.stamina")
		);
	}

	@Override
	public void move(Exit exit) throws ActionException {
		// Check mount can traverse link
		final Location dest = exit.destination();
		if(exit.link().isEntityOnly()) throw ActionException.of("mount.entity.only");
		if(dest.isWater()) throw ActionException.of("mount.invalid.terrain");

		// Move
		actor.parent(dest);
		mount.parent(dest);
		// TODO - followers, current mount should be excluded
	}

	@Override
	public Percentile noise() {
		return mount.noise().max(actor.emission(Emission.SOUND));
	}

	@Override
	public Trail trail() {
		return mount.movement().trail();
	}

	@Override
	public Percentile tracks() {
		return mount.descriptor().race().gear().tracks();
	}
}
