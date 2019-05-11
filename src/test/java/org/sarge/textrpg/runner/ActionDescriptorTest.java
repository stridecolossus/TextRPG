package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.AbstractAction.Flag;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.EnumAction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.runner.ActionDescriptor.ActionParameter;
import org.sarge.textrpg.runner.ActionDescriptor.RequiredDescriptor;
import org.sarge.textrpg.world.Direction;

public class ActionDescriptorTest {
	@Test
	public void builder() throws Exception {
		// Create an action
		final AbstractAction action = new AbstractAction(Flag.REVEALS) {
			@SuppressWarnings("unused")
			public Response method(Entity actor, WorldObject injected, Entity arg, AbstractAction.Effort effort) {
				return null;
			}

			@Override
			public String prefix() {
				return "prefix";
			}
		};

		// Lookup method
		final Method method = action.getClass().getMethod("method", new Class<?>[]{Entity.class, WorldObject.class, Entity.class, AbstractAction.Effort.class});

		// Build descriptor
		final RequiredDescriptor required = new RequiredDescriptor("cat", true);
		final ActionDescriptor descriptor = new ActionDescriptor.Builder(action, method)
			.add(required)
			.requiresActorArgument()
			.requiresEffortArgument()
			.order(42)
			.build();

		// Check descriptor
		assertNotNull(descriptor);
		assertEquals("action.prefix.method", descriptor.name());
		assertEquals(List.of(new ActionParameter(Entity.class, null)), descriptor.parameters());
		assertEquals(Optional.empty(), descriptor.constant());
		assertEquals(List.of(required), descriptor.required());
		assertEquals(true, descriptor.isActorRequired());
		assertEquals(true, descriptor.isEffortAction());
		assertEquals(42, descriptor.order());
	}

	@Test
	public void empty() throws Exception {
		// Create an action with no parameters
		final AbstractAction action = new AbstractAction() {
			@SuppressWarnings("unused")
			public Response method() {
				return null;
			}
		};

		// Lookup method
		final Method method = action.getClass().getMethod("method");

		// Check descriptor
		final ActionDescriptor descriptor = new ActionDescriptor.Builder(action, method).build();
		assertEquals("action.method", descriptor.name());
		assertEquals(List.of(), descriptor.parameters());
		assertEquals(Optional.empty(), descriptor.constant());
		assertEquals(List.of(), descriptor.required());
		assertEquals(false, descriptor.isActorRequired());
		assertEquals(false, descriptor.isEffortAction());

		// Check invalid descriptors for this action
		assertThrows(IndexOutOfBoundsException.class, () -> new ActionDescriptor.Builder(action, method).requiresActorArgument().build());
		assertThrows(IndexOutOfBoundsException.class, () -> new ActionDescriptor.Builder(action, method).requiresEffortArgument().build());
		assertThrows(IndexOutOfBoundsException.class, () -> new ActionDescriptor.Builder(action, method).add(new RequiredDescriptor("arg", true)).build());
	}

	@Test
	public void enumerated() throws Exception {
		// Create an enumerated action
		@EnumAction(Direction.class)
		class Enumerated extends AbstractAction {
			@SuppressWarnings("unused")
			public Response method(Direction dir) {
				return null;
			}
		}
		final AbstractAction action = new Enumerated();

		// Lookup method
		final Method method = action.getClass().getMethod("method", new Class<?>[]{Direction.class});

		// Check descriptor
		final ActionDescriptor descriptor = new ActionDescriptor.Builder(action, method).enumeration(Direction.EAST).build();
		assertEquals("action.east", descriptor.name());
		assertEquals(List.of(), descriptor.parameters());
		assertEquals(Optional.of(Direction.EAST), descriptor.constant());
		assertEquals(List.of(), descriptor.required());
		assertEquals(false, descriptor.isActorRequired());
		assertEquals(false, descriptor.isEffortAction());
	}

	@Test
	public void invalidCarriedActionParameter() {
		assertThrows(IllegalArgumentException.class, () -> new ActionParameter(String.class, mock(Carried.class)));
	}
}
