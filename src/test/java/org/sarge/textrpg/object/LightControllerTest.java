package org.sarge.textrpg.object;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.EmissionController;

public class LightControllerTest extends ActionTestBase {
	private LightController controller;
	private ObjectController decay;
	private EmissionController broadcaster;
	private Light light;

	@BeforeEach
	public void before() {
		// Create controller
		decay = mock(ObjectController.class);
		broadcaster = mock(EmissionController.class);
		controller = new LightController(new Event.Queue.Manager(), decay, broadcaster);

		// Create light
		final Event.Holder holder = mock(Event.Holder.class);
		light = mock(Light.class);
		when(light.lifetime()).thenReturn(42L);
		when(light.expiry()).thenReturn(holder);
		when(light.warning()).thenReturn(holder);

		// Add light emissions
		when(light.emission(Emission.LIGHT)).thenReturn(Percentile.HALF);
		when(light.emission(Emission.SMOKE)).thenReturn(Percentile.ZERO);
	}

	@Test
	public void lightRequiresSource() throws ActionException {
		TestHelper.expect("light.requires.source", () -> controller.light(actor, light));
	}

	@Test
	public void lightFromTinderbox() throws ActionException {
		final WorldObject tinderbox = new ObjectDescriptor.Builder("tinderbox").category(Light.TINDERBOX).build().create();
		tinderbox.parent(actor);
		controller.light(actor, light);
		verify(light).light(0);
		verify(broadcaster).broadcast(actor, Set.of(new EmissionNotification(Emission.LIGHT, Percentile.HALF)));
	}

	@Test
	public void lightFromCampFire() throws ActionException {
		final Light camp = new Light.Descriptor(ObjectDescriptor.of("campfire"), Light.Type.CAMPFIRE, Duration.ofHours(1), Percentile.ONE, Percentile.ZERO).create();
		camp.parent(loc);
		camp.light(0);
		controller.light(actor, light);
		verify(light).light(0);
	}

	@Test
	public void snuff() throws ActionException {
		controller.snuff(light);
		verify(light).snuff(0);
		verify(decay).decay(light);
	}

	@Test
	public void register() {
		final Event.Holder holder = mock(Event.Holder.class);
		when(light.expiry()).thenReturn(holder);
		when(light.warning()).thenReturn(holder);
		when(light.lifetime()).thenReturn(42L);
		controller.register(light);
		verify(holder, times(2)).set(any(Event.Reference.class));
	}
}
