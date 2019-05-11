package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.ResponseFormatter;
import org.sarge.textrpg.entity.EntityValueController;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.runner.PlayScreen.CommandProcessor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.DescriptionFormatter;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Location;

public class PlayScreenTest {
	private PlayScreen screen;
	private Session session;
	private PlayerCharacter player;
	private CommandProcessor proc;
	private EntityValueController update;
	private ResponseFormatter formatter;
	private NameStore store;

	@BeforeEach
	public void before() {
		// Create screen
		proc = mock(CommandProcessor.class);
		update = mock(EntityValueController.class);
		formatter = mock(ResponseFormatter.class);
		store = mock(NameStore.class);
		screen = new PlayScreen(proc, update, formatter, store);

		// Init player
		final Location loc = mock(Location.class);
		player = mock(PlayerCharacter.class);
		when(player.isAlive()).thenReturn(true);
		when(player.location()).thenReturn(loc);
		when(loc.area()).thenReturn(Area.ROOT);

		// Init session
		session = mock(Session.class);
		when(session.player()).thenReturn(player);
		when(session.store()).thenReturn(store);
	}

	@Test
	public void init() {
		screen.init(session);
		verify(formatter).format(player, null, Response.DISPLAY_LOCATION);
	}

	@Test
	public void handle() throws ActionException {
		when(proc.process("command", player, store)).thenReturn(Response.OK);
		assertEquals(screen, screen.handle(session, "command"));
		verify(formatter).format(player, store, Response.OK);
		verify(session).write(null);
	}

	@Test
	public void handleActionException() throws ActionException {
		final DescriptionFormatter delegate = mock(DescriptionFormatter.class);
		when(formatter.formatter()).thenReturn(delegate);
		when(proc.process("command", player, store)).thenThrow(ActionException.of("doh"));
		assertEquals(screen, screen.handle(session, "command"));
		verify(delegate).format(new Description("doh"), store);
		verify(session).write(null);
	}

	@Test
	public void handleException() throws ActionException {
		when(proc.process("command", player, store)).thenThrow(new RuntimeException("doh"));
		assertEquals(screen, screen.handle(session, "command"));
	}

	@Test
	public void handleKilled() throws ActionException {
		final Screen killed = mock(Screen.class);
		screen.setKilledScreen(killed);
		when(player.isAlive()).thenReturn(false);
		final Screen result = screen.handle(session, "command");
		assertEquals(killed, result);
	}
}
