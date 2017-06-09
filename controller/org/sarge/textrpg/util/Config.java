package org.sarge.textrpg.util;

import java.util.Properties;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;

/**
 * Configuration.
 * @author Sarge
 */
public class Config {
	private final Properties props;

	/**
	 * Constructor.
	 * @param props Properties
	 */
	public Config(Properties props) {
		Check.notNull(props);
		this.props = props;
	}
	
	/**
	 * Looks up a config setting.
	 * @param key			Key
	 * @param converter		Converter
	 * @return Config setting
	 * @throws IllegalArgumentException if the setting cannot be found
	 */
	public <T> T get(String key, Converter<T> converter) {
		final String value = props.getProperty(key);
		if(value == null) throw new IllegalArgumentException("Unknown key: " + key);
		return converter.convert(value);
	}
	
	/**
	 * Looks up an integer config setting.
	 * @param key Key
	 * @return Integer value
	 * @throws IllegalArgumentException if the setting cannot be found
	 */
	public int getInteger(String key) {
		return get(key, Converter.INTEGER);
	}
	
	/**
	 * Looks up a long config setting.
	 * @param key Key
	 * @return Long value
	 * @throws IllegalArgumentException if the setting cannot be found
	 */
	public long getLong(String key) {
		return get(key, Converter.LONG);
	}
	
	/**
	 * Looks up a floating-point config setting.
	 * @param key Key
	 * @return Floating-point value
	 * @throws IllegalArgumentException if the setting cannot be found
	 */
	public float getFloat(String key) {
		return get(key, Converter.FLOAT);
	}
}
