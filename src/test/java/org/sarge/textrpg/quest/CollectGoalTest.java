package org.sarge.textrpg.quest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.quest.Goal.ActiveGoal;

public class CollectGoalTest {
	private Goal goal;
	private Object target;
	private Player player;
	private Goal.Listener listener;
	
	@Before
	public void before() {
		target = new Object();
		goal = new CollectGoal(target, 2, "goal");
		player = mock(Player.class);
		listener = mock(Goal.Listener.class);
	}
	
	@Test
	public void start() {
		// Start goal
		final ActiveGoal active = goal.start(player, listener);
		assertNotNull(active);
		
		// Check added listener to player
		final ArgumentCaptor<Player.Listener> captor = ArgumentCaptor.forClass(Player.Listener.class);
		verify(player).add(eq(target), captor.capture());
		
		// Check description
		final Description expected = new Description.Builder("goal").add("count", 0).add("total", 2).build();
		assertEquals(expected, active.describe());

		// Collect one
		captor.getValue().update(target);
		verify(listener).update(active, false);
		assertEquals("1", active.describe().get("count"));

		// Collect second and check completed
		captor.getValue().update(target);
		verify(listener).update(active, true);
		assertEquals("2", active.describe().get("count"));
	}
}
