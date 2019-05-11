package org.sarge.textrpg.world;

import java.util.Set;

import org.sarge.lib.util.Converter;

/**
 * Properties of a location.
 */
public enum Property {
	/**
	 * Location does not have a description.
	 */
	NOT_DESCRIBED,

    /**
     * Location has drinking water available, e.g. a pool or river.
     */
    WATER,

    /**
     * Location can be fished.
     */
    FISH,

    /**
     * Location is floor-less (assumes the bottom of this location is the exit in the <b>down</b> direction).
     */
    FLOORLESS,

    /**
     * Location is a vantage point for watching nearby entities.
     */
    VANTAGE_POINT,

    /**
     * Hint for busy locations such as towns or frequently visited POIs.
     */
    BUSY,

    /**
     * Location is a save checkpoint.
     */
    SAVE_POINT;

	/**
	 * Checks that the size of the properties enumeration does not exceed the available number of bits.
	 */
	static {
		assert Property.values().length < Byte.SIZE;
	}

	/**
	 * Properties converter.
	 */
	public static final Converter<Property> CONVERTER = Converter.enumeration(Property.class);

	/**
	 * Converts the given set of properties to a bit-field.
	 * @param props Properties
	 * @return Properties as a bit-field
	 */
	public static byte toBitField(Set<Property> props) {
		return (byte) props.stream().mapToInt(p -> p.bit).reduce(0, (a, b) -> a | b);
	}

    private final byte bit;

    private Property() {
        this.bit = (byte) (1 << ordinal());
    }

    /**
     * Tests whether the compressed properties byte contains this property.
     * @param props Properties byte
     * @return Whether this property is present
     */
    public boolean isProperty(byte props) {
		return (props & bit) == bit;
	}

    /**
     * @return Whether this property can be used as an area over-ride
     */
    public boolean isAreaProperty() {
    	switch(this) {
    	case WATER:
    	case FISH:
    	case BUSY:
    		return true;

    	default:
    		return false;
    	}
    }
}
