package org.sarge.textrpg.entity;

import org.sarge.textrpg.object.Letter;
import org.sarge.textrpg.util.ActionException;

/**
 * Pet that can deliver letters.
 * @author Sarge
 */
public class Pet extends Entity implements Follower {
	private final FollowerModel follower = new FollowerModel();

	private Letter letter;

	/**
	 * Constructor.
	 * @param race		Pet race
	 * @param owner		Owner
	 */
	public Pet(Race race, Leader owner) {
		super(new DefaultEntityDescriptor(race, null), null); // TODO
		Follower.follow(this, owner);
	}

	@Override
	public FollowerModel follower() {
		return follower;
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
		Follower.clear(this);
		super.destroy();
	}
}
