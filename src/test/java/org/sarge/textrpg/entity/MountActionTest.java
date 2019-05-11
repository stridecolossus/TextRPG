package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.Follower.FollowerModel;
import org.sarge.textrpg.entity.Leader.LeaderModel;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class MountActionTest extends ActionTestBase {
	private MountAction action;
	private Mount mount;

	@BeforeEach
	public void before() {
		final FollowerModel follower = new FollowerModel();
		mount = mock(Mount.class);
		when(mount.name()).thenReturn("mount");
		when(mount.follower()).thenReturn(follower);
		when(mount.size()).thenReturn(Size.MEDIUM);
		action = new MountAction();
	}

	/**
	 * Leads a mount.
	 */
	private void add() {
		Follower.follow(mount, actor);
	}

	/**
	 * Rides a mount.
	 */
	private void ride() {
		add();
		final MountMovementMode mounted = mock(MountMovementMode.class);
		when(mounted.mover()).thenReturn(mount);
		when(actor.movement()).thenReturn(mounted);
		when(actor.model().stance()).thenReturn(Stance.MOUNTED);
	}

	@Test
	public void lead() throws ActionException {
		final Response response = action.execute(actor, MountAction.Operation.LEAD, mount);
		assertEquals(Response.of(new Description("action.mount.lead", "mount")), response);
		assertEquals(true, mount.follower().isFollowing(actor));
	}

	@Test
	public void leadAlreadyLeading() throws ActionException {
		add();
		TestHelper.expect("lead.already.following", () -> action.execute(actor, MountAction.Operation.LEAD, mount));
	}

	@Test
	public void leadAlreadyFollowingOtherEntity() throws ActionException {
		final CharacterEntity other = mock(CharacterEntity.class);
		when(other.leader()).thenReturn(new LeaderModel());
		Follower.follow(mount, other);
		TestHelper.expect("lead.following.other", () -> action.execute(actor, MountAction.Operation.LEAD, mount));
	}

	@Test
	public void leadRequiresMount() throws ActionException {
		TestHelper.expect("lead.requires.mount", () -> action.execute(actor, MountAction.Operation.LEAD));
	}

	@Test
	public void abandon() throws ActionException {
		add();
		final Response response = action.execute(actor, MountAction.Operation.ABANDON, mount);
		assertEquals(Response.of(new Description("action.mount.abandon", "mount")), response);
		assertEquals(false, mount.follower().isFollowing(actor));
	}

	@Test
	public void abandonAllMounts() throws ActionException {
		add();
		final Response response = action.execute(actor, MountAction.Operation.ABANDON);
		assertEquals(Response.of("abandon.mount.all"), response);
		assertEquals(false, mount.follower().isFollowing(actor));
	}

	@Test
	public void abandonNotFollowing() throws ActionException {
		TestHelper.expect("abandon.requires.mount", () -> action.execute(actor, MountAction.Operation.ABANDON));
	}

	@Test
	public void abandonMounted() throws ActionException {
		ride();
		TestHelper.expect("abandon.invalid.mounted", () -> action.execute(actor, MountAction.Operation.ABANDON, mount));
	}

	@Test
	public void mount() throws ActionException {
		// Lead a mount
		add();

		// Ride mount
		final Response response = action.execute(actor, MountAction.Operation.MOUNT);
		assertEquals(Response.of(new Description("action.mount.mount", "mount")), response);
		verify(actor.model()).stance(Stance.MOUNTED);

		// Check riding
		final ArgumentCaptor<MovementMode> captor = ArgumentCaptor.forClass(MovementMode.class);
		verify(actor).movement(captor.capture());

		// Check movement mode descriptor
		final MountMovementMode move = (MountMovementMode) captor.getValue();
		assertEquals(mount, move.mover());
	}

	@Test
	public void mountNotLeading() throws ActionException {
		TestHelper.expect("mount.not.leading", () -> action.execute(actor, MountAction.Operation.MOUNT, mount));
	}

	@Test
	public void mountRequiresMount() throws ActionException {
		TestHelper.expect("mount.requires.mount", () -> action.execute(actor, MountAction.Operation.MOUNT));
	}

	@Test
	public void mountAlreadyMounted() throws ActionException {
		add();
		when(actor.model().stance()).thenReturn(Stance.MOUNTED);
		TestHelper.expect("mount.already.mounted", () -> action.execute(actor, MountAction.Operation.MOUNT, mount));
	}

	@Test
	public void mountTooSmall() throws ActionException {
		add();
		when(actor.size()).thenReturn(Size.LARGE);
		TestHelper.expect("mount.too.small", () -> action.execute(actor, MountAction.Operation.MOUNT, mount));
	}

	@Test
	public void dismount() throws ActionException {
		ride();
		final Response response = action.execute(actor, MountAction.Operation.DISMOUNT);
		assertEquals(Response.of(new Description("action.mount.dismount", "mount")), response);
		verify(actor.model()).stance(Stance.DEFAULT);
		verify(actor).movement(null);
	}

	@Test
	public void dismountNotMounted() throws ActionException {
		TestHelper.expect("dismount.not.mounted", () -> action.execute(actor, MountAction.Operation.DISMOUNT));
	}
}
