package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.DefaultLocation;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.Location;

public class EntityTest {
	private Entity entity;
	private Race race;
	private EntityDescriptor descriptor;
	private EntityManager manager;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		// Create descriptor
		race = new Race.Builder("race").alignment(Alignment.EVIL).weight(42).size(Size.MEDIUM).skill(Skill.NONE).category("cat").build();
		descriptor = new DefaultEntityDescriptor(race, null);

		// Create manager
		final Event.Queue queue = mock(Event.Queue.class);
		final Notification.Handler handler = mock(Notification.Handler.class);
		manager = new EntityManager(queue, handler, mock(Consumer.class));

		// Create entity
		entity = new Entity(descriptor, manager);
	}

	@Nested
	class EntityTests {
		@Test
		public void constructor() {
			// Check basic properties
			assertEquals("race", entity.name());
			assertEquals(descriptor, entity.descriptor());
			assertEquals(Size.MEDIUM, entity.size());
			assertEquals(42, entity.weight());
			assertEquals(Percentile.ONE, entity.visibility());
			assertEquals(Percentile.ZERO, entity.emission(Emission.LIGHT));
			assertNotNull(entity.contents());
			assertEquals(false, entity.isAlive());
			assertEquals(true, entity.isSentient());

			// Check entity properties
			assertNotNull(entity.skills());
			assertEquals(true, entity.skills().contains(Skill.NONE));
			assertEquals(null, entity.location());
			assertEquals(false, entity.isPlayer());
			assertEquals(true, entity.isRaceCategory("cat"));
			assertEquals(false, entity.isAssociated(null));

			// Check generated components
			assertNotNull(entity.model());
			assertNotNull(entity.manager());
			assertNotNull(entity.movement());
		}

		@Test
		public void follower() {
			assertThrows(UnsupportedOperationException.class, () -> entity.follower());
			assertThrows(UnsupportedOperationException.class, () -> entity.leader());
		}

		@Test
		public void location() {
			final Location loc = new DefaultLocation(new Location.Descriptor("loc"), Area.ROOT);
			entity.parent(loc);
			assertEquals(loc, entity.location());
		}

		@Test
		public void alert() {
			final Description alert = new Description("notification");
			entity.alert(alert);
		}

		@Test
		public void damage() {
			final var values = entity.model().values();
			values.get(EntityValue.HEALTH.key()).set(3);
			entity.damage(Damage.Type.CRUSHING, 2);
			assertEquals(1, values.get(EntityValue.HEALTH.key()).get());
		}

		@Test
		public void describe() {
			final Description description = entity.describe(null);
			final Description expected = new Description.Builder("entity.description").name("race").add("stance", Stance.DEFAULT).build();
			assertEquals(expected, description);
		}

		@Test
		public void destroy() {
			final Event.Queue queue = manager.queue();
			entity.parent(TestHelper.parent());
			entity.destroy();
			verify(queue).remove();
		}
	}

	@Nested
	class Perception {
		private Hidden hidden;

		@BeforeEach
		public void before() {
			hidden = mock(Hidden.class);
		}

		@Test
		public void self() {
			assertEquals(false, entity.perceives(entity));
		}

		@Test
		public void invisible() {
			when(hidden.visibility()).thenReturn(Percentile.ZERO);
			assertEquals(false, entity.perceives(hidden));
		}

		@Test
		public void visible() {
			when(hidden.visibility()).thenReturn(Percentile.ONE);
			assertEquals(true, entity.perceives(hidden));
		}

		@Test
		public void group() {
			final Group group = mock(Group.class);
			when(group.perceives(entity, hidden)).thenReturn(true);
			entity.model().group(group);
			assertEquals(true, entity.perceives(hidden));
		}
	}

	@Nested
	class MovementModeTests {
		private MovementMode mode;

		@BeforeEach
		public void before() {
			mode = entity.movement();
			assertNotNull(mode);
		}

		@Test
		public void constructor() {
			assertEquals(entity, mode.mover());
			assertEquals(Percentile.ZERO, mode.noise());
			assertEquals(entity.model().trail(), mode.trail());
		}

		@Test
		public void transactions() {
			// TODO
		}

		@Test
		public void move() throws ActionException {
			final Location loc = new DefaultLocation(new Location.Descriptor("loc"), Area.ROOT);
			final Exit exit = new Exit(Direction.EAST, Link.DEFAULT, loc);
			mode.move(exit);
			assertEquals(loc, entity.parent());
		}
	}

	@Nested
	class ValidTarget {
		private Entity other;

		@BeforeEach
		public void before() {
			final EntityModel model = mock(EntityModel.class);
			final EntityDescriptor descriptor = mock(EntityDescriptor.class);
			other = mock(Entity.class);
			when(other.model()).thenReturn(model);
			when(other.descriptor()).thenReturn(descriptor);
		}

		@Test
		public void self() {
			assertEquals(false, entity.isValidTarget(entity));
		}

		@Test
		public void groupMember() {
			final Group group = mock(Group.class);
			entity.model().group(group);
			when(other.model().group()).thenReturn(group);
			assertEquals(false, entity.isValidTarget(other));
		}

		@Test
		public void notValidTargetAlignment() {
			when(other.descriptor().alignment()).thenReturn(Alignment.EVIL);
			assertEquals(false, entity.isValidTarget(other));
		}

		@Test
		public void isValidTarget() {
			when(other.descriptor().alignment()).thenReturn(Alignment.GOOD);
			assertEquals(true, entity.isValidTarget(other));
		}
	}
}
