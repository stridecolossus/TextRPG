package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.DefaultLocation;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.Terrain;
import org.sarge.textrpg.world.Trail;

public class MountMovementModeTest extends ActionTestBase {
	private MountMovementMode mode;
	private Mount mount;

	@BeforeEach
	public void before() {
		// Create mount race
		final Race race = new Race.Builder("mount").build();
		final EntityDescriptor descriptor = mock(EntityDescriptor.class);
		when(descriptor.race()).thenReturn(race);

		// Create mount
		mount = mock(Mount.class);
		when(mount.name()).thenReturn("mount");
		when(mount.movement()).thenReturn(mock(MovementMode.class));
		when(mount.movement().trail()).thenReturn(new Trail());
		when(mount.noise()).thenReturn(Percentile.HALF);
		when(mount.descriptor()).thenReturn(descriptor);

		// Init rider
		when(actor.emission(Emission.SOUND)).thenReturn(Percentile.ZERO);

		// Create movement mode
		mode = new MountMovementMode(actor, mount);
	}

	@Test
	public void constructor() {
		assertEquals(mount, mode.mover());
		assertEquals(Percentile.HALF, mode.noise());
		assertNotNull(mode.trail());
		assertEquals(Percentile.ONE, mode.tracks());
	}

	@Test
	public void move() throws ActionException {
		final var dest = mock(DefaultLocation.class);
		when(dest.terrain()).thenReturn(Terrain.DESERT);
		final var exit = new Exit(Direction.EAST, Link.DEFAULT, dest);
		mode.move(exit);
		verify(actor).parent(dest);
		verify(mount).parent(dest);
	}
}
