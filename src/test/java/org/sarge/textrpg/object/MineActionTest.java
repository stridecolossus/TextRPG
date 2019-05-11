package org.sarge.textrpg.object;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Area;

public class MineActionTest extends ActionTestBase {
	private MineAction action;
	private Node node;

	@BeforeEach
	public void before() {
		final Node.Descriptor descriptor = mock(Node.Descriptor.class);
		node = mock(Node.class);
		when(node.descriptor()).thenReturn(descriptor);
		when(node.descriptor().resource()).thenReturn(MineAction.LODE);
		action = new MineAction(skill);
	}

	@Test
	public void mine() throws ActionException {
		// Init mined resources
		final LootFactory factory = mock(LootFactory.class);
		final Area area = new Area.Builder("area").resource(MineAction.LODE, factory).build();
		when(loc.area()).thenReturn(area);
		when(factory.generate(actor)).thenReturn(Stream.of(ObjectDescriptor.of("metals").create()));

		// Start mining
		final Response response = action.mine(actor, node);

		// Complete induction
		final Response result = complete(response);
		verify(node).collect();
		verify(factory).generate(actor);

		// Check results
		// TODO
	}

	@Test
	public void mineAlreadyMined() throws ActionException {
		when(node.isCollected()).thenReturn(true);
		TestHelper.expect("mine.already.mined", () -> action.mine(actor, node));
	}
}
