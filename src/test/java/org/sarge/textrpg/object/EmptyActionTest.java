package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class EmptyActionTest extends ActionTestBase {
	private EmptyAction action;
	private Container container;

	@BeforeEach
	public void before() {
		container = mock(Container.class);
		when(container.isOpen()).thenReturn(true);
		action = new EmptyAction();
	}

	@Test
	public void emptyReceptacle() throws ActionException {
		final Receptacle rec = mock(Receptacle.class);
		assertEquals(Response.OK, action.empty(rec));
		verify(rec).empty();
	}

	@Test
	public void emptyContainer() throws ActionException {
		// Add some contents
		final Contents contents = new Contents();
		when(container.contents()).thenReturn(contents);

		// Add an object
		final WorldObject obj = ObjectDescriptor.of("object").create();
		obj.parent(container);

		// Empty to current location
		final Response response = action.empty(actor, container);
		assertEquals(Response.OK, response);
		assertEquals(loc, obj.parent());
	}

	@Test
	public void emptyContainerClosed() {
		when(container.isOpen()).thenReturn(false);
		TestHelper.expect("empty.container.closed", () -> action.empty(actor, container));
	}

	@Test
	public void emptyContainerAlreadyEmpty() {
		when(container.contents()).thenReturn(mock(Contents.class));
		when(container.contents().isEmpty()).thenReturn(true);
		TestHelper.expect("empty.container.empty", () -> action.empty(actor, container));
	}

	@Test
	public void emptyContainerImmutable() {
		when(container.contents()).thenReturn(mock(Contents.class));
		when(container.contents().isRemoveAllowed()).thenReturn(false);
		TestHelper.expect("empty.container.immutable", () -> action.empty(actor, container));
	}
}
