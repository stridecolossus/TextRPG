package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.entity.Skill.Tier;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.DurableObject;
import org.sarge.textrpg.object.DurableObject.Descriptor;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

public class AbstractActionTest extends ActionTest {
	private AbstractAction action;
	private Skill skill;

	private class MockAction extends AbstractAction {
	    @Override
	    public boolean isValidStance(Stance stance) {
	        if(stance == Stance.MOUNTED) {
	            return false;
	        }
	        else {
	            return super.isValidStance(stance);
	        }
		}
	}

	@Before
	public void before() {
		action = new MockAction();
		skill = new Skill("skill", Collections.singletonList(new Tier(Condition.TRUE, 1)));
	}

	@Test
	public void constructor() {
		assertEquals("action.mock", action.getName());
	}

	@Test
	public void isValidStance() {
		assertEquals(true, action.isValidStance(Stance.DEFAULT));
		assertEquals(true, action.isValidStance(Stance.RESTING));
		assertEquals(true, action.isValidStance(Stance.SNEAKING));
		assertEquals(false, action.isValidStance(Stance.COMBAT));
		assertEquals(false, action.isValidStance(Stance.MOUNTED));
		assertEquals(false, action.isValidStance(Stance.SLEEPING));
	}

	@Test
	public void isCombatAction() {
		assertEquals(true, action.isCombatBlockedAction());
	}

	@Test
	public void isVisibleAction() {
		assertEquals(false, action.isVisibleAction());
	}

	@Test
	public void getSkill() throws ActionException {
		when(actor.getSkillLevel(skill)).thenReturn(Optional.of(42));
		assertEquals(42, action.getSkillLevel(actor, skill));
	}

	@Test
	public void getSkillNotFound() throws ActionException {
		when(actor.getSkillLevel(skill)).thenReturn(Optional.empty());
		expect("mock.requires.skill");
		action.getSkillLevel(actor, skill);
	}

	@Test
	public void find() throws ActionException {
		final WorldObject obj = new WorldObject(new ObjectDescriptor("object"));
		obj.setParent(actor);
		assertEquals(Optional.of(obj), action.find(actor, t -> true, false));
	}

	@Test
	public void findNotFound() throws ActionException {
		expect("mock.requires.object");
		action.find(actor, t -> true, true, "object");
	}

	@Test
	public void findBroken() throws ActionException {
		final WorldObject obj = new DurableObject(new Descriptor(new ObjectDescriptor("durable"), 1));
		obj.wear();
		obj.setParent(actor);
		expect("mock.broken.object");
		action.find(actor, t -> true, true, "object");
	}

	@Test
	public void calculateDuration() {
		final long base = 100;
		assertEquals(100, AbstractAction.calculateDuration(base, 1));
		assertEquals(80, AbstractAction.calculateDuration(base, 10));
		assertEquals(60, AbstractAction.calculateDuration(base, 20));
	}

	@Test
	public void response() {
		final ActionResponse res = action.response("arg");
		assertEquals(new ActionResponse(new Description("mock.response", "name", "arg")), res);
	}
}
