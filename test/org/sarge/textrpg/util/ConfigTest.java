package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class ConfigTest {
	private Config cfg;

	@Before
	public void before() {
		final Properties props = new Properties();
		props.setProperty("value", "1");
		cfg = new Config(props);
	}
	
	@Test
	public void get() {
		assertEquals(1, cfg.getInteger("value"));
		assertEquals(1L, cfg.getLong("value"));
		assertEquals(1f, cfg.getFloat("value"), 0.001f);
	}
}
