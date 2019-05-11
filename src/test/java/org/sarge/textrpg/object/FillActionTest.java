package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class FillActionTest extends ActionTestBase {
	private FillAction action;
	private ReceptacleController controller;
	private Receptacle water, well, oil;
	private Light lantern;
	private Utensil utensil;

	@BeforeEach
	public void before() {
		water = new Receptacle.Descriptor(ObjectDescriptor.of("rec"), Liquid.WATER, 2).create();
		well = new Receptacle.Descriptor(ObjectDescriptor.of("rec"), Liquid.WATER, 2).create();
		oil = new Receptacle.Descriptor(ObjectDescriptor.of("oil"), Liquid.OIL, 3).create();
		lantern = mock(Light.class);
		utensil = mock(Utensil.class);
		controller = mock(ReceptacleController.class);
		action = new FillAction(controller);
	}

	@Test
	public void fillWaterReceptacle() throws ActionException {
		water.consume(1);
		when(controller.findWater(actor)).thenReturn(Optional.of(well));
		assertEquals(Response.OK, action.fill(actor, water));
	}

	@Test
	public void fillReceptacle() throws ActionException {
		water.consume(1);
		assertEquals(Response.OK, action.fill(water, well));
		assertEquals(2, water.level());
		assertEquals(1, well.level());
	}

	@Test
	public void fillWaterReceptacleNotFound() throws ActionException {
		TestHelper.expect("fill.requires.water", () -> action.fill(actor, water));
	}

	@Test
	public void fillLantern() throws ActionException {
		assertEquals(Response.OK, action.fill(lantern, oil));
		verify(lantern).fill(oil);
	}

	@Test
	public void fillUtensil() throws ActionException {
		assertEquals(Response.OK, action.fill(utensil, water));
		verify(utensil).water(true);
		assertEquals(1, water.level());
	}

	@Test
	public void fillUtensilAlreadyFilled() throws ActionException {
		when(utensil.isWater()).thenReturn(true);
		TestHelper.expect("fill.utensil.already", () -> action.fill(utensil, water));
	}

	@Test
	public void fillUtensilEmptyReceptacle() throws ActionException {
		water.empty();
		TestHelper.expect("receptacle.source.empty", () -> action.fill(utensil, water));
	}

	@Test
	public void fillUtensilInvalidLiquid() throws ActionException {
		TestHelper.expect("fill.utensil.invalid", () -> action.fill(utensil, oil));
	}
}
