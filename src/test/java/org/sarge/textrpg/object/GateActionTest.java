package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.Faction;

public class GateActionTest extends ActionTestBase {
	private GateAction action;
	private Gate gate;

	@BeforeEach
	public void before() {
		// Create gate
		gate = create(42);

		// Create object controller
		final ObjectController controller = mock(ObjectController.class);
		when(controller.reset(any(), any())).thenReturn(mock(Event.Reference.class));

		// Create action
		action = new GateAction(mock(MoneyArgumentParser.class), controller);
	}

	private Gate create(Integer bribe) {
		// Create gate
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("gate").reset(DURATION).build();
		final Faction faction = mock(Faction.class);
		final Gate.Keeper keeper = new Gate.Keeper("harry", new Faction.Association(faction, Relationship.FRIENDLY), bribe);
		final Gate gate = new Gate(new Gate.Descriptor(descriptor, keeper));

		// Init actor association
		when(actor.isAssociated(gate.descriptor().keeper().get().association())).thenReturn(true);

		// Add as link controller
		final Exit exit = Exit.of(Direction.EAST, gate.link(), loc);
		when(loc.exits()).thenReturn(ExitMap.of(exit));

		return gate;
	}

	@Test
	public void call() throws ActionException {
		final Response response = action.call(actor);
		assertEquals(Response.of("action.call.open"), response);
		assertEquals(true, gate.isOpen());
	}

	@Test
	public void callGateNotPresent() throws ActionException {
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		TestHelper.expect("gate.not.present", () -> action.call(actor));
	}

	@Test
	public void callClose() throws ActionException {
		gate.call();
		final Response response = action.call(actor);
		assertEquals(Response.of("action.call.close"), response);
		assertEquals(false, gate.isOpen());
	}

	@Test
	public void callFactionIgnored() throws ActionException {
		when(actor.isAssociated(gate.descriptor().keeper().get().association())).thenReturn(false);
		TestHelper.expect("call.faction.ignored", () -> action.call(actor));
	}

	@Test
	public void bribe() throws ActionException {
		actor.settings().set(PlayerSettings.Setting.CASH, 42);
		final Response response = action.bribe(actor, new Money(42));
		assertEquals(Response.of("gate.bribe.success"), response);
		assertEquals(0, actor.settings().toInteger(PlayerSettings.Setting.CASH));
		assertEquals(true, gate.isOpen());
	}

	@Test
	public void bribeCannotBribe() throws ActionException {
		gate = create(null);
		TestHelper.expect("gate.cannot.bribe", () -> action.bribe(actor, new Money(999)));
	}

	@Test
	public void bribeInsufficientBribe() throws ActionException {
		TestHelper.expect("gate.insufficient.bribe", () -> action.bribe(actor, new Money(1)));
	}

	@Test
	public void bribeInsufficientFunds() throws ActionException {
		TestHelper.expect("bribe.insufficient.money", () -> action.bribe(actor, new Money(42)));
	}
}
