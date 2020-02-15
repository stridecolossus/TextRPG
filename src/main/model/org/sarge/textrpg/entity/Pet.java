package org.sarge.textrpg.entity;

import org.sarge.textrpg.object.Letter;
import org.sarge.textrpg.util.ActionException;

/**
 * A <i>pet</i> is an entity that can deliver letters.
 * @author Sarge
 */
public class Pet extends Entity {
	private final FollowModel follower = new FollowModel();

	private Letter letter;

	/**
	 * Constructor.
	 * @param race		Pet race
	 * @param owner		Owner
	 */
	public Pet(Race race, Entity owner) {
		super(new DefaultEntityDescriptor(race, null), null); // TODO

		try {
			follower.follow(owner);
		}
		catch(ActionException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	public void set(Letter letter) throws ActionException {
		if(this.letter != null) throw ActionException.of("pet.already.carrying");
		// TODO - letter.destroy();
		this.letter = letter;
	}
	*/

	public void remove() throws ActionException {
	}

	public void deliver(Entity entity) throws ActionException {

	}

	@Override
	protected void destroy() {
		follower.clear();
		super.destroy();
	}
}
