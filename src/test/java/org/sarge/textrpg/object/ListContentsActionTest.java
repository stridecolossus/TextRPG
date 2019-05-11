package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class ListContentsActionTest extends ActionTestBase {
	private ListContentsAction action;
	private Parent parent;

	@BeforeEach
	public void before() {
		action = new ListContentsAction(new ArgumentFormatter.Registry());
		parent = mock(Parent.class);
		create(Contents.EnumerationPolicy.PERCEIVED);
	}

	private void create(Contents.EnumerationPolicy policy) {
		final Contents contents = new Contents() {
			@Override
			public EnumerationPolicy policy() {
				return policy;
			}
		};
		when(parent.contents()).thenReturn(contents);
		when(parent.name()).thenReturn("name");
	}

	@Test
	public void list() throws ActionException {
		// Add a perceived object
		final WorldObject obj = ObjectDescriptor.of("object").create();
		obj.parent(parent);
		when(actor.perceives(obj)).thenReturn(true);

		// Add a hidden object
		final WorldObject hidden = ObjectDescriptor.of("hidden").create();
		hidden.parent(parent);

		// Enumerate visible contents
		final Response response = action.contents(actor, parent);
		assertEquals(Response.of(obj.describe(null)), response);
	}

	@Test
	public void listEmpty() throws ActionException {
		final Response response = action.contents(actor, parent);
		final Description expected = new Description.Builder("list.contents.empty").name("name").add("prep", "contents.in").build();
		assertEquals(Response.of(expected), response);
	}

	@Test
	public void listInvalidContents() throws ActionException {
		create(Contents.EnumerationPolicy.NONE);
		TestHelper.expect("list.contents.invalid", () -> action.contents(actor, parent));
	}

	@Test
	public void listClosedContents() throws ActionException {
		create(Contents.EnumerationPolicy.CLOSED);
		TestHelper.expect("list.contents.closed", () -> action.contents(actor, parent));
	}
}
