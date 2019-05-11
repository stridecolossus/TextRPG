package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.BandingTable.Builder;
import org.sarge.textrpg.util.BandingTable.Loader;

public class BandingTableTest {
	@Test
	public void builder() {
		// Create table
		final BandingTable<Percentile> table = new Builder<Percentile>()
			.add(Percentile.HALF, "half")
			.add(Percentile.ONE, "one")
			.build();

		// Check bands
		assertEquals("half", table.map(Percentile.ZERO));
		assertEquals("half", table.map(Percentile.HALF));
		assertEquals("one", table.map(Percentile.ONE));

		// Check maximum
		assertEquals(Percentile.ONE, table.max());
	}

	@Test
	public void empty() {
		assertThrows(IllegalArgumentException.class, () -> new Builder<>().build());
	}

	@Test
	public void notAscending() {
		assertThrows(IllegalArgumentException.class, () -> new Builder<Percentile>().add(Percentile.ONE, "one").add(Percentile.HALF, "half").build());
	}

	@Test
	public void load() throws IOException {
		final Loader<Percentile> loader = new Loader<>(Percentile.CONVERTER);
		final BandingTable<Percentile> table = loader.load(new StringReader("50 half \n 100 one"));
		final BandingTable<Percentile> expected = new Builder<Percentile>().add(Percentile.HALF, "half").add(Percentile.ONE, "one").build();
		assertEquals(expected, table);
	}

	@Test
	public void loadInvalidLine() throws IOException {
		final Loader<Percentile> loader = new Loader<>(Percentile.CONVERTER);
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("50")));
	}
}
