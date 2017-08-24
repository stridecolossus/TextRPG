package org.sarge.textrpg.entity;

import org.sarge.lib.object.ToString;
import org.sarge.textrpg.common.EnvironmentNotification;
import org.sarge.textrpg.common.MovementNotification;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.Notification.Handler;
import org.sarge.textrpg.common.RevealNotification;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.ModelUtil;

/**
 * Creature.
 * @author Sarge
 */
public class Creature extends Entity {
	/**
	 * AI notification handler.
	 */
	private final Notification.Handler handler = new Notification.Handler() {
		@Override
		public void handle(Notification n) {
			// Ignored
		}

		@Override
		public void handle(MovementNotification move) {
			// TODO - fight/flee/pursue
		}

		@Override
		public void handle(EnvironmentNotification env) {
			// TODO
			// - fight/flee
			// - investigate if remote
		}

		@Override
		public void handle(RevealNotification reveal) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String toString() {
			return ToString.toString(this);
		}
	};
	
	/**
	 * Constructor.
	 * @param race			Race
	 * @param manager		Entity manager
	 */
	public Creature(Race race, EntityManager manager) {
		this(race, race.getAttributes().getAttributes(), manager);
	}
	
	/**
	 * Constructor.
	 * @param race			Race
	 * @param attrs			Initial attributes
	 * @param manager		Entity manager
	 */
	protected Creature(Race race, IntegerMap<Attribute> attrs, EntityManager manager) {
		super(race, attrs, manager);
		init();
	}
	
	/**
	 * Equips default equipment.
	 * @throws RuntimeException if the equipment is not valid
	 */
	private void init() {
		final Equipment equipment = getEquipment();
		race.getEquipment().getEquipment().map(ObjectDescriptor::create).forEach(ModelUtil.wrap(equipment::equip));
	}
	
	@Override
	protected String getDescriptionKey() {
		return "creature";
	}
	
	@Override
	public Handler getNotificationHandler() {
		return handler;
	}
	
	/**
	 * @return Gender of this entity
	 */
	@Override
	public Gender getGender() {
		return race.getAttributes().getDefaultGender();
	}
	
	/**
	 * @return Alignment of this entity
	 */
	@Override
	public Alignment getAlignment() {
		return race.getAttributes().getDefaultAlignment();
	}
}
