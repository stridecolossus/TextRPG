package org.sarge.textrpg.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sarge.lib.util.Converter;

public class DataTableTest {
	private enum MockEnum {
		ONE,
		TWO
	}

	private DataTable table;

	@Before
	public void before() {
		final String[][] rows = {
			{"ONE", "1", "2"},
			{"TWO", "3", "4"},
		};
		table = new DataTable(Arrays.asList("key", "a", "b"), rows);
	}

	@Test
	public void get() {
		assertEquals("ONE", table.get(0, "key"));
		assertEquals("1", table.get(0, "a"));
		assertEquals("2", table.get(0, "b"));
		assertEquals("TWO", table.get(1, "key"));
		assertEquals("3", table.get(1, "a"));
		assertEquals("4", table.get(1, "b"));
	}

	@Test
	public void getRow() {
		assertArrayEquals(new String[]{"ONE", "1", "2"}, table.getRow(0).toArray());
		assertArrayEquals(new String[]{"TWO", "3", "4"}, table.getRow(1).toArray());
	}

	@Test
	public void getColumn() {
		final Map<String, Integer> map = table.getColumn("a", Converter.INTEGER);
		assertEquals(new Integer(1), map.get("ONE"));
		assertEquals(new Integer(3), map.get("TWO"));
	}

	@Test
	public void load() throws IOException {
		final String str = "key a b \n ONE 1 2 \n TWO 3 4 \n";
		table = DataTable.load(new StringReader(str));
		assertNotNull(table);
		get();
		getColumn();
	}
}
