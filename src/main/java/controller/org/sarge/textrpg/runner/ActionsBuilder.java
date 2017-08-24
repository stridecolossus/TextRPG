package org.sarge.textrpg.runner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictList;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.entity.*;
import org.sarge.textrpg.object.*;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.util.Config;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.ListExitsAction;

/**
 * Instantiates actions.
 * @author Sarge
 */
public class ActionsBuilder {
	
	public List<AbstractAction> build(Config cfg, Registry<Skill> skills, Registry<ObjectDescriptor> descriptors) throws IOException {
		
		
		final List<AbstractAction> actions = new StrictList<>();

		// Add enumerated actions
		enumerate(Direction.class, dir -> new MoveAction(dir)).forEach(actions::add);
		enumerate(InfoAction.Operation.class, InfoAction::new).forEach(actions::add);
		enumerate(Openable.Operation.class, PortalAction::new).forEach(actions::add);
		enumerate(Light.Operation.class, LightAction::new).forEach(actions::add);
		enumerate(GroupAction.Operation.class, GroupAction::new).forEach(actions::add);
		enumerate(MountAction.Operation.class, op -> new MountAction(op, skills.find("ride"))).forEach(actions::add);
		enumerate(Direction.class, dir -> new FollowRouteAction(dir, cfg.getLong("follow.route.period"))).forEach(actions::add);
		enumerate(ShopAction.Operation.class, ShopAction::new).forEach(actions::add);
		
		final int interactMod = cfg.getInteger("interact.stamina.mod");
		Arrays.stream(Interaction.values()).filter(action -> action != Interaction.EXAMINE).map(action -> new InteractAction(action, interactMod)).forEach(actions::add);
		
		// Add other actions
		final AbstractAction[] array = {
			// Location
			new LookAction(),
			new ExamineAction(),
			new ListExitsAction(),
			new FleeAction(cfg.getInteger("flee.movement.mod")),
			new SearchAction(cfg.getInteger("search.perception"), cfg.getLong("search.duration")),
			new ListenAction(),
			
			// Object manipulation
			new TakeAction(),
			new DropAction(),
			new PutAction(),
			new HoldAction(),
			new RopeAction(),
			new EmptyAction(),
			
			// Equipment
			new EquipAction(),
			new RemoveAction(),

			// Receptacle
			new FillAction(),
			new DrinkAction(),
			new ConsumeAction(),
			
			// Stance
			new StanceAction(Stance.DEFAULT),
			new StanceAction(Stance.RESTING),
			new StanceAction(Stance.SLEEPING),
			
			// Furniture
			new FurnitureAction(Stance.DEFAULT),
			new FurnitureAction(Stance.RESTING),
			new FurnitureAction(Stance.SLEEPING),

			// Vehicle
			new EnterAction(),
			new LeaveAction(),

			// Following
			new FollowEntityAction(),
			new FollowTracksAction(skills.find("track"), cfg.getLong("follow.tracks.period"), cfg.getInteger("tracks.movement.mod")),
			
			// Miscellaneous
			new CallAction(),
			new ClimbAction(),
			new ReadAction(),
			new TalkAction(),
			new TimeAction(),
			new CookAction(skills.find("cook"), "cooking.utensil", "cooking.fire", cfg.getLong("cook.duration")),
			new SwimAction(skills.find("swim")),
			new SmokeAction(cfg.getLong("smoke.duration")),
			new FishAction(skills.find("fish"), cfg.getLong("fish.period")),

			// Burglar
			new PickAction(skills.find("pick.lock"), descriptors.find("lockpicks"), cfg.getLong("pick.duration")),
			new BackStabAction(skills.find("backstab"), cfg.getLong("backstab.duration")),
			new SneakAction(skills.find("sneak")),
			
			// Healer
			new BandageAction(skills.find("bandage"), cfg.getLong("bandage.duration"), descriptors.find("bandage")),
			new GatherAction(Area.Resource.HERBS, skills.find("herblore"), cfg.getLong("gather.herbs.duration"), descriptors.find("sickle")),

			// Ranger
			new GatherAction(Area.Resource.WOOD, skills.find("wilderness.lore"), cfg.getLong("gather.wood.duration"), null),
			new CampAction(skills.find("wilderness.lore"), descriptors.find("camp.fire"), cfg.getInteger("camp.lifetime"), descriptors.find("fire.wood"), descriptors.find("tinderbox"), cfg.getLong("camp.duration")),
			new ButcherAction(skills.find("butcher"), cfg.getLong("butcher.duration")),
			//new TrackAction(skills.find("track"), cfg.getInteger("track.skill.mod"), cfg.getLong("tracks.duration")),
			
			// Archer
			new RecoverArrowsAction(skills.find("archery"), cfg.getInteger("recover.skill.mod"), cfg.getLong("recover.duration")),
		};
		actions.addAll(Arrays.asList(array));
		
		return actions;
	}

	/**
	 * Helper - Builds an enumerated set of actions.
	 */
	private static <E extends Enum<E>> Stream<AbstractAction> enumerate(Class<E> clazz, Function<E, AbstractAction> ctor) {
		return Arrays.stream(clazz.getEnumConstants()).map(ctor);
	}
}
