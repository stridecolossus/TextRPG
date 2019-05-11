package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

public class OpenableControlHandlerTest {
	private Control.Handler handler;
	private Openable.Model model;

	@BeforeEach
	public void before() {
		model = mock(Openable.Model.class);
		handler = new OpenableControlHandler(() -> model, Openable.State.OPEN, "message");
	}

	@Test
	public void handle() {
		handle(true);
		handle(false);
	}

	private void handle(boolean open) {
		// Check response
		final Description response = handler.handle(null, null, open);
		assertEquals(Description.of(TextHelper.join("message", String.valueOf(open))), response);

		// Check model updated
		if(open) {
			verify(model).set(Openable.State.OPEN);
		}
		else {
			verify(model).reset();
		}
	}
}
