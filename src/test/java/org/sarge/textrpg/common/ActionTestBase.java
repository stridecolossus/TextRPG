package org.sarge.textrpg.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.sarge.textrpg.common.SkillSet.MutableSkillSet;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.entity.*;
import org.sarge.textrpg.entity.Follower.FollowerModel;
import org.sarge.textrpg.entity.Leader.LeaderModel;
import org.sarge.textrpg.entity.PlayerCharacter.PlayerModel;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.EnumerationIntegerMap;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

/**
 * Base-class for an action test.
 * @author Sarge
 */
public abstract class ActionTestBase {
	protected static final Duration DURATION = Duration.ofMinutes(1);

	protected PlayerCharacter actor;
	protected Location loc;
	protected Skill skill;

	@BeforeEach
	public void beforeBase() {
		// Create location
		final Contents contents = new Contents();
		loc = mock(Location.class);
		when(loc.name()).thenReturn("loc");
		when(loc.area()).thenReturn(Area.ROOT);
		when(loc.contents()).thenReturn(contents);
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		when(loc.terrain()).thenReturn(Terrain.GRASSLAND);

		// Create actor
		actor = mock(PlayerCharacter.class);
		when(actor.name()).thenReturn("actor");
		when(actor.parent()).thenReturn(loc);
		when(actor.location()).thenReturn(loc);
		when(actor.size()).thenReturn(Size.MEDIUM);

		// Init descriptor
		final EntityDescriptor descriptor = mock(EntityDescriptor.class);
		when(actor.descriptor()).thenReturn(descriptor);

		// Init race
		final Race race = new Race.Builder("race").build();
		when(actor.descriptor().race()).thenReturn(race);

		// Init model
		final EntityModel model = mock(EntityModel.class);
		when(actor.model()).thenReturn(model);
		when(actor.model().stance()).thenReturn(Stance.DEFAULT);

		// Init manager
		final EntityManager manager = mock(EntityManager.class);
		final Event.Queue queue = new Event.Queue.Manager().queue("actor");
		when(actor.manager()).thenReturn(manager);
		when(actor.manager().queue()).thenReturn(queue);

		// Init attributes
		final EntityValueIntegerMap values = new EntityValueIntegerMap();
		final EnumerationIntegerMap<Attribute> attrs = new EnumerationIntegerMap<>(Attribute.class);
		when(model.values()).thenReturn(values);
		when(model.attributes()).thenReturn(attrs);

		// Init preferences
		final PlayerSettings prefs = new PlayerSettings();
		when(actor.settings()).thenReturn(prefs);

		// Init known hidden objects
		final TransientModel hidden = mock(TransientModel.class);
		when(actor.hidden()).thenReturn(hidden);

		// Init inventory and equipment
		final Inventory inv = new Inventory();
		when(actor.contents()).thenReturn(inv);

		// Init induction manager
		final Induction.Manager induction = mock(Induction.Manager.class);
		final Notification.Handler handler = mock(Notification.Handler.class);
		when(manager.induction()).thenReturn(induction);
		when(manager.handler()).thenReturn(handler);

		// Init actor follower/leader model
		final FollowerModel follower = new FollowerModel();
		final LeaderModel leader = new LeaderModel();
		when(actor.follower()).thenReturn(follower);
		when(actor.leader()).thenReturn(leader);

		// Init player-character
		final PlayerModel player = mock(PlayerModel.class);
		final MutableSkillSet skills = new MutableSkillSet();
		when(actor.player()).thenReturn(player);
		when(player.skills()).thenReturn(skills);
		when(actor.skills()).thenReturn(skills);

		// Create required skill
		skill = new Skill.Builder()
			.name("skill")
			.defaultScore(Percentile.HALF)
			.score(Percentile.ONE)
			.duration(DURATION)
			.power(42)
			.build();
	}

	/**
	 * Adds a required skill to the actor.
	 */
	protected void addRequiredSkill() {
		actor.player().skills().add(skill);
	}

	/**
	 * Completes an induction.
	 * @param response Action response
	 * @return Induction response
	 * @throws ActionException if the induction fails
	 */
	protected Response complete(Response response) throws ActionException {
		return response.induction().get().induction().complete();
	}
}
