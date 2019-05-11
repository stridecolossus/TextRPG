package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DispenserTest {
	private static final Duration DURATION = Duration.ofMinutes(42);

	private Dispenser dispenser;

	@BeforeEach
	public void before() {
		dispenser = new Dispenser(ObjectDescriptor.of("object"), 1, DURATION);
	}

	@Test
	public void constructor() {
		assertEquals(1, dispenser.maximum());
		assertEquals(DURATION, dispenser.refresh());
	}

	@Test
	public void constructorFixture() {
		assertThrows(IllegalArgumentException.class, () -> new Dispenser(ObjectDescriptor.fixture("fixture"), 1, DURATION));
	}

	@Test
	public void dispense() {
		dispenser.dispense();
	}

	@Test
	public void dispenseLimitExceeded() {
		dispenser.dispense();
		assertThrows(IllegalStateException.class, () -> dispenser.dispense());
	}

	@Test
	public void restore() {
		dispenser.dispense();
		dispenser.restore();
	}

	@Test
	public void restoreNotDispensed() {
		assertThrows(IllegalStateException.class, () -> dispenser.restore());
	}
}
