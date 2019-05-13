package org.sarge.textrpg.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.Location;

public class ProximityMovementListenerTest extends ActionTestBase {
	private ProximityMovementListener listener;

	@BeforeEach
	public void before() {
		listener = new ProximityMovementListener(1);
	}

	@Test
	public void update() {
		// Add an entity with a glowing weapon
		final Entity entity = mock(Entity.class);
		final Location dest = mock(Location.class);
		final Contents contents = new Contents() {{			// TODO - horrible
			add(entity);
		}};
		when(dest.contents()).thenReturn(contents);
		listener.add(entity);

		// Link locations
		final Exit exit = Exit.of(Direction.EAST, dest);
		when(loc.exits()).thenReturn(ExitMap.of(exit));

		// Equip glowing weapon and check now receives alert
		final Weapon weapon = new Weapon.Descriptor.Builder().build().create();
		actor.contents().equipment().equip(weapon, Slot.MAIN);
		listener.update(actor, null, null);
		verify(entity).alert(new Description.Builder("proximity.alert").add("intensity", Percentile.ONE, ArgumentFormatter.PLAIN).build());
	}
}
