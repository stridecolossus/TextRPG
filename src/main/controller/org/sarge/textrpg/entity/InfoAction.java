package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.function.Function;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.EntityModel.AppliedEffect;
import org.sarge.textrpg.entity.PlayerSettings.Setting;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.BandingTable;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Terrain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Displays information about the player.
 * @author Sarge
 */
@Component
public class InfoAction extends AbstractAction {
	private static final List<EntityValue> PRIMARY = List.of(EntityValue.HEALTH, EntityValue.POWER, EntityValue.STAMINA);
	private static final List<EntityValue> DEFENCE = List.of(EntityValue.ARMOUR, EntityValue.BLOCK, EntityValue.DODGE, EntityValue.PARRY);

	private final EncumberanceCalculator calc;
	private final ArgumentFormatter numeric;
	private final ArgumentFormatter money;

	private Function<Percentile, String> encumberance = ignore -> "none";
	private int threshold = 0;

	/**
	 * Constructor.
	 * @param calc			Encumberance calculator
	 * @param numeric		Numeric formatter
	 * @param money			Money argument formatter
	 */
	public InfoAction(EncumberanceCalculator calc, @Qualifier(ArgumentFormatter.NUMERIC) ArgumentFormatter numeric, @Qualifier(ArgumentFormatter.MONEY) ArgumentFormatter money) {
		super(Flag.OUTSIDE, Flag.ACTIVE);
		this.calc = notNull(calc);
		this.numeric = notNull(numeric);
		this.money = notNull(money);
	}

	/**
	 * Sets the threshold for hunger/thirst warnings.
	 * @param threshold Threshold
	 */
	@Autowired
	public void setThreshold(@Value("${entity.warning.threshold}") Percentile threshold) {
		this.threshold = threshold.intValue();
	}

	/**
	 * Sets the encumberance banding table.
	 * @param encumberance Encumberance banding table
	 */
	@Autowired
	public void setEncumberanceMapper(@Value("#{banding.table('encumberance')}") BandingTable<Percentile> encumberance) {
		this.encumberance = encumberance::map;
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	public boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Displays player information.
	 * @param actor Actor
	 * @return Player info
	 */
	@RequiresActor
	public Response info(PlayerCharacter actor) {
		// Build player info
		final Description player = new Description.Builder("player.info.player")
			.add("name", actor.name(), ArgumentFormatter.PLAIN)
			.add("race", actor.descriptor().race().name())
			.add("gender", actor.descriptor().gender())
			.add("alignment", actor.descriptor().alignment())
			.build();

		// Build attributes
		final IntegerMap<Attribute> attrs = actor.model().attributes();
		final Description.Builder attributes = new Description.Builder("player.info.attributes");
		for(Attribute attr : Attribute.values()) {
			attributes.add(attr.mnemonic(), attrs.get(attr).get());
		}

		// Build entity-values
		final IntegerMap<EntityValue.Key> values = actor.model().values();
		final Description primary = build("player.info.primary", values, PRIMARY);
		final Description defence = build("player.info.defence", values, DEFENCE);

		// Build equipment info
		final int armour = values.get(EntityValue.ARMOUR.key()).get();
		final Description equipment = new Description.Builder("player.info.equipment")
			.add("weight", actor.contents().weight(), numeric)
			.add("armour", armour, numeric)
			.add("encumberance", TextHelper.join("encumberance", encumberance.apply(calc.calculate(actor))))
			.build();

		// Build cash info
		final PlayerSettings settings = actor.settings();
		final int amount = settings.toInteger(Setting.CASH);
		final Description.Builder cash = new Description.Builder("player.info.cash");
		if(amount == 0) {
			cash.add("money", "player.info.cash.none");
		}
		else {
			cash.add("money", amount, money);
		}

		// Build experience info
		final Description xp = new Description.Builder("player.info.xp")
			.add("xp", settings.toInteger(Setting.EXPERIENCE))
			.add("points", settings.toInteger(Setting.POINTS))
			.build();

		// Build response
		final Response.Builder response = new Response.Builder();
		response.add(player);
		response.add(attributes.build());
		response.add(primary);
		response.add(defence);
		response.add(equipment);
		if(armour == 0) {
			response.add(Description.of("player.info.armour.none"));
		}
		response.add(cash.build());
		response.add(xp);

		// Add hunger and thirst warnings
		warning("player.info.hunger", values.get(EntityValue.HUNGER.key()), response);
		warning("player.info.thirst", values.get(EntityValue.THIRST.key()), response);

		// Add panic indicator
		final int panic = values.get(EntityValue.PANIC.key()).get();
		if(panic > 0) {
			response.add(Description.of("player.info.panic"));
		}

		// Add applied effects
		add(actor, Effect.Group.DISEASE, response);
		add(actor, Effect.Group.POISON, response);

		// List wounds
		actor.model().effects()
			.filter(e -> e.group() == Effect.Group.WOUND)
			.map(InfoAction::wound)
			.forEach(response::add);

		return response.build();
	}

	/**
	 * Builds entity values.
	 */
	private static Description build(String key, IntegerMap<EntityValue.Key> values, List<EntityValue> list) {
		final Description.Builder builder = new Description.Builder(key);
		for(EntityValue value : list) {
			builder.add(value.mnemonic(), values.get(value.key()).get());
			if(value.isPrimary()) {
				final EntityValue.Key max = value.key(EntityValue.Key.Type.MAXIMUM);
				builder.add(TextHelper.join("max", value.mnemonic()), values.get(max).get());
			}
		}
		return builder.build();
	}

	/**
	 * Adds a hunger/thirst warning.
	 */
	private void warning(String key, IntegerMap.Entry value, Response.Builder builder) {
		if(value.get() > threshold) {
			builder.add(key);
		}
	}

	/**
	 * Adds an effect indicator.
	 * @param actor			Actor
	 * @param group			Effect group
	 * @param builder		Response
	 */
	private static void add(Entity actor, Effect.Group group, Response.Builder builder) {
		// TODO - magnitude -> banding?
		final boolean present = actor.model().effects().anyMatch(e -> e.group() == group);
		if(present) {
			builder.add(Description.of(TextHelper.join("player.info.effect", group.name())));
		}
	}

	/**
	 * Builds a wound description.
	 * @param wound Wound effect
	 * @return Description
	 */
	private static Description wound(AppliedEffect wound) {
		// TODO - banding, bound
//		 * light
//		 * deep
//		 * serious
//		 * grevious
//		 * critical
		return new Description.Builder("player.info.wound").add("size", wound.size()).build();
	}
}
