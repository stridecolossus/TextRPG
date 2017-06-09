package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;

import javax.lang.model.element.Modifier;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.util.MutableIntegerMap;

public class MutableIntegerMapTest {
	private static final Modifier KEY = Modifier.DEFAULT;
	
	private MutableIntegerMap<Modifier> map;
	
	@Before
	public void before() {
		map = new MutableIntegerMap<>(Modifier.class);
	}
	
	@Test
	public void set() {
		map.set(KEY, 42);
		assertEquals(42, map.get(KEY));
	}
	
	@Test
	public void reset() {
		map.set(Modifier.class, 42);
		assertEquals(42, map.get(KEY));
	}
	
	@Test
	public void add() {
		map.add(KEY, 1);
		map.add(KEY, 2);
		assertEquals(3, map.get(KEY));
	}
	
	@Test
	public void copyConstructor() {
		map.set(KEY, 42);
		final MutableIntegerMap<Modifier> copy = new MutableIntegerMap<>(Modifier.class, map);
		assertEquals(42, copy.get(KEY));
	}
}
