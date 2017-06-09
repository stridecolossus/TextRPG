package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Notification.Handler;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.Entity.AppliedEffect;
import org.sarge.textrpg.entity.Race.Builder;
import org.sarge.textrpg.entity.Skill.Tier;
import org.sarge.textrpg.object.DeploymentSlot;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Tracks;

public class EntityTest extends ActionTest {
	private Entity entity;
	private Race race;
	private Skill skill;
	private Induction induction;
	
	@Before
	public void before() {
		skill = new Skill("skill", Collections.singletonList(new Tier(Condition.TRUE, 1)));
		race = new Builder("race").size(Size.MEDIUM).skills(new SkillSet(skill, 6)).build();
		entity = new Entity(race, new MutableIntegerMap<Attribute>(Attribute.class), EntityManager.IDLE) {
			@Override
			public Handler getNotificationHandler() {
				return null;
			}

			@Override
			public Gender getGender() {
				return Gender.NEUTER;
			}

			@Override
			public Alignment getAlignment() {
				return null;
			}
			
			@Override
			protected String getDescriptionKey() {
				return "mock";
			}
		};
		entity.setParentAncestor(loc);
		induction = mock(Induction.class);
	}

	@After
	public void after() {
		entity.getEventQueue().reset();
	}

	@Test
	public void constructor() {
		// Check entity attributes
		assertEquals(race, entity.getRace());
		assertEquals("race", entity.getName());
		assertEquals(42, entity.getWeight());
		assertEquals(true, entity.isSentient());
		assertEquals(loc, entity.getLocation());
		
		// Check transient attributes
		assertEquals(Stance.DEFAULT, entity.getStance());
		assertEquals(Percentile.ONE, entity.getVisibility());
		assertEquals(Optional.empty(), entity.getGroup());
		assertEquals(true, entity.isDead());
		
		// Check stats
		assertNotNull(entity.getAttributes());
		assertNotNull(entity.getValues());
		
		// Check effects
		assertNotNull(entity.getAppliedEffects());
		assertEquals(0, entity.getAppliedEffects().count());

		// Check gear
		assertEquals(Optional.of(6), entity.getSkillLevel(skill));
		
		// Check followers
		assertNotNull(entity.getFollowers());
		assertEquals(0, entity.getFollowers().count());
	}
	
	@Test
	public void setStance() throws ActionException {
		// Reset
		entity.setStance(Stance.RESTING);
		assertEquals(Stance.RESTING, entity.getStance());

		// Sleep
		entity.setStance(Stance.SLEEPING);
		assertEquals(Stance.SLEEPING, entity.getStance());
		
		// Wake
		entity.setStance(Stance.RESTING);
		assertEquals(Stance.RESTING, entity.getStance());
		
		// Stand
		entity.setStance(Stance.DEFAULT);
		assertEquals(Stance.DEFAULT, entity.getStance());
		
		// Mount
		entity.setStance(Stance.MOUNTED);
		assertEquals(Stance.MOUNTED, entity.getStance());
		
		// Dismount
		entity.setStance(Stance.DEFAULT);
		assertEquals(Stance.DEFAULT, entity.getStance());
		
		// Sneak
		entity.setStance(Stance.SNEAKING);
		assertEquals(Stance.SNEAKING, entity.getStance());
		
		// Fight
		entity.setStance(Stance.COMBAT);
		assertEquals(Stance.COMBAT, entity.getStance());
	}
	
	@Test
	public void setGroup() {
		final Group group = mock(Group.class);
		entity.setGroup(group);
		assertEquals(group, group);
	}
	
	@Test
	public void perceives() {
		// Check full visible
		final Hidden obj = mock(Hidden.class);
		when(obj.getVisibility()).thenReturn(Percentile.ONE);
		assertEquals(true, entity.perceives(obj));
		
		// Check passive detection
		entity.modify(Attribute.PERCEPTION, 5);
		when(obj.getVisibility()).thenReturn(new Percentile(0.9f));
		assertEquals(true, entity.perceives(obj));

		// Check not visible
		when(obj.getVisibility()).thenReturn(new Percentile(0.8f));
		assertEquals(false, entity.perceives(obj));
		
		// Check completely invisible
		when(obj.getVisibility()).thenReturn(Percentile.ZERO);
		assertEquals(false, entity.perceives(obj));
	}
	
	@Test
	public void perceivesGroup() throws ActionException {
		// Create hidden object
		final Hidden obj = mock(Hidden.class);
		when(obj.getVisibility()).thenReturn(new Percentile(0.01f));
		assertEquals(false, entity.perceives(obj));

		// Create an entity that perceives the object
		final Entity other = mock(Entity.class);
		when(other.getGroup()).thenReturn(Optional.empty());
		when(other.perceives(obj)).thenReturn(true);
		
		// Add both to a group and check this entity can now perceive the object
		final Group group = new Group(other);
		group.add(entity);
		assertEquals(true, entity.perceives(obj));
	}
	
	@Test
	public void modifyAttribute() {
		entity.modify(Attribute.ENDURANCE, 1);
		entity.modify(Attribute.ENDURANCE, 2);
		assertEquals(1 + 2, entity.getAttributes().get(Attribute.ENDURANCE));
	}

