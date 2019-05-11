package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.EffortAction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.runner.ActionDescriptor.RequiredDescriptor;

public class ActionDescriptorProcessorTest {
	@Test
	@SuppressWarnings("unused")
	public void build() throws Exception {
		// Create action
		final AbstractAction action = new AbstractAction() {
			@RequiresActor
			@EffortAction
			@RequiredObject("one")
			@ActionOrder(42)
			public Response method(Entity actor, WorldObject arg, @RequiredObject("two") WorldObject obj, AbstractAction.Effort effort) {
				return null;
			}

			private Response privateMethod() {
				return null;
			}
		};

		// Build descriptors
		final ActionDescriptorProcessor proc = new ActionDescriptorProcessor(action);
		final var descriptors = proc.build();
		assertNotNull(descriptors);

		// Check generated descriptor
		final Method method = action.getClass().getMethod("method", new Class<?>[]{Entity.class, WorldObject.class, WorldObject.class, AbstractAction.Effort.class});
		final ActionDescriptor expected = new ActionDescriptor.Builder(action, method)
			.add(new RequiredDescriptor("one", false))
			.add(new RequiredDescriptor("two", true))
			.requiresActorArgument()
			.requiresEffortArgument()
			.order(42)
			.build();
		assertArrayEquals(new ActionDescriptor[]{expected}, descriptors.toArray());
	}
}
