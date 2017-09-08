package org.sarge.textrpg.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ToggleListener.Entry;

@SuppressWarnings("unused")
public class ToggleListenerTest {
	private Consumer<Boolean> toggle;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		toggle = mock(Consumer.class);
	}

	@Test
	public void toggle() {
		// Create toggle
		final Clock.Listener listener = new ToggleListener(toggle, Collections.singletonList(new Entry(1, 2)));

		// Update and check not toggled
        listener.update(0);
        listener.update(3);
		verifyZeroInteractions(toggle);

		// Toggle open
		listener.update(1);
		verify(toggle).accept(true);

		// Toggle close
		listener.update(2);
		verify(toggle).accept(false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidHour() {
		new ToggleListener(toggle, Collections.singletonList(new Entry(1, 24)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyHours() {
		new ToggleListener(toggle, Collections.emptyList());
	}

    @Test(expected = IllegalArgumentException.class)
    public void invalidOpenCloseTimes() {
        new ToggleListener(toggle, Collections.singletonList(new Entry(2, 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAscending() {
        new ToggleListener(toggle, Arrays.asList(new Entry(1, 2), new Entry(2, 3)));
    }
}
