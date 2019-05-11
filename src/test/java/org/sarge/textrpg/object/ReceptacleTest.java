package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class ReceptacleTest {
	private Receptacle rec;
	private Receptacle infinite;

	@BeforeEach
	public void before() {
		rec = create("rec", 4);
		infinite = create("infinite", Receptacle.INFINITE);
	}

	private static Receptacle create(String name, int level) {
		return new Receptacle(new Receptacle.Descriptor(ObjectDescriptor.of(name), Liquid.WATER, level));
	}

	@Test
	public void constructor() {
		assertEquals("rec", rec.name());
		assertNotNull(rec.descriptor());
		assertEquals(4, rec.level());
		assertEquals(4, rec.weight());
		assertEquals(Liquid.WATER, rec.descriptor().liquid());
	}

	@Test
	public void describe() {
		// Partially consume
		rec.consume(2);

		// Register level formatter
		final ArgumentFormatter.Registry formatters = new ArgumentFormatter.Registry();
		formatters.add("receptacle.level", ArgumentFormatter.PLAIN);

		// Describe
		final Description description = rec.describe(true, formatters);
		final Description.Entry level = description.get("level");
		assertNotNull(level);
		assertEquals(Percentile.HALF, level.argument());
	}

	@Test
	public void consume() throws ActionException {
		assertEquals(1, rec.consume(1));
		assertEquals(3, rec.level());
	}

	@Test
	public void consumeRemainder() throws ActionException {
		assertEquals(4, rec.consume(999));
		assertEquals(0, rec.level());
	}

	@Test
	public void consumeEmpty() throws ActionException {
		rec.empty();
		assertThrows(IllegalArgumentException.class, () -> rec.consume(1));
	}

	@Test
	public void consumeInfinite() throws ActionException {
		assertEquals(1, infinite.consume(1));
		assertEquals(Receptacle.INFINITE, infinite.level());
	}

	@Test
	public void empty() throws ActionException {
		rec.empty();
		assertEquals(0, rec.level());
	}

	@Test
	public void emptyAlreadyEmpty() throws ActionException {
		rec.empty();
		TestHelper.expect("receptacle.already.empty", rec::empty);
	}

	@Test
	public void emptyInfinite() throws ActionException {
		TestHelper.expect("receptacle.empty.infinite", infinite::empty);
	}

	@Test
	public void fillComplete() throws ActionException {
		final Receptacle src = create("src", 10);
		rec.consume(3);
		rec.fill(src);
		assertEquals(4, rec.level());
		assertEquals(7, src.level());
	}

	@Test
	public void fillPartial() throws ActionException {
		final Receptacle src = create("src", 1);
		rec.consume(3);
		rec.fill(src);
		assertEquals(2, rec.level());
		assertEquals(0, src.level());
	}

	@Test
	public void fillFromInfiniteSource() throws ActionException {
		rec.consume(3);
		rec.fill(infinite);
		assertEquals(4, rec.level());
	}

	@Test
	public void fillInfinite() throws ActionException {
		TestHelper.expect("receptacle.fill.infinite", () -> infinite.fill(rec));
	}

	@Test
	public void fillAlreadyFull() throws ActionException {
		TestHelper.expect("receptacle.already.full", () -> rec.fill(infinite));
	}

	@Test
	public void fillEmptySource() throws ActionException {
		final Receptacle src = create("src", 1);
		src.empty();
		rec.consume(1);
		TestHelper.expect("receptacle.source.empty", () -> rec.fill(src));
	}

	@Test
	public void fillSelf() throws ActionException {
		rec.consume(1);
		assertThrows(IllegalArgumentException.class, () -> rec.fill(rec));
	}

	@Test
	public void fillInvalidLiquid() throws ActionException {
		final var invalid = new Receptacle(new Receptacle.Descriptor(ObjectDescriptor.of("invalid"), Liquid.OIL, 42));
		rec.consume(1);
		TestHelper.expect("receptacle.fill.invalid", () -> rec.fill(invalid));
	}
}
