package org.sarge.textrpg.world;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.util.TextHelper;

/**
 * Route types.
 * @author Sarge
 */
public enum Route implements CommandArgument {
	NONE(' '),
	TRAIL('-'),
	PATH('-'),
	LANE('-'),
	ROAD('='),
	STREET('='),
	CORRIDOR(' '),
	TUNNEL('^'),
	STAIR('+'),
	BRIDGE(' '),
	LADDER('#'),
	RIVER('~'),
	FORD('~');

	/**
	 * Converter.
	 */
	public static final Converter<Route> CONVERTER = Converter.enumeration(Route.class);

	private final char icon;

	private Route(char icon) {
		this.icon = icon;
	}

	/**
	 * Wraps the given string with the icon for this route.
	 * @param str String to wrap
	 * @return Wrapped string
	 */
	public String wrap(String str) {
		switch(this) {
		case NONE:		return str;
		case BRIDGE:	return TextHelper.wrap(str, ')', '(');
		default:		return TextHelper.wrap(str, icon);
		}
	}

	/**
	 * @return Whether this route can be followed
	 */
	public boolean isFollowRoute() {
		switch(this) {
		case NONE:
		case BRIDGE:
		case LADDER:
		case FORD:
			return false;

		default:
			return true;
		}
	}
}
