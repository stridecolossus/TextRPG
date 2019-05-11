package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.function.Executable;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.object.DurableObject;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.object.WorldObject;

/**
 * Test utilities.
 * @author Sarge
 */
public final class TestHelper {
	private TestHelper() {
	}

	public static Exception expect(Class<? extends Exception> expectedType, String expected, Executable executable) {
		Check.notEmpty(expected);
		final Exception actual = assertThrows(expectedType, executable);
		assertTrue(actual.getMessage().toLowerCase().contains(expected.toLowerCase()), () -> String.format("Expected: [%s] Actual: [%s]", expected, actual));
		return actual;
	}

	public static Exception expect(String message, Executable executable) {
		final Exception actual = assertThrows(ActionException.class, executable);
		assertEquals(message, actual.getMessage());
		return actual;
	}

	public static Parent parent() {
		final Contents contents = new Contents();

		return new Parent() {
			@Override
			public Parent parent() {
				return null;
			}

			@Override
			public String name() {
				return "mock.parent";
			}

			@Override
			public Contents contents() {
				return contents;
			}
		};
	}

	/**
	 * Creates a light-emitting object.
	 * @param parent Parent
	 * @return Light
	 */
	public static WorldObject light(Parent parent) {
		final WorldObject light = new WorldObject(ObjectDescriptor.of("light")) {
			@Override
			public Percentile emission(Emission emission) {
				return Percentile.ONE;
			}
		};
		light.parent(parent);
		return light;
	}

	/**
	 * Creates a weapon.
	 * @param skill Required skill
	 * @return Weapon
	 */
	public static Weapon weapon(Skill skill, Damage.Type type) {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("weapon").category("cat").slot(Slot.MAIN).build();
		final Damage damage = new Damage(type, Calculation.ZERO, Effect.NONE);
		final Weapon.Descriptor weapon = new Weapon.Descriptor(new DurableObject.Descriptor(descriptor, 1), skill, 1, damage, null, null);
		return weapon.create();
	}

	public static LootFactory loot() {
		return LootFactory.of(ObjectDescriptor.of("loot"), 1);
	}

	public static Event.Queue queue() {
		final Event.Queue.Manager manager = new Event.Queue.Manager();
		return manager.queue("queue");
	}
}
