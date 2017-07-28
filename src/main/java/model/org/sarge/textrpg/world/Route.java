package org.sarge.textrpg.world;

/**
 * Route types.
 * @author Sarge
 */
public enum Route {
	NONE(""),
	TRAIL("-"),
	PATH("-"),
	LANE("-"),
	ROAD("="),
	STREET("="),
	CORRIDOR(""),
	TUNNEL("^"),
	STAIR("+"),
	BRIDGE("("),
	LADDER("#"),
	ROPE("|"),
	RIVER("~"),
	FORD("~");
	
	private final String icon;
	
	private Route(String icon) {
		this.icon = icon;
	}

	public String getLeftIcon() {
		return icon;
	}
	
	public String getRightIcon() {
		if(this == BRIDGE) {
			return "(";
		}
		else {
			return icon;
		}
	}
}
