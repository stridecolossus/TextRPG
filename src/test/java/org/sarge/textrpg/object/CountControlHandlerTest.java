package org.sarge.textrpg.object;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CountControlHandlerTest {
	private CountControlHandler handler;
	private Control.Handler delegate;

	@BeforeEach
	public void before() {
		delegate = mock(Control.Handler.class);
		handler = new CountControlHandler(delegate, 2);
	}

	@Test
	public void activate() {
		handler.handle(null, null, true);
		verifyZeroInteractions(delegate);
	}

	@Test
	public void activateCompleted() {
		handler.handle(null, null, true);
		handler.handle(null, null, true);
		verify(delegate).handle(null, null, true);
	}

	@Test
	public void deactivate() {
		handler.handle(null, null, true);
		handler.handle(null, null, false);
		verifyZeroInteractions(delegate);
	}

	@Test
	public void deactivateWasCompleted() {
		handler.handle(null, null, true);
		handler.handle(null, null, true);
		handler.handle(null, null, false);
		verify(delegate).handle(null, null, true);
		verify(delegate).handle(null, null, false);
	}
}
