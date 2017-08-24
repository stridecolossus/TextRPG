package org.sarge.textrpg.runner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.DescriptionStore;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

public class CommandParserTest {
	private class MockAction extends AbstractAction {
		public MockAction() {
			super("action");
		}
		
		@Override
		public ActionResponse execute(Entity actor) {
			return null;
		}

		@SuppressWarnings("unused")
		public void execute(Entity actor, WorldObject obj) {
			// Empty
		}

		@SuppressWarnings("unused")
		public void execute(Entity actor, String decoration) {
			// Empty
		}
	}
	
	@Rule
	public final ExpectedException expected = ExpectedException.none();
	
	private CommandParser parser;
	private DescriptionStore store;
	private Player player;
	private MockAction action;
	private WorldObject obj;
	
	@Before
	public void before() {
		// Create an action
		action = new MockAction();
		
		// Create word store
		store = mock(DescriptionStore.class);
		when(store.getStringArray("action.action")).thenReturn(new String[]{"action"});
		
		// Create parser
		parser = new CommandParser(Collections.singletonList(action), Collections.singleton("the"), store);
		
		// Create player
		player = mock(Player.class);
		when(player.getContents()).thenReturn(new Contents());
		
		// Put in a location
		final Location loc = mock(Location.class);
		when(loc.getContents()).thenReturn(new Contents());
		when(player.getLocation()).thenReturn(loc);
		
		// Create argument
		obj = new ObjectDescriptor("object").create();
		when(player.perceives(obj)).thenReturn(true);
	}
	
	@Test
	public void parse() throws Exception {
		final Command cmd = parser.parse(player, "action");
		assertEquals(action, cmd.getAction());
		assertEquals(cmd, new Command(action, MockAction.class.getMethod("execute", new Class<?>[]{Entity.class}), new Object[]{}));
	}

	@Test
	public void parseInventoryArgument() throws Exception {
		when(store.getStringArray("object")).thenReturn(new String[]{"object"});
		obj.setParent(player);
		final Command cmd = parser.parse(player, "action object");
		assertEquals(action, cmd.getAction());
		verify(cmd, WorldObject.class, obj);
	}

	@Test
	public void parseLocationArgument() throws Exception {
		when(store.getStringArray("object")).thenReturn(new String[]{"object"});
		obj.setParent(player.getLocation());
		final Command cmd = parser.parse(player, "action object");
		assertEquals(action, cmd.getAction());
		verify(cmd, WorldObject.class, obj);
	}

	@Test
	public void parseDecoration() throws Exception {
		final String decoration = "decoration";
		when(store.getStringArray(decoration)).thenReturn(new String[]{decoration});
		final Location loc = new Location("loc", Area.ROOT, Terrain.DESERT, false, Collections.singleton(decoration));
		when(player.getLocation()).thenReturn(loc);
		final Command cmd = parser.parse(player, "action decoration");
		verify(cmd, String.class, decoration);
	}
	
	@Test
	public void parseSurface() throws Exception {
		final String floor = "surface.floor";
		when(store.getStringArray(floor)).thenReturn(new String[]{"floor"});
		final Command cmd = parser.parse(player, "action floor");
		verify(cmd, String.class, floor);
	}
	
	private void verify(Command cmd, Class<?> clazz, Object arg) throws Exception {
		assertEquals(action, cmd.getAction());
		assertEquals(cmd, new Command(action, MockAction.class.getMethod("execute", new Class<?>[]{Entity.class, clazz}), new Object[]{arg}));
	}
	
	@Test
	public void parseUnknownArgument() throws ActionException {
		expected.expect(ActionException.class);
		expected.expectMessage("action.unknown.argument");
		parser.parse(player, "action cobblers");
	}

	@Test
	public void parseInvalidSyntax() throws ActionException {
		when(store.getStringArray("object")).thenReturn(new String[]{"object"});
		obj.setParent(player);
		expected.expect(ActionException.class);
		expected.expectMessage("parser.unknown.action");
		parser.parse(player, "action object object");
	}

	@Test
	public void parseEmptyCommand() throws ActionException {
		expected.expect(ActionException.class);
		expected.expectMessage("parser.empty.command");
		parser.parse(player, " ");
	}

	@Test
	public void parseStopWords() throws ActionException {
		expected.expect(ActionException.class);
		expected.expectMessage("parser.empty.command");
		parser.parse(player, "the");
	}

	@Test
	public void parseUnknownCommand() throws ActionException {
		expected.expect(ActionException.class);
		expected.expectMessage("parser.unknown.action");
		parser.parse(player, "cobblers");
	}
}
