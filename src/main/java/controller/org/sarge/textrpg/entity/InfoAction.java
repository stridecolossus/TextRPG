package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.EnumSet;
import java.util.List;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.Furniture;
import org.sarge.textrpg.object.Vehicle;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.IntegerMap;

/**
 * Action to list character information.
 * @author Sarge
 */
public class InfoAction extends AbstractAction {
	/**
	 * Types of info.
	 */
	public enum Operation {
		/**
		 * List transient values and effects.
		 */
		STATS {
			private final EnumSet<EntityValue> KEYS = EnumSet.of(EntityValue.HEALTH, EntityValue.POWER, EntityValue.STAMINA);
			
			@Override
			public Description execute(Entity player) {
				final Description.Builder desc = new Description.Builder("info.stats");
				final IntegerMap<EntityValue> values = player.values();
				for(EntityValue key : KEYS) {
					desc.add(key.name(), values.get(key));
					key.max().ifPresent(k -> desc.add(k.name(), values.get(k)));
				}
				if(values.get(EntityValue.HUNGER) > 0) {
					desc.add(new Description("hungry"));
				}
				if(values.get(EntityValue.THIRST) > 0) {
					desc.add(new Description("thirsty"));
				}
				// TODO - applied effects
				return desc.build();
			}
		},
		
		/**
		 * Display character info.
		 */
		INFO {
			private final int SILVER = 1000;
			private final int GOLD  = 20 * SILVER;
			
			@Override
			public Description execute(Entity player) {
				// Add character info
				final Description.Builder info = new Description.Builder("character.info");
				info.add("name", player.name());
				info.wrap("gender", "gender." + player.gender());
				info.wrap("race", player.race().name());
				info.wrap("alignment", player.alignment());
				
				// Add attribute values
				final Description.Builder attributesBuilder = new Description.Builder("character.info.attributes");
				final IntegerMap<Attribute> attrs = player.attributes();
				for(Attribute attr : Attribute.values()) {
					attributesBuilder.add(attr.name(), attrs.get(attr));
				}
				info.add(attributesBuilder.build());
				
				// Add mount or vehicle
				switch(player.parent().parentName()) {
				case Vehicle.NAME:
					info.add(new Description("character.info.vehicle", "name", player.parent()));
					break;
					
				case Furniture.NAME:
					final Description desc = new Description.Builder("character.info.furniture")
						.add("stance", "stance." + player.stance())
						.add("name", player.parent())
						.build();
					info.add(desc);
					break;
					
				// TODO - mount
				}

				// Add equipment weight
				// TODO - banding
				info.add(new Description.Builder("character.info.equipment").add("weight", player.equipment().getWeight()).build());
				
				// Add armour
				info.add(new Description.Builder("character.info.armour").add("armour", player.values().get(EntityValue.ARMOUR)).build());

				// Add money
				final int cash = player.values().get(EntityValue.CASH);
				final Description.Builder money = new Description.Builder("character.info.money");
				add("gold", cash / GOLD, money);
				add("silver", (cash % GOLD) / SILVER, money);
				add("copper", cash % SILVER, money);
				info.add(money.buildNone());
				
				// Add experience
				final Description xp = new Description.Builder("character.info.xp")
					.add("xp", player.values().get(EntityValue.EXPERIENCE))
					.add("points", player.values().get(EntityValue.POINTS))
					.build();
				info.add(xp);
				
				return info.build();
			}
			
			private void add(String key, int amount, Description.Builder b) {
				if(amount > 0) {
					final String plural = amount == 1 ? "one" : "many";
					final Description desc = new Description.Builder("money." + key)
						.add(key + ".amount", amount)
						.wrap(key + ".count", key + "." + plural)
						.newline(false)
						.build();
					b.add(desc);
				}
			}
		},
		
		/**
		 * List player inventory.
		 */
		INVENTORY {
			@Override
			protected Description execute(Entity player) {
				final List<Description> inv = player.contents().stream()
					.map(t -> (WorldObject) t)
					.map(WorldObject::describeShort)
					.collect(toList());
				return Description.create("info.inventory", inv);
			}
		},
		
		/**
		 * List equipment.
		 */
		EQUIPMENT {
			@Override
			public Description execute(Entity player) {
				return Description.create("info.equipment", player.equipment().describe());
			}
		},
		
		/**
		 * List skills.
		 */
		SKILLS {
			@Override
			public Description execute(Entity player) {
				return Description.create("info.skills", player.skills().describe());
			}
		};
		
		protected abstract Description execute(Entity player) throws ActionException;
	}

	private final Operation op;
	
	public InfoAction(Operation op) {
		super(op.name());
		this.op = op;
	}

	@Override
	public boolean isCombatBlockedAction() {
		switch(op) {
		case STATS:
			return false;
			
		default:
			return true;
		}
	}
	
	@Override
	public boolean isLightRequiredAction() {
		switch(op) {
		case INVENTORY:
			return true;
			
		default:
			return false;
		}
	}
	
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	@Override
	public boolean isValidStance(Stance stance) {
		if((op == Operation.INFO) && (stance == Stance.SLEEPING)) {
			return true;
		}
		else {
			return super.isValidStance(stance);
		}
	}
	
	/**
	 * Display character information.
	 * @param ctx
	 * @param actor
	 * @throws ActionException
	 */
	public ActionResponse info(Player actor) throws ActionException {
		final Description desc = op.execute(actor);
		return new ActionResponse(desc);
	}
}
