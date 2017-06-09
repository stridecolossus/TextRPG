package org.sarge.textrpg.quest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.quest.Goal.ActiveGoal;
import org.sarge.textrpg.world.Location;

public class VisitLocationGoalTest {
	private VisitLocationGoal goal;
	private Location loc;
	private Player player;
	private Goal.Listener listener;
	
	@Before
	public void before() {
		loc = mock(Location.class);
		goal = new VisitLocationGoal(Collections.singleton(loc));
		player = mock(Player.class);
		listener = mock(Goal.Listener.class);
	}
	
	@Test
	public void start() {
		// Start goal
		final ActiveGoal active = goal.start(player, listener);
		assertNotNull(active);
		
		// Check added location listener to player
		final ArgumentCaptor<Player.Listener> captor = ArgumentCaptor.forClass(Player.Listener.class);
		verify(player).add(eq(loc), captor.capture());
		
		// Check description
		final Description expected = new Description.Builder("goal.visit.location").add("count", 0).add("total", 1).build();
		assertEquals(expected, active.describe());

		// Visit location and check goal completed
		captor.getValue().update(loc);
		verify(listener).update(active, true);
	}
}
