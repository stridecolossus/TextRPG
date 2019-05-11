package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.util.Converter;

public class DataTableTest {
	private enum MockEnum {
		ONE,
		TWO
	}

	private DataTable<MockEnum> table;

	@BeforeEach
	public void before() {
		final var map = Map.of(
			MockEnum.ONE, List.of("1", "true"),
			MockEnum.TWO, List.of("2", "false")
		);
		table = new DataTable<>(List.of("key", "integer", "boolean"), map);
	}

	@Test
	public void column() {
		assertEquals(Map.of(MockEnum.ONE, 1, MockEnum.TWO, 2), table.column("integer", Converter.INTEGER));
		assertEquals(Map.of(MockEnum.ONE, true, MockEnum.TWO, false), table.column("boolean", Converter.BOOLEAN));
	}

	@Test
	public void load() throws IOException {
		final var loaded = DataTable.load(MockEnum.class, new StringReader("key integer boolean \n ONE 1 true\n TWO 2 false"), true);
		assertEquals(table, loaded);
	}

	@Test
	public void loadEmpty() throws IOException {
		TestHelper.expect(IOException.class, "Empty table", () -> DataTable.load(MockEnum.class, new StringReader(""), false));
	}

	@Test
	public void loadUnknownKey() throws IOException {
		TestHelper.expect(IllegalArgumentException.class, "Unknown enum constant", () -> DataTable.load(MockEnum.class, new StringReader("key integer boolean \n cobblers 1 true"), false));
	}

	@Test
	public void loadInvalidRow() throws IOException {
		TestHelper.expect(IllegalArgumentException.class, "mis-match", () -> DataTable.load(MockEnum.class, new StringReader("key integer boolean \n ONE 1"), false));
	}

	@Test
	public void loadIncompleteTable() throws IOException {
		DataTable.load(MockEnum.class, new StringReader("key integer boolean \n ONE 1 true"), false);
		TestHelper.expect(IOException.class, "Incomplete table", () -> DataTable.load(MockEnum.class, new StringReader("key integer boolean \n ONE 1 true"), true));
	}
}
