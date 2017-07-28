package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.sarge.textrpg.util.BandingTable;

public class BandingTableTest {
	@Test
	public void build() {
		final BandingTable table = new BandingTable.Builder()
			.add(0, "zero")
			.add(50, "half")
			.add(100, "one")
			.build();
		checkTable(table);
	}
	
	private static void checkTable(BandingTable table) {
		assertEquals("zero", table.get(0));
		assertEquals("half", table.get(25));
		assertEquals("half", table.get(50));
		assertEquals("one", table.get(75));
		assertEquals("one", table.get(100));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void buildInvalid() {
		new BandingTable.Builder()
			.add(1, "one")
			.add(0, "zero");
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildDuplicateName() {
		new BandingTable.Builder()
			.add(0, "same")
			.add(1, "same");
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildEmptyTable() {
		new BandingTable.Builder().build();
	}
	
	@Test
	public void load() throws IOException {
		final String in =
			"0 zero \n" +
			"50 half \n" +
			"100 one \n";
		final BandingTable table = BandingTable.load(new StringReader(in));
		checkTable(table);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void loadInvalidEntry() throws IOException {
		BandingTable.load(new StringReader("cobblers"));
	}
}
