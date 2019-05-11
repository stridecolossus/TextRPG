package org.sarge.textrpg.entity;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.sarge.textrpg.util.MutableIntegerMap.MutableEntry;
import org.sarge.textrpg.util.Persistent;

/**
 * Player settings and preferences.
 * @author Sarge
 */
public class PlayerSettings extends AbstractEqualsObject implements Persistent {
	/**
	 * Settings.
	 */
	public enum Setting implements CommandArgument {
		/**
		 * Amount of money.
		 */
		CASH(false),

		/**
		 * Earned experience.
		 */
		EXPERIENCE(false),

		/**
		 * Available skill points.
		 */
		POINTS(false),

		/**
		 * Locations descriptions are brief.
		 */
		BRIEF(true),

		/**
		 * Player will try to swim.
		 */
		SWIM(true),

		/**
		 * Player will only attempt <i>safe</i> climbs.
		 */
		CLIMB_SAFE(true),

		/**
		 * Whether this player allows PC followers.
		 */
		ALLOW_FOLLOW(true),

		/**
		 * Minimum health for auto-flee.
		 */
		AUTO_FLEE(false);

		// TODO
		// - always craft all
		// - muted channels
		// - default language?

		private boolean bool;

		private Setting(boolean bool) {
			this.bool = bool;
		}

		/**
		 * @return Whether this is a boolean setting
		 */
		public boolean isBoolean() {
			return bool;
		}

		/**
		 * @return Whether this setting is a preference
		 */
		public boolean isPreference() {
			switch(this) {
			case CASH:
			case EXPERIENCE:
			case POINTS:
				return false;

			default:
				return true;
			}
		}
	}

	private final MutableIntegerMap<Setting> settings = new MutableIntegerMap<>();

	/**
	 * Looks up a setting.
	 * @param setting		Setting
	 * @param bool			Expected setting type
	 * @return Setting entry
	 * @throws IllegalStateException if the setting is not of the expected type
	 */
	private MutableEntry get(Setting setting, boolean bool) {
		if(setting.isBoolean() != bool) throw new IllegalStateException(String.format("Incorrect setting type: expected=%b actual=%s", bool, setting));
		return settings.get(setting);
	}

	/**
	 * Looks up a boolean setting.
	 * @param setting Setting
	 * @return Boolean setting
	 * @throws IllegalStateException if the setting is not of the expected type
	 */
	public boolean toBoolean(Setting setting) {
		final MutableEntry entry = get(setting, true);
		return entry.get() == 1;
	}

	/**
	 * Sets a boolean setting.
	 * @param setting		Setting
	 * @param flag			Boolean setting
	 * @throws IllegalStateException if the setting is not of the expected type
	 */
	public void set(Setting setting, boolean flag) {
		final MutableEntry entry = get(setting, true);
		entry.set(flag ? 1 : 0);
	}

	/**
	 * Looks up an integer setting.
	 * @param setting Setting
	 * @return Integer setting
	 * @throws IllegalStateException if the setting is not of the expected type
	 */
	public int toInteger(Setting setting) {
		final MutableEntry entry = get(setting, false);
		return entry.get();
	}

	/**
	 * Sets an integer setting.
	 * @param setting		Setting
	 * @param value			Integer setting
	 * @throws IllegalStateException if the setting is not of the expected type
	 */
	public void set(Setting setting, int value) {
		Check.oneOrMore(value);
		final MutableEntry entry = get(setting, false);
		entry.set(value);
	}

	/**
	 * Modifies an integer setting.
	 * @param setting		Setting
	 * @param mod			Modifier
	 * @throws IllegalStateException if the setting is not of the expected type
	 */
	public void modify(Setting setting, int mod) {
		final MutableEntry entry = get(setting, false);
		if(entry.get() + mod < 0) throw new IllegalStateException(String.format("Setting cannot be modified to a negative value: setting=%s mod=%d", setting, mod));
		entry.modify(mod);
	}

	/**
	 * Creates a transaction for a non-preference integer setting.
	 * @param setting		Setting
	 * @param amount		Transaction amount
	 * @param message		Exception message
	 * @return Transaction
	 */
	public Transaction transaction(Setting setting, int amount, String message) {
		final MutableEntry entry = get(setting, false);
		if(setting.isPreference()) throw new IllegalArgumentException("Invalid setting for transaction: " + setting);
		return new Transaction(entry, amount, message);
	}
}
