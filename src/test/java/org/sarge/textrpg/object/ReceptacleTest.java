package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.object.Receptacle.Descriptor;

public class ReceptacleTest extends ActionTest {
	private Receptacle rec, inf;
	private Descriptor descriptor;

	@Before
	public void before() {
		final ObjectDescriptor obj = new Builder("receptacle").weight(2).build();
		descriptor = new Descriptor(obj, Liquid.WATER, 3, false);
		rec = new Receptacle(descriptor);
		inf = new Receptacle(new Descriptor(obj, Liquid.WATER, Receptacle.INFINITE, false));
	}

	@Test
	public void constructor() {
		assertEquals(descriptor, rec.descriptor());
		assertEquals("receptacle", descriptor.getDescriptionKey());
		assertEquals(3, rec.level());
		assertEquals(2 + 3, rec.weight());
		assertEquals(false, rec.isFixture());
	}

	@Test
	public void constructorInfinite() {
		assertEquals(Receptacle.INFINITE, inf.level());
	}

	@Test
	public void describe() {
		final Description desc = rec.describe();
		assertEquals("{receptacle}", desc.get("name"));
		assertEquals("3", desc.get("level"));
		assertEquals("water", desc.get("liquid"));
	}

	@Test
	public void consume() throws ActionException {
		// Consume some of the contents
		int actual = rec.consume(2);
		assertEquals(2, actual);
		assertEquals(1, rec.level());
		assertEquals(2 + 1, rec.weight());

		// Consume the remainder
		actual = rec.consume(999);
		assertEquals(1, actual);
		assertEquals(0, rec.level());
	}

	@Test
	public void consumeEmpty() throws ActionException {
		rec.consume(999);
		expect("receptacle.consume.empty");
		rec.consume(1);
	}

	@Test
	public void consumeInfinite() throws ActionException {
		assertEquals(999, inf.consume(999));
		assertEquals(Receptacle.INFINITE, inf.level());
	}

	@Test
	public void fill() throws ActionException {
		final Receptacle src = new Receptacle(descriptor);
		rec.consume(2);
		rec.fill(src);
		assertEquals(3, rec.level());
		assertEquals(1, src.level());
	}

	@Test
	public void fillInvalidSource() throws ActionException {
		final Receptacle src = new Receptacle(new Descriptor(new ObjectDescriptor("invalid"), Liquid.OIL, 42, false));
		rec.consume(2);
		expect("fill.invalid.source");
		rec.fill(src);
	}

	@Test
	public void fillAlreadyFull() throws ActionException {
		final Receptacle src = new Receptacle(descriptor);
		expect("fill.already.full");
		rec.fill(src);
	}

	@Test
	public void fillSelf() throws ActionException {
		rec.consume(1);
		expect("fill.self");
		rec.fill(rec);
	}

	@Test
	public void fillInfiniteReceptacle() throws ActionException {
		expect("fill.infinite.receptacle");
		inf.fill(rec);
	}

	@Test
	public void fillEmptySource() throws ActionException {
		final Receptacle src = new Receptacle(descriptor);
		src.consume(999);
		rec.consume(2);
		expect("fill.empty.source");
		rec.fill(src);
	}

	@Test
	public void fillInfiniteSource() throws ActionException {
		rec.consume(3);
		rec.fill(inf);
		assertEquals(3, rec.level());
		assertEquals(Receptacle.INFINITE, inf.level());
	}

	@Test
	public void empty() throws ActionException {
		rec.empty();
		assertEquals(0, rec.level());
	}

	@Test
	public void emptyAlreadyEmpty() throws ActionException {
		rec.empty();
		expect("receptacle.already.empty");
		rec.empty();
	}

	@Test
	public void emptyInfinite() throws ActionException {
		expect("receptacle.empty.infinite");
		inf.empty();
	}

	@Test
	public void destroy() {
		rec.destroy();
		assertEquals(0, rec.level());
	}
}
