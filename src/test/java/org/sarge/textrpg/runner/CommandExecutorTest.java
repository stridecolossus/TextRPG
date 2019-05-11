package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.AbstractAction.Flag;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.parser.Command;
import org.sarge.textrpg.runner.ActionDescriptor.ActionParameter;
import org.sarge.textrpg.runner.ActionDescriptor.RequiredDescriptor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.MutableIntegerMap.MutableEntry;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.LightLevelProvider;

public class CommandExecutorTest extends ActionTestBase {
	private CommandExecutor executor;
	private ActionDescriptor action;
	private Command command;
	private LightLevelProvider light;

	@BeforeEach
	public void before() {
		final SkillAction instance = mock(SkillAction.class);
		action = mock(ActionDescriptor.class);
		when(action.action()).thenReturn(instance);
		command = new Command(actor, action, List.of(), AbstractAction.Effort.NORMAL);
		executor = new CommandExecutor();
		light = mock(LightLevelProvider.class);
	}

	/**
	 * Adds a required skill to the action.
	 * @param mandatory		Skill is mandatory
	 * @param power			Requires power
	 * @param add			Adds to actor
	 */
	private void addRequiredSkill(boolean mandatory, boolean power, boolean add) {
		// Create skill
		final Skill.Builder builder = new Skill.Builder().name("skill");
		if(power) {
			builder.power(42);
		}
		if(!mandatory) {
			builder.defaultScore(Percentile.HALF);
		}
		final Skill skill = builder.build();

		// Add to action
		when(action.action().power(actor)).thenReturn(power ? 42 : 0);

		// Add to actor
		if(add) {
			actor.player().skills().add(skill);
		}
	}

	/**
	 * Adds a required object to the action.
	 */
	private void addRequiredObject(boolean inject) {
		final RequiredDescriptor descriptor = new RequiredDescriptor("cat", inject);
		when(action.required()).thenReturn(List.of(descriptor));
	}

	/**
	 * Adds an object to the actors inventory.
	 */
	private WorldObject addObject() {
		final WorldObject obj = new ObjectDescriptor.Builder("object").category("cat").build().create();
		obj.parent(actor);
		return obj;
	}

	@Test
	public void execute() throws Exception {
		executor.execute(command, light);
		verify(action).invoke(List.of());
	}

	@Test
	public void executeArguments() throws Exception {
		final Object arg = new Object();
		when(action.parameters()).thenReturn(List.of(new ActionParameter(Object.class, null)));
		command = new Command(actor, action, List.of(arg), AbstractAction.Effort.NORMAL);
		executor.execute(command, light);
		verify(action).invoke(List.of(arg));
	}

	@Test
	public void executeRevealsActor() throws Exception {
		when(action.action().isFlag(AbstractAction.Flag.REVEALS)).thenReturn(true);
		when(actor.model().stance()).thenReturn(Stance.SNEAKING);
		actor.model().values().visibility().stance(Percentile.HALF);
		executor.execute(command, light);
		verify(actor.model()).stance(Stance.DEFAULT);
		assertEquals(Percentile.ONE, actor.model().values().visibility().get());
	}

	@Test
	public void executeRequiresActor() throws Exception {
		when(action.isActorRequired()).thenReturn(true);
		executor.execute(command, light);
		verify(action).invoke(List.of(actor));
	}

	@Test
	public void executeEffortAction() throws Exception {
		when(action.isEffortAction()).thenReturn(true);
		executor.execute(command, light);
		verify(action).invoke(List.of(AbstractAction.Effort.NORMAL));
	}

	@Test
	public void executeRequiredSkill() throws ActionException {
		addRequiredSkill(true, false, true);
		executor.execute(command, light);
	}

	@Test
	public void executeRequiresLight() throws ActionException {
		when(action.action().isFlag(Flag.LIGHT)).thenReturn(true);
		TestHelper.expect("action.invalid.light", () -> executor.execute(command, light));
	}

	@Test
	public void executeOptionalSkill() throws ActionException {
		addRequiredSkill(false, false, false);
		executor.execute(command, light);
	}

	@Test
	public void executePowerTransaction() throws ActionException {
		final MutableEntry power = actor.model().values().get(EntityValue.POWER.key());
		power.set(42);
		addRequiredSkill(true, true, true);
		executor.execute(command, light);
		assertEquals(0, power.get());
	}

	@Test
	public void executeInsufficientPower() {
		addRequiredSkill(true, true, true);
		TestHelper.expect("action.insufficient.power", () -> executor.execute(command, light));
	}

	@Test
	public void executeRequiredObject() throws Exception {
		addObject();
		addRequiredObject(false);
		executor.execute(command, light);
		verify(action).invoke(List.of());
	}

	@Test
	public void executeRequiredObjectInjected() throws Exception {
		final WorldObject obj = addObject();
		addRequiredObject(true);
		executor.execute(command, light);
		verify(action).invoke(List.of(obj));
	}

	@Test
	public void executeMissingRequiredObject() {
		addRequiredObject(false);
		TestHelper.expect("action.required.cat", () -> executor.execute(command, light));
	}

	@Test
	public void executeRequiredObjectBroken() {
		final WorldObject obj = new WorldObject(new ObjectDescriptor.Builder("object").category("cat").build()) {
			@Override
			public boolean isBroken() {
				return true;
			}
		};
		obj.parent(actor);
		addRequiredObject(false);
		TestHelper.expect("action.required.broken", () -> executor.execute(command, light));
	}

	@Test
	public void executeVerifyFailed() throws ActionException {
		final AbstractAction instance = action.action();
		doThrow(ActionException.of("doh")).when(instance).verify(actor);
		TestHelper.expect("doh", () -> executor.execute(command, light));
	}

	@Test
	public void executeThrowsActionException() throws Exception {
		final InvocationTargetException e = new InvocationTargetException(ActionException.of("doh"));
		when(action.invoke(List.of())).thenThrow(e);
		TestHelper.expect("doh", () -> executor.execute(command, light));
	}
}
