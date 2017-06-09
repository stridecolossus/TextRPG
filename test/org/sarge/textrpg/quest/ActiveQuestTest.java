package org.sarge.textrpg.quest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.quest.Goal.ActiveGoal;
import org.sarge.textrpg.quest.Quest.Stage;

public class ActiveQuestTest {
	private Stage one, two;
	private ActiveQuest quest;
	private Goal goal;
	private ActiveGoal active;
	private Player player;
	private ActiveQuest.Listener callback;
	
	@Before
	public void before() {
		// Mock collaborators
		player = mock(Player.class);
		callback = mock(ActiveQuest.Listener.class);
		
		// Define goals
		goal = mock(Goal.class);
		active = mock(ActiveGoal.class);
		when(active.describe()).thenReturn(new Description("active"));
		when(goal.start(eq(player), any())).thenReturn(active);
		
		// Define stages
		one = new Stage("one", Arrays.asList(goal, goal), Reward.NONE);
		two = new Stage("one", Arrays.asList(goal), Reward.NONE);
		
		// Create active quest
		final Quest q = new Quest("quest", Condition.TRUE, Arrays.asList(one, two));
		quest = new ActiveQuest(q, player, callback);
	}
	
	@Test
	public void constructor() {
		assertEquals(one, quest.getStage());
		assertArrayEquals(new ActiveGoal[]{active, active}, quest.getActiveGoals().toArray());
		verifyZeroInteractions(callback);
	}
	
	@Test
	public void updateGoal() {
		quest.update(active, false, player);
		assertEquals(one, quest.getStage());
		assertArrayEquals(new ActiveGoal[]{active, active}, quest.getActiveGoals().toArray());
		verifyZeroInteractions(callback);
	}
	
	@Test
	public void updateGoalCompleted() {
		quest.update(active, true, player);
		assertEquals(one, quest.getStage());
		assertArrayEquals(new ActiveGoal[]{active}, quest.getActiveGoals().toArray());
		verifyZeroInteractions(callback);
	}
	
	@Test
	public void updateStageCompleted() {
		quest.update(active, true, player);
		quest.update(active, true, player);
		assertEquals(two, quest.getStage());
		assertArrayEquals(new ActiveGoal[]{active}, quest.getActiveGoals().toArray());
		verifyZeroInteractions(callback);
	}
	
	@Test
	public void updateQuestCompleted() {
		quest.update(active, true, player);
		quest.update(active, true, player);
		quest.update(active, true, player);
		verify(callback).completed();
	}
}
