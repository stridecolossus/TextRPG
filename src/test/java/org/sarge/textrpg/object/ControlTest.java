package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class ControlTest {
	private Control control;
	private Control.Handler handler;
	private Actor actor;

	@BeforeEach
	public void before() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("control").fixture().reset(Duration.ofMinutes(1)).build();
		handler = mock(Control.Handler.class);
		control = new Control(descriptor, Interaction.PUSH, handler, Control.Policy.TOGGLE);
		actor = mock(Actor.class);
	}

	@Test
	public void constructor() {
		assertEquals("control", control.name());
		assertEquals(true, control.descriptor().isFixture());
		verifyZeroInteractions(handler);
	}

	@Test
	public void constructorInvalidExamineToggle() {
		assertThrows(IllegalArgumentException.class, () -> new Control(control.descriptor(), Interaction.EXAMINE, handler, Control.Policy.TOGGLE));
	}

	@Test
	public void constructorNotFixture() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("control").reset(Duration.ofMinutes(1)).build();
		assertThrows(IllegalArgumentException.class, () -> new Control(descriptor, Interaction.PUSH, handler, Control.Policy.DEFAULT));
	}

	@Test
	public void constructorNotResetable() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("control").fixture().build();
		assertThrows(IllegalArgumentException.class, () -> new Control(descriptor, Interaction.PUSH, handler, Control.Policy.DEFAULT));
	}

	@Test
	public void interact() throws ActionException {
		control.interact(actor, Interaction.PUSH);
		verify(handler).handle(actor, control, true);
	}

	@Test
	public void interactToggle() throws ActionException {
		control.interact(actor, Interaction.PUSH);
		control.interact(actor, Interaction.PULL);
		verify(handler).handle(actor, control, true);
		verify(handler).handle(actor, control, false);
	}

	@Test
	public void interactAlready() throws ActionException {
		control.interact(actor, Interaction.PUSH);
		TestHelper.expect("control.already.push", () -> control.interact(actor, Interaction.PUSH));
	}

	@Test
	public void interactActivatedIgnored() throws ActionException {
		control.interact(actor, Interaction.PUSH);
		TestHelper.expect("control.interaction.none", () -> control.interact(actor, Interaction.MOVE));
	}

	@Test
	public void interactIgnoredInteraction() throws ActionException {
		TestHelper.expect("control.interaction.none", () -> control.interact(actor, Interaction.MOVE));
	}

	@Test
	public void interactReset() throws ActionException {
		control.interact(actor, Interaction.PUSH);
		control.reset();
		verify(handler).handle(actor, control, true);
		verify(handler).handle(null, control, false);
	}

	@Test
	public void interactHidesControl() throws ActionException {
		control = new Control(control.descriptor(), Interaction.PUSH, handler, Control.Policy.HIDES);
		control.parent(TestHelper.parent());
		control.interact(actor, Interaction.PUSH);
		assertEquals(false, control.isAlive());
	}
}
