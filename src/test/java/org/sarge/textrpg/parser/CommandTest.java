package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Inventory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.runner.ActionDescriptor;
import org.sarge.textrpg.runner.ActionDescriptor.ActionParameter;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class CommandTest {
	private Entity actor;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		final Inventory inv = new Inventory();
		actor = mock(Entity.class);
		when(actor.contents()).thenReturn(inv);
		obj = ObjectDescriptor.of("object").create();
	}

	/**
	 * Creates and verifies a command.
	 * @param param		Action parameter
	 * @param arg		Argument
	 * @throws ActionException if the command is not valid
	 */
	private void run(ActionParameter param, Object arg) throws ActionException {
		// Create action descriptor
		final AbstractAction instance = mock(AbstractAction.class);
		final ActionDescriptor action = mock(ActionDescriptor.class);
		when(action.action()).thenReturn(instance);
		when(action.parameters()).thenReturn(List.of(param));

		// Create and verify command
		final Command command = new Command(actor, action, Arrays.asList(arg), AbstractAction.Effort.NORMAL);
		command.verify();
	}

	@Test
	public void verify() throws ActionException {
		run(new ActionParameter(String.class, null), "arg");
	}

	@Test
	public void verifyCarried() throws ActionException {
		obj.parent(actor);
		run(new ActionParameter(WorldObject.class, mock(Carried.class)), obj);
	}

	@Test
	public void nullArgument() {
		assertThrows(NullPointerException.class, () -> run(new ActionParameter(String.class, null), null));
	}

	@Test
	public void invalidArgumentType() {
		assertThrows(IllegalArgumentException.class, () -> run(new ActionParameter(String.class, null), 42));
	}

	@Test
	public void notCarried() {
		TestHelper.expect("object.not.carried", () -> run(new ActionParameter(WorldObject.class, mock(Carried.class)), obj));
	}

	@Test
	public void autoCarried() {
		// TODO
		//TestHelper.expect("object.not.carried", () -> run(new ActionParameter(WorldObject.class, mock(Carried.class)), obj));
	}
}