	@Test
	public void modifyValue() {
		entity.modify(EntityValue.POWER, 42);
		assertEquals(42, entity.getValues().get(EntityValue.POWER));
	}
	
	@Test
	public void damage() {
		entity.modify(EntityValue.HEALTH, 3);
		entity.damage(DamageType.COLD, 1);
		assertEquals(3 - 1, entity.getValues().get(EntityValue.HEALTH));
	}
	
	@Test
	public void damageKilled() {
		entity.damage(DamageType.COLD, 999);
		assertEquals(true, entity.isDead());
	}
	
	@Test
	public void applyEffect() {
		// Apply effect
		final EffectMethod method = mock(EffectMethod.class);
		entity.apply(method, 1, Optional.of(2), entity.getEventQueue());
		verify(method).apply(entity, 1);
		
		// Check applied effect registered
		assertEquals(1, entity.getAppliedEffects().count());
		final AppliedEffect applied = entity.getAppliedEffects().iterator().next();
		assertEquals(method, applied.getEffect());
		assertEquals(1, applied.getSize());
		
		// Advance past duration and check expiry event
		entity.getEventQueue().update(2);
		verify(method).apply(entity, -1);
		assertEquals(0, entity.getAppliedEffects().count());
	}
	
	@Test
	public void dispel() {
		// Apply a transient effect
		final EffectMethod method = mock(EffectMethod.class);
		entity.apply(method, 1, Optional.of(2), actor.getEventQueue());
		
		// Dispel and check effect removed
		entity.dispel();
		verify(method).apply(entity, -1);
		assertEquals(0, entity.getAppliedEffects().count());
	}
	
	@Test
	public void equipmentEmission() throws ActionException {
		assertEquals(Optional.empty(), entity.getEmission(Emission.Type.LIGHT));
		final Emission light = Emission.light(Percentile.HALF);
		final WorldObject obj = new ObjectDescriptor.Builder("light").emission(light).slot(DeploymentSlot.HEAD).build().create();
		entity.getEquipment().equip(obj);
		assertEquals(Optional.of(light), entity.getEmission(Emission.Type.LIGHT));
	}
	
	@Test
	public void getWeapon() throws ActionException {
		// Equip a weapon
		final ObjectDescriptor desc = new ObjectDescriptor.Builder("weapon").slot(DeploymentSlot.MAIN_HAND).build();
		final WorldObject obj = new WorldObject(desc);
		entity.getEquipment().equip(obj);
		assertEquals(obj, entity.getWeapon());
		
		// Remove and check using default weapon
		entity.getEquipment().remove(obj);
		assertEquals(race.getEquipment().getWeapon(), entity.getWeapon());
	}
	
	@Test
	public void addTracks() {
		final Tracks t = new Tracks(entity.getName(), loc, Direction.EAST, Percentile.ONE, 1L);
		entity.add(t, 0);
		assertEquals(1, entity.getTracks().count());
		assertEquals(t, entity.getTracks().iterator().next());
	}

	@Test
	public void removeOldTracks() {
		// Add some tracks
		final Tracks older = new Tracks(entity.getName(), loc, Direction.EAST, Percentile.ONE, 0);
		entity.add(older, 0);

		// Add some more and check old tracks removed
		final Tracks tracks = new Tracks(entity.getName(), loc, Direction.EAST, Percentile.ONE, 2);
		entity.add(tracks, 1);
		assertEquals(1, entity.getTracks().count());
		assertEquals(tracks, entity.getTracks().iterator().next());
	}

	@Test
	public void setVisibility() {
		entity.setVisibility(Percentile.HALF);
		assertEquals(Percentile.HALF, entity.getVisibility());
	}
	
	@Test
	public void setStanceNotSneaking() throws ActionException {
		entity.setVisibility(Percentile.HALF);
		entity.setStance(Stance.RESTING);
		assertEquals(Percentile.ONE, entity.getVisibility());
	}
	
	@Test
	public void startInduction() {
		entity.start(ctx, induction, 42, false);
		assertEquals(induction, entity.getInduction());
		assertEquals(1, entity.getEventQueue().stream().count());
	}
	
	@Test
	public void completeInduction() throws ActionException {
		entity.start(ctx, induction, 42, false);
		entity.getEventQueue().update(42);
		verify(induction).complete();
		assertEquals(null, entity.getInduction());
	}
	
	@Test(expected = IllegalStateException.class)
	public void startInductionAlreadyActive() {
		entity.start(ctx, induction, 42, false);
		entity.start(ctx, induction, 42, false);
	}
	
	@Test
	public void interruptInduction() {
		entity.start(ctx, induction, 42, false);
		entity.interrupt();
		verify(induction).interrupt();
		assertEquals(null, entity.getInduction());
	}
	
	@Test
	public void repeatingInduction() throws ActionException {
		entity.start(ctx, induction, 1, true);
		entity.getEventQueue().update(1);
		entity.getEventQueue().update(2);
		assertEquals(induction, entity.getInduction());
		verify(induction, times(2)).complete();
	}
	
	@Test(expected = IllegalStateException.class)
	public void interruptInductionNotActive() {
		entity.interrupt();
	}
	
	@Test
	public void destroy() throws ActionException {
		entity.destroy();
		assertEquals(true, entity.isDead());
		assertEquals(0, loc.getContents().stream().filter(e -> e == entity).count());
	}
}
