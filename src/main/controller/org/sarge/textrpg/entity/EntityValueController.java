package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;
import java.util.function.Function;

import org.sarge.textrpg.object.Light;
import org.sarge.textrpg.runner.SessionManager;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Controller for initialising and updating transient entity values.
 * @author Sarge
 */
@Controller
public class EntityValueController {
	private final Event.Queue queue;
	private final SessionManager manager;
	private final EntityValueCalculator init;
	private final EntityValueCalculator update;

	private Function<Stance, Float> stances = ignore -> 1f;
	private Duration period = Duration.ofMinutes(1);
	private int threshold = Integer.MAX_VALUE;
	private long tick = Duration.ofSeconds(1).toMillis();
	private float camp = 1;

	/**
	 * Constructor.
	 * @param queue			Event queue
	 * @param manager		Session manager for entity updates
	 * @param init			Initialiser
	 * @param update		Updater
	 */
	public EntityValueController(Event.Queue queue, SessionManager manager, @Qualifier("calc.init") EntityValueCalculator init, @Qualifier("calc.update") EntityValueCalculator update) {
		this.queue = notNull(queue);
		this.manager = notNull(manager);
		this.init = notNull(init);
		this.update = notNull(update);
		queue.add(this::increment, period);
	}

	/**
	 * Sets the iteration period for transient value increments.
	 * @param period Iteration period
	 */
	@Autowired
	public void setPeriod(@Value("${entity.iteration.period}") Duration period) {
		this.period = DurationConverter.oneOrMore(period);
	}

	/**
	 * Sets the threshold for hunger/thirst warnings.
	 * @param threshold Threshold
	 */
	@Autowired
	public void setThreshold(@Value("${entity.warning.threshold}") int threshold) {
		this.threshold = oneOrMore(threshold);
	}

	/**
	 * Sets the update tick duration.
	 * @param tick Tick period
	 */
	@Autowired
	public void setTickDuration(@Value("${entity.tick.period}") Duration tick) {
		this.tick = DurationConverter.oneOrMore(tick).toMillis();
	}

	/**
	 * Sets the update modifier for the current stance.
	 * @param stances Stance modifier
	 */
	@Autowired
	public void setStanceModifier(Function<Stance, Float> stances) {
		this.stances = notNull(stances);
	}

	/**
	 * Sets the update modifier when a camp-fire is present.
	 * @param camp Camp-fire modifier
	 */
	@Autowired
	public void setCampFireModifier(@Value("${entity.camp.mod}") float camp) {
		this.camp = oneOrMore(camp);
	}

	/**
	 * Initialises the entity-values of the given entity.
	 * @param entity Entity
	 */
	public void init(Entity entity) {
		final EntityValueIntegerMap map = entity.model().values();
		for(EntityValue key : EntityValue.PRIMARY_VALUES) {
			final int value = (int) init.calculate(key, entity.model());
			if(value == 0) throw new IllegalStateException("Calculated entity-value is zero: " + key);
			map.get(key.key()).set(value);
		}
	}

	/**
	 * Increments cumulative entity-values for <b>all</b> active players.
	 */
	protected boolean increment() {
		manager.players().forEach(this::increment);
		return true;
	}

	/**
	 * Increments cumulative values for the given player.
	 * @param player Player
	 */
	private void increment(Entity player) {
		increment(player, EntityValue.THIRST);
		increment(player, EntityValue.HUNGER);
	}

	/**
	 * Increments a transient value and generates notifications accordingly.
	 * @param entity		Entity
	 * @param value			Value to increment
	 */
	private void increment(Entity entity, EntityValue value) {
		// Check for maximum
		final MutableIntegerMap.MutableEntry entry = entity.model().values().get(value.key());
		if(entry.get() >= Percentile.MAX) {
			entity.alert(new Description(TextHelper.join("entity.critical", value.name())));
			return;
		}

		// Increment value
		final int result = entry.modify(1);

		// Notify if threshold exceeded
		if(result > threshold) {
			entity.alert(new Description(TextHelper.join("entity.alert", value.name())));
		}
	}

	/**
	 * Updates values for the given entity.
	 * @param entity Entity
	 * @see EntityManager#update(long)
	 */
	public void update(Entity entity) {
		// Calculate time difference between updates
		final long now = queue.manager().now();
		final long last = entity.manager().updated();
		final long num = (now - last) / tick;

		// Ignore unless at least one complete tick
		if(num < 1) {
			return;
		}

		// Mark as updated
		entity.manager().update(last + num * tick);

		// Stop if swimming
		final Stance stance = entity.model().stance();
		if(stance == Stance.SWIMMING) {
			return;
		}

		// Calculate modifier
		final float mod = num * modifier(entity);

		// Update entity values
		final var values = entity.model().values();
		for(EntityValue key : EntityValue.PRIMARY_VALUES) {
			final double value = mod * update.calculate(key, entity.model());
			assert value >= 0;
			values.get(key.key()).modify((float) value);
		}
	}

	/**
	 * Determines the stance modifier for the given entity.
	 * @param entity Entity
	 * @return Stance modifier
	 */
	private float modifier(Entity entity) {
		// Lookup stance modifier
		final Stance stance = entity.model().stance();
		final Float mod = stances.apply(stance);
		if(mod == null) {
			return 1f;
		}

		// Apply camp-fire modifier
		switch(stance) {
		case RESTING:
		case SLEEPING:
			if(Light.find(entity.location(), Light.Type.CAMPFIRE).isPresent()) {
				return mod * camp;
			}
		}

		// Otherwise use default modifier
		return mod;
	}
}
