package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.RevealNotification;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.entity.Player.Listener;
import org.sarge.textrpg.entity.Race.Builder;
import org.sarge.textrpg.runner.ConsoleRunner.Device;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Location;

public class PlayerTest {
	private Player player;
	private Notification received;
	private Listener listener;
	
	@Before
	public void before() {
		final Race race = new Builder("race").build();
		player = new Player("name", race, new MutableIntegerMap<>(Attribute.class), Gender.FEMALE, Alignment.EVIL, mock(Device.class)) {
			@Override
			protected void write(Notification n) {
				received = n;
			}
		};
		received = null;
		listener = mock(Listener.class);
	}
	
	@Test
	public void constructor() {
		assertEquals("name", player.getName());
		assertEquals(Gender.FEMALE, player.getGender());
		assertEquals(Alignment.EVIL, player.getAlignment());
	}
	
	@Test
	public void perceives() {
		// Create a hidden object
		final Hidden obj = mock(Hidden.class);
		when(obj.getVisibility()).thenReturn(Percentile.HALF);
		assertEquals(false, player.perceives(obj));
		
		// Register as known
		final RevealNotification reveal = new RevealNotification("message", obj);
		player.getNotificationHandler().handle(reveal);
		assertEquals(true, player.perceives(obj));
		assertEquals(reveal, received);
		assertEquals(1, player.getEventQueue().stream().count());
		
		// Forget it
		player.forget(obj);
		assertEquals(false, player.perceives(obj));
	}
	
	@Test
	public void listenerLocationVisited() throws ActionException {
		final Location loc = ActionTest.createLocation();
		player.add(loc, listener);
		player.setParent(loc);
		verify(listener).update(loc);
	}
	
	@Test
	public void listenerObjectCollected() throws ActionException {
		final Thing obj = mock(Thing.class);
		player.add(obj, listener);
		player.getContents().add(obj);
		verify(listener).update(obj);
	}

	@Test
	public void listenerEntityKilled() throws ActionException {
		// TODO
	}
}
