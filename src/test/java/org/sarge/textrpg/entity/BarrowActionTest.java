package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.entity.BarrowAction.Barrow;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class BarrowActionTest extends ActionTestBase {
	private BarrowAction action;

	@BeforeEach
	public void before() {
		action = new BarrowAction(DURATION, DURATION);
	}

	@Test
	public void bury() throws ActionException {
		// Create a corpse
		final Corpse corpse = mock(Corpse.class);
		when(corpse.isAlive()).thenReturn(true);
		when(corpse.name()).thenReturn("corpse");

		// Add some contents
		final Contents contents = new Contents();
		final WorldObject obj = ObjectDescriptor.of("object").create();
		when(corpse.contents()).thenReturn(contents);
		obj.parent(corpse);

		// Start barrow
		final Response response = action.barrow(actor, corpse);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete induction
		final Response result = complete(response);
		assertEquals(Response.of(new Description("action.barrow", "corpse")), result);
		verify(corpse).destroy();

		// Check barrow created
		final Barrow barrow = loc.contents().select(Barrow.class).iterator().next();
		assertEquals("barrow", barrow.name());
		assertEquals(loc, barrow.parent());
		assertEquals(true, barrow.descriptor().isFixture());

		// Check barrow contents
		assertEquals(false, barrow.isExcavated());
		assertEquals(Contents.EnumerationPolicy.NONE, barrow.contents().policy());
		assertEquals(1, barrow.contents().size());
		assertEquals(obj, barrow.contents().stream().iterator().next());
	}

	@Test
	public void buryBarrowPresent() throws ActionException {
		bury();
		TestHelper.expect("barrow.already.present", () -> action.barrow(actor, mock(Corpse.class)));
	}

	@Test
	public void excavate() throws ActionException {
		// Create a barrow
		bury();

		// Start barrow
		final Response response = action.excavate(actor);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete induction
		assertEquals(Response.OK, complete(response));

		// Check barrow excavated
		final Barrow barrow = loc.contents().select(Barrow.class).iterator().next();
		assertEquals(true, barrow.isExcavated());
		assertEquals(Contents.EnumerationPolicy.DEFAULT, barrow.contents().policy());
	}

	@Test
	public void digRequiresBarrow() throws ActionException {
		TestHelper.expect("excavate.requires.barrow", () -> action.excavate(actor));
	}

	@Test
	public void digBarrowExcavated() throws ActionException {
		excavate();
		TestHelper.expect("excavate.already.excavated", () -> action.excavate(actor));
	}
}
