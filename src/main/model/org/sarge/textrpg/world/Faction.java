package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.OpeningTimes;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.PeriodModel;

/**
 * A <i>faction</i> defines a political group in the world and its relationships to other alignments.
 * @author Sarge
 * TODO - do we need a hierarchy/graph of factions? e.g. bree friendly with shire
 */
public class Faction extends AbstractEqualsObject {
	/**
	 * Association with a faction.
	 */
	public static final class Association extends AbstractEqualsObject {
		private final Faction faction;
		private final Relationship relationship;

		/**
		 * Constructor.
		 * @param faction			Faction
		 * @param relationship		Relationship
		 */
		public Association(Faction faction, Relationship relationship) {
			this.faction = notNull(faction);
			this.relationship = notNull(relationship);
		}

		/**
		 * @return Faction
		 */
		public Faction faction() {
			return faction;
		}

		/**
		 * @return Relationship
		 */
		public Relationship relationship() {
			return relationship;
		}

		@Override
		public String toString() {
			return faction.name + "-" + relationship;
		}
	}

	private final String name;
	private final Alignment alignment;
	private final Area area;
	private final PeriodModel<OpeningTimes> times;
	private final Calendar calendar;

	/**
	 * Constructor.
	 * @param name			Faction name
	 * @param alignment		Alignment of this faction
	 * @param area			Main area of this faction
	 * @param times			Default opening times for gates and shops belonging to this faction
	 * @param calendar		Default calendar for this faction
	 */
	public Faction(String name, Alignment alignment, Area area, PeriodModel<OpeningTimes> times, Calendar calendar) {
		this.name = notEmpty(name);
		this.alignment = notNull(alignment);
		this.area = notNull(area);
		this.times = notNull(times);
		this.calendar = notNull(calendar);
	}

	/**
	 * @return Name of this faction
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Alignment of this faction
	 */
	public Alignment alignment() {
		return alignment;
	}

	/**
	 * @return Main area of this faction
	 */
	public Area area() {
		return area;
	}

	/**
	 * @return Default opening times in this faction
	 */
	public PeriodModel<OpeningTimes> opening() {
		return times;
	}

	/**
	 * @return Calendar used by this faction
	 */
	public Calendar calendar() {
		return calendar;
	}

	/**
	 * Determines the default relationship level of this faction towards the given alignment.
	 * @param alignment Alignment
	 * @return Default relationship
	 */
	public Relationship relationship(Alignment that) {
		if((alignment == Alignment.NEUTRAL) || (that == Alignment.NEUTRAL)) {
			return Relationship.NEUTRAL;
		}
		else
		if(alignment == that) {
			return Relationship.FRIENDLY;
		}
		else {
			return Relationship.ENEMY;
		}
	}
}
