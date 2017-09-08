package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.EventHolder;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Message;

/**
 * Object that can be eaten.
 * @author Sarge
 */
public class Food extends WorldObject {
	/**
	 * Queue for decay events.
	 */
	public static final EventQueue QUEUE = new EventQueue();

	/**
	 * Type of food.
	 */
	public enum Type {
		/**
		 * Cooked food.
		 */
		COOKED,

		/**
		 * Food that <b>cannot</b> be eaten but can be cooked, e.g. raw meat.
		 */
		RAW,

		/**
		 * Food that can be eaten but <b>not</b> cooked, e.g. a carrot.
		 */
		FOOD
	}

	/**
	 * Food descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Type type;
		private final int level;
		private final long lifetime;

		/**
		 * Constructor.
		 * @param descriptor		Descriptor
		 * @param type				Food type
		 * @param level				Nutrition level
		 * @param lifetime			Lifetime (ms)
		 */
		public Descriptor(ObjectDescriptor descriptor, Type type, int level, long lifetime) {
			super(descriptor);
			Check.notNull(type);
			Check.zeroOrMore(level);
			Check.oneOrMore(lifetime);
			this.type = type;
			this.level = level;
			this.lifetime = lifetime;
		}

		/**
		 * @return Nutrition level
		 */
		public int getLevel() {
			return level;
		}

		@Override
		public WorldObject create() {
			return new Food(this);
		}
	}

	private final EventHolder holder = new EventHolder();

	private Type type;

	/**
	 * Constructor.
	 * @param descriptor Food descriptor
	 */
	public Food(Descriptor descriptor) {
		super(descriptor);
		this.type = descriptor.type;
		register(descriptor);
	}

	@Override
	public String name() {
		if(type == Type.RAW) {
			return "raw." + super.name();
		}
		else {
			return super.name();
		}
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor;
	}

	/**
	 * @return Whether this food can be eaten
	 */
	public boolean isEdible() {
		switch(type) {
		case COOKED:
		case FOOD:
			return true;

		default:
			return false;
		}
	}

	/**
	 * @throws ActionException if this food has already been cooked or cannot be cooked
	 */
	public void verifyCook() throws ActionException {
		switch(type) {
		case COOKED:	throw new ActionException("cook.already.cooked");
		case FOOD:		throw new ActionException("cook.cannot.cook");
		}
	}

	/**
	 * Cooks this food.
	 */
	protected void cook() {
		// Cook
		type = Type.COOKED;

		// Re-register decay event
		holder.cancel();
		register(descriptor());
		// TODO - longer lifetime for cooked?
	}

	/**
	 * Consumes this food.
	 * @throws ActionException if this food is {@link Type#RAW}
	 */
	public void consume() throws ActionException {
		if(type == Type.RAW) throw new ActionException("consume.not.cooked");
		holder.cancel();
		destroy();
	}

	/**
	 * Registers a decay event for this food.
	 */
	private void register(Descriptor descriptor) {
		QUEUE.add(this::decay, descriptor.lifetime);
	}

	/**
	 * Decays this food.
	 */
	private void decay() {
		// Notify owner
		final Actor owner = (Actor) owner();
		if(owner != null) {
			owner.alert(new Message("food.decayed", this));
		}

		// Decay
		destroy();
	}
}
