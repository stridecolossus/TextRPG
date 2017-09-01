package org.sarge.textrpg.entity;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.DescriptionStore;
import org.sarge.textrpg.common.EnvironmentNotification;
import org.sarge.textrpg.common.EventHolder;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.MovementNotification;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.Notification.Handler;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.RevealNotification;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.object.Money;
import org.sarge.textrpg.object.TrackedContents;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.runner.ConsoleRunner.Device;
import org.sarge.textrpg.util.DescriptionFormatter;
import org.sarge.textrpg.util.EnglishFormatter;
import org.sarge.textrpg.util.IntegerMap;

/**
 * Player-character.
 * @author Sarge
 * - XP, points
 * - trophy
 */
public class Player extends CharacterEntity {
	// TODO...
	public static DescriptionStore.Repository repo;
	// ...TODO

	/**
	 * Listener for player updates.
	 */
	public interface Listener {
		/**
		 * Updates this listener.
		 * @param target Target object
		 * @return Whether this listener should be removed
		 */
		boolean update(Object target);
	}

	private final Notification.Handler handler = new Notification.Handler() {
		@Override
		public void handle(RevealNotification reveal) {
			final Hidden hidden = reveal.getHidden();
			add(hidden);
			write(reveal);
		}

		@Override
		public void handle(EnvironmentNotification env) {
			write(env);
		}

		@Override
		public void handle(MovementNotification move) {
			write(move);
		}

		@Override
		public void handle(Notification n) {
			write(n);
		}
	};

	/**
	 * Character inventory.
	 */
	private final Contents inv = new TrackedContents() {
		// TODO
		// - limits based on character attributes, e.g. strength > weight

		@Override
		public void add(Thing obj) {
			if(obj instanceof Money) {
				final Money money = (Money) obj;
				modify(EntityValue.CASH, money.value());
				// TODO
				// obj.destroy();
			}
			else {
				super.add(obj);
				update(obj);
			}
		}
	};

	private final Set<Hidden> known = new StrictSet<>();
	private final Map<Object, Listener> listeners = new StrictMap<>();

	private final Device dev;
	private final DescriptionFormatter formatter; // TODO

	private boolean swim;
	private WorldObject prev;

	/**
	 * constructor.
	 * @param name		Player name
	 * @param race		Race
	 * @param attrs		Attributes
	 * @param gender	Gender
	 * @param align		Alignment
	 * @param dev		Device
	 */
	public Player(String name, Race race, IntegerMap<Attribute> attrs, Gender gender, Alignment align, Device dev) {
		super(name, race, attrs, EntityManager.IDLE, gender, align, Collections.emptyList());
		Check.notEmpty(name);
		Check.notNull(dev);
		this.dev = dev;

		// TODO...
		modify(EntityValue.CASH, 1000 + 25);
		modify(EntityValue.EXPERIENCE, 123);
		modify(EntityValue.POINTS, 50);

		if(repo != null)
			formatter = new DescriptionFormatter(repo.find(Locale.UK)::getString); // TODO
		else
			formatter = null;

		// ...TODO
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	/**
	 * @return Previous object used by this player
	 */
	public WorldObject getPreviousObject() {
		return prev;
	}

	/**
	 * Sets the previous object used by this player
	 * @param prev Previous object
	 */
	public void setPreviousObject(WorldObject prev) {
		Check.notNull(prev);
		this.prev = prev;
	}

	@Override
	public Contents getContents() {
		return inv;
	}

	@Override
	protected String getDescriptionKey() {
		return "character";
	}

	/**
	 * Outputs to device.
	 * @param n Notification
	 */
	protected void write(Notification n) {
		// TODO
		try {
			final String text = formatter.format(n.describe());
			dev.write("PLAYER: "+new EnglishFormatter().format(text)+"\n");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public EventHolder getEventHolder() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Handler getNotificationHandler() {
		return handler;
	}

	@Override
	public boolean perceives(Hidden obj) {
		if(super.perceives(obj)) {
			return true;
		}
		else {
			return known.contains(obj);
		}
	}

	/**
	 * Adds a hidden object to those known by this player.
	 * @param hidden Hidden object
	 */
	public void add(Hidden hidden) {
		known.add(hidden);
		getEventQueue().add(() -> forget(hidden), hidden.getForgetPeriod());
	}

	/**
	 * Forgets the given hidden object.
	 * @param hidden Hidden object
	 */
	public void forget(Hidden hidden) {
		known.remove(hidden);
	}

	@Override
	public boolean isSwimming() {
		return swim;
	}

	/**
	 * Sets whether this player can enter water locations.
	 * @param swim Swimming
	 */
	public void setSwimming(boolean swim) {
		this.swim = swim;
	}

	/**
	 * Registers a listener for the given target.
	 * @param target		Target
	 * @param listener		Listener
	 */
	public void add(Object target, Listener listener) {
		listeners.put(target, listener);
	}

	/**
	 * Updates and removes the listener on the given target.
	 * @param target Target
	 */
	private void update(Object target) {
		final Listener listener = listeners.get(target);
		if(listener != null) {
			final boolean remove = listener.update(target);
			if(remove) {
				listeners.remove(target);
			}
		}
	}

	@Override
	public void setParent(Parent parent) throws ActionException {
		update(parent);
		super.setParent(parent);
	}
}
