package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;

public class LightActionTest extends ActionTestBase {
	private LightAction action;
	private LightController controller;
	private Light light;

	@BeforeEach
	public void before() {
		light = new Light.Descriptor(ObjectDescriptor.of("light"), Light.Type.LANTERN, Duration.ofMinutes(42), Percentile.HALF, Percentile.HALF).create();
		controller = mock(LightController.class);
		action = new LightAction(controller);
	}

	@Test
	public void light() throws ActionException {
		action.apply(actor, LightAction.Operation.LIGHT, light);
		verify(controller).light(actor, light);
	}

	@Test
	public void snuff() throws ActionException {
		light.light(0L);
		action.apply(actor, LightAction.Operation.SNUFF, light);
		verify(controller).snuff(light);
	}

	@Test
	public void cover() throws ActionException {
		action.apply(actor, LightAction.Operation.COVER, light);
		assertEquals(true, light.isCovered());
	}

	@Test
	public void uncover() throws ActionException {
		light.cover();
		action.apply(actor, LightAction.Operation.UNCOVER, light);
		assertEquals(false, light.isCovered());
	}
}
