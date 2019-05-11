package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PeriodModelFactoryTest {
	private PeriodModel.Factory factory;
	private Event.Queue queue;
	private PeriodModel.Period one, two;
	private PeriodModel<PeriodModel.Period> model;

	@BeforeEach
	public void before() {
		final Event.Queue.Manager manager = new Event.Queue.Manager();
		queue = manager.queue("periods");
		factory = new PeriodModel.Factory(queue);
		one = period(1);
		two = period(3);
		model = null;
	}

	/**
	 * Creates a period.
	 */
	private static PeriodModel.Period period(int start) {
		final PeriodModel.Period period = mock(PeriodModel.Period.class);
		when(period.start()).thenReturn(LocalTime.of(start, 0));
		return period;
	}

	/**
	 * Creates the model.
	 */
	private void init() {
		model = factory.create(List.of(one, two));
		assertNotNull(model);
		assertEquals(1, queue.size());
	}

	@Test
	public void createClockBeforeFirstPeriod() {
		init();
		assertEquals(two, model.current());
	}

	@Test
	public void createClockBetweenPeriods() {
		queue.manager().advance(Duration.ofHours(2).toMillis());
		init();
		assertEquals(one, model.current());
	}

	@Test
	public void createClockAfterLastPeriod() {
		queue.manager().advance(Duration.ofHours(5).toMillis());
		init();
		assertEquals(one, model.current());
	}

	@Test
	public void createNotEnoughPeriods() {
		assertThrows(IllegalArgumentException.class, () -> factory.create(List.of()));
		assertThrows(IllegalArgumentException.class, () -> factory.create(List.of(one)));
	}

	@Test
	public void createPeriodsNotAscending() {
		assertThrows(IllegalArgumentException.class, () -> factory.create(List.of(two, one)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void listener() {
		init();
		final PeriodModel.Listener<PeriodModel.Period> listener = mock(PeriodModel.Listener.class);
		model.add(listener);
		queue.manager().advance(Duration.ofHours(1).toMillis());
		verify(listener).update(one);
		assertEquals(one, model.current());
		assertEquals(1, queue.size());
	}
}
