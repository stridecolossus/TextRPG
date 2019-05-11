package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.object.Light.Type;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class LightTest {
	private Light light;

	private void create(Type type) {
		light = new Light(new Light.Descriptor(ObjectDescriptor.of(type.name()), type, Duration.ofMillis(2), Percentile.HALF, Percentile.ZERO));
	}

	@Nested
	class Default {
		@BeforeEach
		public void before() {
			create(Type.DEFAULT);
		}

		@Test
		public void constructor() {
			assertEquals("DEFAULT", light.name());
			assertNotNull(light.descriptor());
			assertEquals(false, light.isActive());
			assertEquals(false, light.isCovered());
			assertEquals(true, light.isLightable());
			assertEquals(Type.DEFAULT, light.type());
			assertEquals(Percentile.ZERO, light.emission(Emission.LIGHT));
			assertEquals(Percentile.ZERO, light.emission(Emission.SMOKE));
		}

		@Test
		public void constructorInvalidLifetime() {
			assertThrows(IllegalArgumentException.class, () -> new Light(new Light.Descriptor(ObjectDescriptor.of("invalid"), Light.Type.DEFAULT, Duration.ZERO, Percentile.HALF, Percentile.ZERO)));
		}

		@Test
		public void light() throws ActionException {
			light.light(0);
			assertEquals(true, light.isActive());
			assertEquals(false, light.isLightable());
			assertEquals(Percentile.HALF, light.emission(Emission.LIGHT));
		}

		@Test
		public void lightAlreadyLit() throws ActionException {
			light.light(0);
			TestHelper.expect("light.already.active", () -> light.light(0));
		}

		@Test
		public void snuff() throws ActionException {
			light.light(0);
			light.snuff(1);
			assertEquals(false, light.isActive());
			assertEquals(true, light.isLightable());
			assertEquals(Percentile.ZERO, light.emission(Emission.LIGHT));
		}

		@Test
		public void snuffNotLit() throws ActionException {
			TestHelper.expect("light.not.active", () -> light.snuff(0));
		}

		@Test
		public void coverCannotCover() throws ActionException {
			TestHelper.expect("light.cannot.cover", () -> light.cover());
		}

		@Test
		public void fillCannotRefuel() throws ActionException {
			TestHelper.expect("light.cannot.refuel", () -> light.fill(mock(Receptacle.class)));
		}

		@Test
		public void find() throws ActionException {
			final Parent parent = TestHelper.parent();
			light.parent(parent);
			light.light(0);
			assertEquals(Optional.of(light), Light.find(parent, Light.Type.DEFAULT));
		}
	}

	@Nested
	class Lantern {
		private Receptacle rec;

		@BeforeEach
		public void before() {
			create(Type.LANTERN);
			rec = new Receptacle(new Receptacle.Descriptor(ObjectDescriptor.of("oil"), Liquid.OIL, 2));
		}

		@Test
		public void constructor() {
			assertEquals(Type.LANTERN, light.type());
		}

		@Test
		public void cover() throws ActionException {
			light.light(0);
			light.cover();
			assertEquals(true, light.isActive());
			assertEquals(true, light.isCovered());
			assertEquals(false, light.isLightable());
			assertEquals(Percentile.ZERO, light.emission(Emission.LIGHT));
		}

		@Test
		public void uncover() throws ActionException {
			light.light(0);
			light.cover();
			light.uncover();
			assertEquals(false, light.isCovered());
			assertEquals(Percentile.ONE, light.visibility());
		}

		@Test
		public void uncoverNotCovered() throws ActionException {
			TestHelper.expect("light.not.covered", () -> light.uncover());
		}

		@Test
		public void fill() throws ActionException {
			light.light(0);
			light.snuff(1);
			light.fill(rec);
			assertEquals(true, light.isLightable());
		}

		@Test
		public void fillInvalidLiquid() throws ActionException {
			final Receptacle water = new Receptacle(new Receptacle.Descriptor(ObjectDescriptor.of("water"), Liquid.WATER, 42));
			TestHelper.expect("light.fill.invalid", () -> light.fill(water));
		}

		@Test
		public void fillAlreadyFull() throws ActionException {
			TestHelper.expect("light.already.filled", () -> light.fill(rec));
		}
	}

	@Nested
	class Campfire {
		@BeforeEach
		public void before() {
			create(Type.CAMPFIRE);
		}

		@Test
		public void placementActive() throws ActionException {
			light.light(0);
			assertEquals("camp.active", light.key(false));
		}

		@Test
		public void placementRemains() {
			assertEquals("camp.remains", light.key(false));
		}
	}

	@Nested
	class Permanent {
		@BeforeEach
		public void before() {
			create(Type.PERMANENT);
		}

		@Test
		public void constructor() {
			assertEquals(Type.PERMANENT, light.type());
			assertEquals(true, light.isActive());
		}

		@Test
		public void permanent() throws ActionException {
			TestHelper.expect("light.snuff.permanent", () -> light.snuff(0));
		}
	}
}
