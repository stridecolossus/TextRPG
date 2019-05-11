package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.entity.Entity.LocationTrigger;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.AmbientEvent;

public class AmbientLocationTriggerTest extends ActionTestBase {
	private LocationTrigger trigger;

	@BeforeEach
	public void before() {
		trigger = new AmbientLocationTrigger(new AmbientEvent("name", Duration.ofMinutes(1), true));
	}

	@Test
	public void trigger() {
		// Register ambient event
		trigger.trigger(actor);
		assertEquals(1, actor.manager().queue().size());

		// Execute event
		actor.manager().queue().manager().advance(Duration.ofMinutes(1).toMillis());
		verify(actor).alert(Description.of("name"));
	}
}
