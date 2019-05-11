package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Response;

@Disabled
public class DiscoverControllerTest {
	private static final Duration DURATION = Duration.ofMinutes(1);

	private DiscoverController<Hidden> controller;
	private Consumer<Hidden> listener;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		controller = new DiscoverController<>("discover", Hidden::visibility, null); // TODO
		listener = mock(Consumer.class);
	}

	@Test
	public void inductionEmptyResults() throws ActionException {
		final var induction = controller.induction(DURATION, Stream.empty(), listener);
		assertNotNull(induction);
		final var response = induction.complete();
		assertEquals(Response.of("discover.none.found"), response);
	}

	@Test
	public void inductionInterrupt() throws ActionException {
		final var induction = controller.induction(DURATION, Stream.empty(), listener);
		induction.interrupt();
		verifyZeroInteractions(listener);
	}

	@Test
	public void induction() throws ActionException {
		// Create induction
		final Hidden found = () -> Percentile.ONE;
		final Hidden hidden = () -> Percentile.ZERO;
		final var induction = controller.induction(DURATION, Stream.of(found, hidden), listener);

		// Check discovered
// TODO
//		Event.Queue.advance(DURATION.toMillis());
		verify(listener).accept(found);
		verifyNoMoreInteractions(listener);

		// Check response
		final var expected = new Description.Builder("discover.success").add("count", 1).build();
		assertEquals(Response.of(expected), induction.complete());
	}
}
