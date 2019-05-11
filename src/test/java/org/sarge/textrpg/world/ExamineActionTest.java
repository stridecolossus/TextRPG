package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Control;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.ExamineAction.Decoration;

public class ExamineActionTest extends ActionTestBase {
	private ExamineAction action;
	private ArgumentFormatter.Registry formatters;

	@BeforeEach
	public void before() {
		formatters = new ArgumentFormatter.Registry();
		action = new ExamineAction(formatters);
	}

	@Test
	public void examineObject() {
		final WorldObject obj = ObjectDescriptor.of("object").create();
		assertEquals(Response.of(obj.describe(formatters)), action.examine(actor, obj));
	}

	@Test
	public void examineEntity() {
		final Entity entity = mock(Entity.class);
		final Description description = Description.of("entity");
		when(entity.describe(formatters)).thenReturn(description);
		assertEquals(Response.of(description), action.examine(entity));
	}

	@Test
	public void examineControl() throws ActionException {
		final Control control = mock(Control.class);
		when(control.describe(formatters)).thenReturn(Description.of("control"));
		assertEquals(Response.of(control.describe(formatters)), action.examine(actor, control));
	}

	@Test
	public void examineControlReveal() throws ActionException {
		final Control control = mock(Control.class);
		when(control.interaction()).thenReturn(Interaction.EXAMINE);
		when(control.describe(formatters)).thenReturn(Description.of("control"));
		when(control.interact(actor, Interaction.EXAMINE)).thenReturn(Description.of("revealed"));

		final Response expected = new Response.Builder()
			.add(Description.of("revealed"))
			.add(control.describe(formatters))
			.build();
		assertEquals(expected, action.examine(actor, control));
		action.examine(actor, control);
	}

	@Test
	public void examineDecoration() {
		assertEquals(Response.of("examine.decoration"), action.examine(mock(Decoration.class)));
	}
}
