package org.sarge.textrpg.common;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.sarge.lib.util.StreamUtil;

/**
 * Base-class for an object or entity in the world.
 * @author Sarge
 */
public abstract class Thing implements Hidden {
	/**
	 * Helper.
	 */
	public static final Predicate<Thing> NOT_QUIET = StreamUtil.not(Thing::isQuiet);

	/**
	 * Limbo parent.
	 */
	public static final Parent LIMBO = new Parent() {
		@Override
		public Contents contents() {
			return Contents.EMPTY;
		}

		@Override
		public Parent parent() {
			return null;
		}
	};

	private Parent parent = LIMBO;

	/**
	 * @return Name of this thing
	 */
	public abstract String name();

	/**
	 * @return Weight of this thing and any contents
	 */
	public abstract int weight();

	/**
	 * @return Size of this thing
	 */
	public abstract Size size();

	/**
	 * @return Parent of this thing
	 */
	public Parent parent() {
		return parent;
	}

	/**
	 * Helper - Builds a stream of the path from this object to its root parent.
	 * @return Path
	 */
	public Stream<Parent> path() {
		final Iterator<Parent> itr = new Iterator<Parent>() {
			private Parent p = Thing.this.parent();

			@Override
			public boolean hasNext() {
				return p != null;
			}

			@Override
			public Parent next() {
				assert hasNext();
				final Parent next = p;
				p = p.parent();
				return next;
			}
		};
		return StreamUtil.toStream(itr);
	}

	/**
	 * Determines the <i>root</i> parent of this object.
	 * @param p Parent
	 * @return Root
	 */
	public Parent root() {
		Parent p = parent;
		while(p.parent() != null) {
			p = p.parent();
		}
		return p;
	}

	/**
	 * Sets the parent of this thing.
	 * @param parent New parent
	 * @throws ActionException if this thing cannot be added to the given parent
	 */
	public void setParent(Parent parent) throws ActionException {
		final String reason = parent.contents().reason(this);
		if(reason != null) throw new ActionException(reason);
		move(parent);
		add();
	}

	/**
	 * Sets the parent of this thing to the first valid ancestor.
	 * @param parent New parent
	 */
	public void setParentAncestor(Parent parent) {
		// Check whether can add to next parent
		Parent p = parent;
		final String reason = p.contents().reason(this);

		// Find ancestor
		if(reason != null) {
			// Walk to first valid parent
			while(true) {
				p = p.parent();
				if(p == null) throw new RuntimeException("Cannot find ancestor");
				if(p.contents().reason(this) == null) break;
			}

			// Otherwise notify actor
			// TODO - should this be P?
			if(parent instanceof Actor) {
				final Actor actor = (Actor) parent;
				actor.alert(new Message(reason, this));
			}
			else {
				throw new RuntimeException("No valid ancestor: " + this);
			}
		}

		// Move to ancestor
		move(p);
		add();
	}

	/**
	 * Moves this thing to the given parent.
	 * @param parent Parent
	 */
	protected void move(Parent parent) {
		this.parent.contents().remove(this);
		this.parent = parent;
	}

	/**
	 * Adds this thing to its parent.
	 */
	private void add() {
		parent.contents().add(this);
	}

	/**
	 * Builds a description of this thing.
	 * @return Description
	 */
	public abstract Description describe();

	/**
	 * @param type Emission type
	 * @return Emission
	 */
	public Optional<Emission> emission(Emission.Type type) {
		return Optional.empty();
	}

	/**
	 * Applies damage to this thing, default implementation does nothing.
	 * @param type		Damage type
	 * @param amount	Amount of damage
	 */
	protected void damage(DamageType type, int amount) {
		// Does nowt
	}

	/**
	 * @return Whether this is a sentient entity that can receive notifications (default is <tt>false</tt>)
	 */
	public boolean isSentient() {
		return false;
	}

	/**
	 * @return Whether this object is omitted from location descriptions (default is <tt>false</tt>)
	 */
	public boolean isQuiet() {
		return false;
	}

	/**
	 * @return Whether this thing is dead
	 */
	public boolean isDead() {
		return parent == LIMBO;
	}

	/**
	 * Alerts this object.
	 * @param n Notification
	 */
	public void alert(Notification n) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Destroys this thing.
	 */
	protected void destroy() {
		move(LIMBO);
	}

	@Override
	public String toString() {
		return name();
	}
}
