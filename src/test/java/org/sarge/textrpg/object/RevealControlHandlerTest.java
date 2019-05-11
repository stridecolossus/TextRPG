package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.object.Control.Handler;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class RevealControlHandlerTest {
	private Actor actor;
	private Control control;

	@BeforeEach
	public void before() {
		actor = mock(Actor.class);

//		final TransientModel hidden = new TransientModel(TestHelper.queue());
//		when(actor.hidden()).thenReturn(hidden);

		control = mock(Control.class);
		when(control.parent()).thenReturn(TestHelper.parent());
	}

	@Test
	public void fixture() {
		// Create fixture reveal-handler
		final WorldObject fixture = new ObjectDescriptor.Builder("fixture").reset(Duration.ofMinutes(1)).fixture().build().create();
		final Handler handler = RevealControlHandler.fixture(fixture);
		assertNotNull(handler);

		// Reveal fixture
		final Description response = handler.handle(actor, control, true);
		assertEquals(control.parent(), fixture.parent());

//		// Check added to known hidden objects
//		assertEquals(true, actor.hidden().contains(fixture));

		// Check response
		assertEquals(new Description("revealed.object", "fixture"), response);

		// Hide fixture
		handler.handle(actor, control, false);
	}

	@Test
	public void invalidFixture() {
		assertThrows(IllegalArgumentException.class, () -> RevealControlHandler.fixture(ObjectDescriptor.of("invalid").create()));
	}

	@Test
	public void factory() {
		// Create generated reveal-handler
		final ObjectDescriptor descriptor = ObjectDescriptor.of("object");
		final Handler handler = RevealControlHandler.factory(descriptor);
		assertNotNull(handler);

		// Reveal fixture
		final Description response = handler.handle(actor, control, true);
		assertEquals(1, control.parent().contents().size());

		// Check response
		assertEquals(new Description("revealed.object", "object"), response);

		// Check generated object
		final WorldObject obj = (WorldObject) control.parent().contents().stream().iterator().next();
		assertEquals(descriptor, obj.descriptor());

		// Destroy object
		handler.handle(actor, control, false);
		assertEquals(false, obj.isAlive());
	}

	@Test
	public void invalidGeneratedObject() {
		assertThrows(IllegalArgumentException.class, () -> RevealControlHandler.factory(ObjectDescriptor.fixture("invalid")));
	}
}
