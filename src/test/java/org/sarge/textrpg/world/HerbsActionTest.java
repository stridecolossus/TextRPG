package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.Node;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class HerbsActionTest extends ActionTestBase {
	private HerbsAction action;

	@BeforeEach
	public void before() {
		action = new HerbsAction(skill, DURATION);
	}

	/**
	 * Creates a node.
	 * @param res Resource type
	 * @return Node
	 */
	private Node create() {
		// Create node and add to location
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder(HerbsAction.HERBS).reset(DURATION).fixture().build();
		final Node node = new Node.Descriptor(descriptor, HerbsAction.HERBS).create();
		node.parent(loc);

		// Add resource factory to area
		final Area area = new Area.Builder("area").resource(HerbsAction.HERBS, TestHelper.loot()).build();
		when(loc.area()).thenReturn(area);

		return node;
	}

	@Test
	public void find() throws ActionException {
		// Add some herbs
		final Node node = create();

		// Start search
		final Response response = action.find(actor);

		// Complete induction
		final Response result = complete(response);
		assertEquals(Response.of(new Description("herbs.discovered", "herbs")), result);

		// Check herbs discovered
		verify(actor.hidden()).add(node, DURATION);
		assertEquals(false, node.isCollected());
		assertEquals(true, node.isAlive());
	}

	@Test
	public void findNotFound() throws ActionException {
		final Response result = complete(action.find(actor));
		assertEquals(Response.of("herbs.not.found"), result);
	}

	@Test
	public void cull() throws ActionException {
		// Add a known herbs node
		final Node node = create();
		when(actor.perceives(node)).thenReturn(true);

		// Cull herbs
		final Response response = action.cull(actor);
		// TODO - responses

		// Check node is culled
		assertEquals(true, node.isCollected());
		assertEquals(false, node.isAlive());
	}

	@Test
	public void cullNotKnown() throws ActionException {
		TestHelper.expect("cull.not.found", () -> action.cull(actor));
	}
}
