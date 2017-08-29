package org.sarge.textrpg.runner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

public class CommandTest extends ActionTest {
	private Command cmd;
	private Player player;
	private AbstractAction action;
	private WorldObject obj;
	private boolean arg, fail, light;

	@Before
	public void before() throws Exception {
		// Create action
		arg = false;
		fail = false;
		light = false;
		action = new AbstractAction() {
			@Override
			public boolean isLightRequiredAction() {
				return light;
			}

			@Override
			public boolean isVisibleAction() {
				return true;
			}

			@Override
			public boolean isValidStance(Stance stance) {
				if(stance == Stance.SNEAKING) {
					return true;
				}
				else {
					return super.isValidStance(stance);
				}
			}

			@Override
			public boolean isParentBlockedAction() {
				return true;
			}

			@SuppressWarnings("unused")
			public ActionResponse execute(Entity actor, WorldObject obj) throws ActionException {
				if(fail) throw new ActionException("doh");
				arg = true;
				return ActionResponse.OK;
			}
		};

		// Create player
		player = mock(Player.class);
		when(player.getLocation()).thenReturn(loc);
		when(player.getParent()).thenReturn(loc);
		when(player.getStance()).thenReturn(Stance.DEFAULT);

		// Create argument
		obj = mock(WorldObject.class);
		when(obj.getParent()).thenReturn(player);

		// Create command
		final Method method = action.getClass().getDeclaredMethod("execute", new Class<?>[]{Entity.class, WorldObject.class});
		cmd = new Command(action, method, new Object[]{obj});
	}

	@Test
	public void constructor() {
		assertEquals(action, cmd.getAction());
		assertEquals(obj, cmd.getPreviousObject());
	}

	@Test
	public void execute() throws Exception {
		final ActionResponse res = cmd.execute(player, true);
		assertEquals(ActionResponse.OK, res);
		assertEquals(true, arg);
	}

	@Test
	public void executeActionException() throws Exception {
		expect("doh");
		fail = true;
		final ActionResponse res = cmd.execute(player, true);
		assertEquals(null, res);
	}

	@Test
	public void executeCombatBlocked() throws ActionException {
		when(player.getStance()).thenReturn(Stance.COMBAT);
		expect("action.combat.blocked");
		cmd.execute(player, true);
	}

	@Test
	public void executeInvalidStance() throws ActionException {
		when(player.getStance()).thenReturn(Stance.SLEEPING);
		expect("action.invalid.sleeping");
		cmd.execute(player, true);
	}

	@Test
	public void executeRemoteArgument() throws ActionException {
		final Parent parent = mock(Parent.class);
		when(parent.getParentName()).thenReturn("parent");
		when(player.getParent()).thenReturn(parent);
		when(obj.getParent()).thenReturn(mock(Parent.class));
		expect("action.invalid.parent");
		cmd.execute(player, true);
	}

	@Test
	public void executeRequiresLight() throws ActionException {
		light = true;
		loc = new Location("underground", Area.ROOT, Terrain.UNDERGROUND, false, Collections.emptyList());
		when(player.getLocation()).thenReturn(loc);
		expect("action.requires.light");
		cmd.execute(player, false);
	}

	@Test
	public void executeArgumentDark() throws ActionException {
		when(obj.getParent()).thenReturn(loc);
		obj.setParent(loc);
		expect("action.unknown.argument");
		cmd.execute(player, false);
	}

	@Test
	public void executeSneaking() throws ActionException {
		when(player.getStance()).thenReturn(Stance.SNEAKING);
		cmd.execute(player, true);
		verify(player).setStance(Stance.DEFAULT);
	}
}
