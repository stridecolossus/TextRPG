package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.object.Control.ControlDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.object.WorldObject.Interaction;

public class ControlTest extends ActionTest {
	private Control control;
	private Script open, close;

	@Before
	public void before() {
		open = mock(Script.class);
		close = mock(Script.class);
		control = new Control(new ControlDescriptor(new Builder("control").reset(42).build(), Interaction.PUSH, open, close));
	}

	@After
	public void after() {
		Control.QUEUE.reset();
	}

	@Test
	public void constructor() {
		assertEquals(false, control.isPushed());
		assertEquals(true, control.isFixture());
	}

	@Test
	public void describe() {
		final Description desc = control.describe();
		assertEquals("{control}", desc.get("name"));
		assertEquals("false", desc.get("pushed"));
	}

	@Test
	public void open() throws ActionException {
		control.apply(Interaction.PUSH, actor);
		assertEquals(true, control.isPushed());
		verify(open).execute(actor);
	}

	@Test
	public void close() throws ActionException {
		control.apply(Interaction.PUSH, actor);
		control.apply(Interaction.PULL, actor);
		assertEquals(false, control.isPushed());
		verify(open).execute(actor);
		verify(close).execute(actor);
	}

	@Test
	public void reset() throws ActionException {
		// Open control
		control.apply(Interaction.PUSH, actor);

		// Check event registered
		assertEquals(1, Control.QUEUE.size());

		// Update clock and check control is reset
		Control.QUEUE.execute(42L);
		assertEquals(false, control.isPushed());
		verify(close).execute(actor);
	}

	@Test
	public void invalidOpenOperation() throws ActionException {
		expect("action.invalid.interaction");
		control.apply(Interaction.PULL, actor);
	}

	@Test
	public void invalidCloseOperation() throws ActionException {
		control.apply(Interaction.PUSH, actor);
		expect("action.invalid.interaction");
		control.apply(Interaction.PUSH, actor);
	}
}
