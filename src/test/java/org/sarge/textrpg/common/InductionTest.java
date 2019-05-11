package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Induction.Flag;
import org.sarge.textrpg.common.Induction.Instance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.TestHelper;

public class InductionTest {
	private static final Duration DURATION = Duration.ofSeconds(1);

	private Induction induction;
	private Induction.Descriptor descriptor;
	private Induction.Manager manager;
	private Event.Queue queue;
	private Consumer<Response> listener;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		induction = mock(Induction.class);
		descriptor = new Induction.Descriptor.Builder().period(DURATION).flag(Flag.SPINNER).build();
		queue = TestHelper.queue();
		listener = mock(Consumer.class);
		manager = new Induction.Manager(queue, listener);
	}

	@Test
	public void constructor() {
		assertEquals(true, descriptor.isFlag(Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Flag.PRIMARY));
		assertEquals(false, descriptor.isFlag(Flag.REPEATING));
		assertEquals(0, queue.size());
		assertEquals(false, manager.isActive());
		assertEquals(false, manager.isPrimary());
	}

	@Test
	public void start() {
		manager.start(new Instance(descriptor, induction));
		assertEquals(true, manager.isActive());
		assertEquals(false, manager.isPrimary());
		assertEquals(1, queue.size());
	}

	@Test
	public void startInductionActive() {
		manager.start(new Instance(descriptor, induction));
		assertThrows(IllegalStateException.class, () -> manager.start(new Instance(descriptor, induction)));
	}

	@Test
	public void complete() throws ActionException {
		when(induction.complete()).thenReturn(Response.OK);
		manager.start(new Instance(descriptor, induction));
		queue.manager().advance(DURATION.toMillis());
		verify(induction).complete();
		verify(listener).accept(Response.OK);
		assertEquals(0, queue.size());
		assertEquals(false, manager.isActive());
	}

	@Test
	public void completeException() throws ActionException {
		final ActionException e = ActionException.of("doh");
		manager.start(new Instance(descriptor, induction));
		when(induction.complete()).thenThrow(e);
		queue.manager().advance(DURATION.toMillis());
		verify(listener).accept(Response.of(e.description()));
		assertEquals(false, manager.isActive());
	}

	@Test
	public void interrupt() {
		manager.start(new Instance(descriptor, induction));
		manager.interrupt();
		verify(induction).interrupt();
		assertEquals(false, manager.isActive());
	}

	@Test
	public void indefinite() throws ActionException {
		// Start indefinite induction
		final Runnable stop = mock(Runnable.class);
		final Induction.Instance instance = Induction.Instance.indefinite(stop);
		assertEquals(false, instance.descriptor().isFlag(Flag.SPINNER));
		assertEquals(false, instance.descriptor().isFlag(Flag.PRIMARY));
		assertEquals(false, instance.descriptor().isFlag(Flag.REPEATING));

		// Start indefinite induction
		manager.start(instance);
		verifyZeroInteractions(stop);
		assertEquals(0, queue.size());

		// Terminate induction and check callback invoked
		manager.interrupt();
		verify(stop).run();
	}

	@Test
	public void startPrimaryInduction() {
		descriptor = new Induction.Descriptor.Builder().period(DURATION).flag(Flag.PRIMARY).build();
		manager.start(new Instance(descriptor, induction));
		assertEquals(false, manager.isActive());
		assertEquals(true, manager.isPrimary());
		assertEquals(1, queue.size());
	}

	@Test
	public void stopPrimaryInduction() {
		startPrimaryInduction();
		manager.stop();
		assertEquals(false, manager.isPrimary());
	}

	@Test
	public void stopInvalid() {
		assertThrows(IllegalStateException.class, () -> manager.stop());
	}
}
