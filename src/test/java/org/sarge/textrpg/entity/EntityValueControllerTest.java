package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.runner.SessionManager;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;

public class EntityValueControllerTest extends ActionTestBase {
	private EntityValueController controller;
	private Event.Queue queue;
	private SessionManager manager;
	private EntityValueCalculator update;

	@BeforeEach
	public void before() {
		// Create update calculator
		update = new EntityValueCalculator.Builder()
			.add(EntityValue.HEALTH, Calculation.literal(1))
			.add(EntityValue.STAMINA, Calculation.literal(2))
			.add(EntityValue.POWER, Calculation.literal(3))
			.build();

		// Init session manager
		manager = mock(SessionManager.class);
		when(manager.players()).thenReturn(Stream.of(actor));

		// Create controller
		queue = new Event.Queue.Manager().queue("entity");
		controller = new EntityValueController(queue, manager, update, update);
		controller.setPeriod(DURATION);

		// Init actor
		final var power = actor.model().values().get(EntityValue.POWER.key());
		power.set(100);
		power.modify(-100);
	}

	@Test
	public void start() {
		assertEquals(1, queue.size());
	}

	@Test
	public void init() {
		final EntityValueIntegerMap map = actor.model().values();
		controller.init(actor);
		assertEquals(1, map.get(EntityValue.HEALTH.key()).get());
		assertEquals(2, map.get(EntityValue.STAMINA.key()).get());
		assertEquals(3, map.get(EntityValue.POWER.key()).get());
	}

	@Test
	public void initZeroValue() {
		update = new EntityValueCalculator.Builder()
			.add(EntityValue.HEALTH, Calculation.literal(1))
			.add(EntityValue.STAMINA, Calculation.literal(0))
			.add(EntityValue.POWER, Calculation.literal(3))
			.build();
		controller = new EntityValueController(queue, manager, update, update);
		assertThrows(IllegalStateException.class, () -> controller.init(actor));
	}

	@Test
	public void increment() {
		controller.increment();
		final EntityValueIntegerMap map = actor.model().values();
		assertEquals(1, map.get(EntityValue.THIRST.key()).get());
		assertEquals(1, map.get(EntityValue.HUNGER.key()).get());
	}

	@Test
	public void incrementAlert() {
		final EntityValueIntegerMap map = actor.model().values();
		map.get(EntityValue.THIRST.key()).modify(1);
		controller.setThreshold(1);
		controller.increment();
		verify(actor).alert(new Description("entity.alert.thirst"));
	}

	@Test
	public void updateIgnored() {
		controller.update(actor);
		verify(actor.manager()).updated();
		verifyNoMoreInteractions(actor.manager());
	}

	@Test
	public void update() {
		// Apply update
		queue.manager().advance(2500L);
		controller.update(actor);

		// Check updated to whole tick
		verify(actor.manager()).update(2000L);

		// Check values incremented by number of ticks
		assertEquals(2 * 3, actor.model().values().get(EntityValue.POWER.key()).get());
	}


	@Test
	public void updateStanceModifier() {
		queue.manager().advance(2500L);
		controller.setStanceModifier(whatever -> 4f);
		controller.update(actor);
		assertEquals(2 * 3 * 4, actor.model().values().get(EntityValue.POWER.key()).get());
	}

	@Test
	public void updateIgnoredSwimming() {
		queue.manager().advance(2500L);
		when(actor.model().stance()).thenReturn(Stance.SWIMMING);
		controller.update(actor);
		assertEquals(0, actor.model().values().get(EntityValue.POWER.key()).get());
	}
}
