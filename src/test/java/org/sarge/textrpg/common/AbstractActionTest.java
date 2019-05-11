package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.sarge.textrpg.common.AbstractAction.Flag;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.EntityManager;
import org.sarge.textrpg.entity.EntityModel;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.LightLevelProvider;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

public class AbstractActionTest {
	private AbstractAction action;
	private Entity actor;
	private LightLevelProvider light;

	@BeforeEach
	public void before() {
		action = new AbstractAction(Flag.LIGHT, Flag.OUTSIDE, Flag.INDUCTION) {
			// Implementation
		};

		// Create an actor
		final EntityModel model = mock(EntityModel.class);
		actor = mock(Entity.class);
		when(actor.model()).thenReturn(model);
		when(actor.model().stance()).thenReturn(Stance.DEFAULT);

		// Create manager
		final EntityManager manager = mock(EntityManager.class);
		when(actor.manager()).thenReturn(manager);
		when(actor.manager().induction()).thenReturn(mock(Induction.Manager.class));

		// Create actors location
		final Location loc = mock(Location.class);
		when(actor.parent()).thenReturn(loc);
		when(actor.location()).thenReturn(loc);
		when(loc.terrain()).thenReturn(Terrain.DESERT);

		// Create light-level provider
		light = mock(LightLevelProvider.class);
		when(light.isAvailable(loc)).thenReturn(true);
	}

	@Test
	public void constructor() {
		assertEquals(StringUtils.EMPTY, action.prefix());
		assertEquals(0, action.power(null));
	}

	@Test
	public void parser() {
		assertEquals(ArgumentParser.Registry.EMPTY, action.parsers(actor));
	}

	@ParameterizedTest
	@EnumSource(value=Flag.class, mode=EnumSource.Mode.EXCLUDE, names={"LIGHT", "REVEALS", "OUTSIDE", "BROADCAST"})
	public void isFlagSetOverride(Flag flag) {
		assertEquals(true, action.isFlag(flag));
	}

	@ParameterizedTest
	@EnumSource(value=Flag.class, names={"REVEALS", "OUTSIDE", "BROADCAST"})
	public void isFlagClearOverride(Flag flag) {
		assertEquals(false, action.isFlag(flag));
	}

	@Test
	public void response() {
		final Response response = AbstractAction.response("key", "arg");
		assertNotNull(response);
		assertEquals(false, response.isDisplayLocation());
		assertEquals(Optional.empty(), response.induction());
		assertEquals(1, response.responses().count());
		assertEquals(new Description("key", "arg"), response.responses().iterator().next());
	}

	@ParameterizedTest
	@EnumSource(value=Terrain.class, mode=EnumSource.Mode.EXCLUDE, names={"WATER"})
	public void isValidTerrain(Terrain terrain) {
		assertEquals(true, action.isValid(terrain));
	}

	@ParameterizedTest
	@EnumSource(value=Terrain.class, names={"WATER"})
	public void isValidTerrainNotValid(Terrain terrain) {
		assertEquals(false, action.isValid(terrain));
	}

	@ParameterizedTest
	@EnumSource(value=Stance.class, names={"DEFAULT", "SNEAKING", "HIDING"})
	public void isValidStance(Stance stance) {
		assertEquals(true, action.isValid(stance));
	}

	@EnumSource(value=Stance.class, mode=EnumSource.Mode.EXCLUDE, names={"DEFAULT", "SNEAKING", "HIDING"})
	public void isValidStanceNotValid(Stance stance) {
		assertEquals(false, action.isValid(stance));
	}

	@Test
	public void verify() throws ActionException {
		action.verify(actor);
	}

	@Test
	public void verifySleeping() throws ActionException {
		when(actor.model().stance()).thenReturn(Stance.SLEEPING);
		TestHelper.expect("action.invalid.sleeping", () -> action.verify(actor));
	}

	@Test
	public void verifyInvalidStance() throws ActionException {
		when(actor.model().stance()).thenReturn(Stance.MOUNTED);
		TestHelper.expect("action.invalid.mounted", () -> action.verify(actor));
	}

	@Test
	public void verifyInvalidTerrain() throws ActionException {
		when(actor.location().terrain()).thenReturn(Terrain.WATER);
		TestHelper.expect("action.invalid.terrain", () -> action.verify(actor));
	}

	@Test
	public void verifyParentBlocked() throws ActionException {
		final Parent parent = mock(Parent.class);
		when(parent.parent()).thenReturn(mock(Parent.class));
		when(parent.name()).thenReturn("parent");
		when(actor.parent()).thenReturn(parent);
		action.verify(actor);
	}

	@Test
	public void verifyPrimaryInductionBlocked() throws ActionException {
		when(actor.manager().induction().isPrimary()).thenReturn(true);
		TestHelper.expect("action.invalid.primary", () -> action.verify(actor));
	}

	@Test
	public void verifyInductionActive() throws ActionException {
		when(actor.manager().induction().isActive()).thenReturn(true);
		TestHelper.expect("action.invalid.induction", () -> action.verify(actor));
	}

	@Test
	public void isSuccess() {
		final Calculation mod = mock(Calculation.class);
		final Skill skill = new Skill.Builder().name("skill").score(Percentile.HALF).modifier(mod).build();
		final SkillSet skills = mock(SkillSet.class);
		when(actor.skills()).thenReturn(skills);
		when(skills.find(skill)).thenReturn(skill);
		assertEquals(false, action.isSuccess(actor, skill, Percentile.ONE));
		assertEquals(true, action.isSuccess(actor, skill, Percentile.ZERO));
		Mockito.verify(mod, times(2)).evaluate(actor.model());
	}
}
