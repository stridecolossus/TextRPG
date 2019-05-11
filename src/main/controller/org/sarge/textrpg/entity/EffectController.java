package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.List;

import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.entity.EntityModel.AppliedEffect;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.ValueModifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Controller for effects applied to an entity.
 * @author Sarge
 */
@Controller
public class EffectController {
	private Duration duration = Duration.ofMinutes(1);

	/**
	 * Sets the duration of a panic effect.
	 * @param duration Panic duration
	 */
	@Autowired
	public void setPanicDuration(@Value("${effect.panic.duration}") Duration duration) {
		this.duration = notNull(duration);
	}

	/**
	 * Applies the given effect to the target entities.
	 * @param actor			Actor
	 * @param effect		Effect descriptor
	 * @param targets		Target(s)
	 */
	public void apply(Entity actor, Effect effect, List<Entity> targets) {
		// Determine effect magnitude
		final int size = (int) effect.size().evaluate(actor.model());

		// Apply to targets
		final Duration duration = effect.duration();
		for(Entity e : targets) {
			// Apply effect
			final AppliedEffect applied = apply(effect, size, e);

			// Register refresh/removal event
			if(!duration.isZero()) {
				final Event remove = () -> {
					// TODO - repeating effects, needs a count-down
					applied.remove();
					return false;
				};
				e.manager().queue().add(remove, duration);
			}
		}
	}

	/**
	 * Applies an effect to the given entity.
	 * @param effect		Effect descriptor
	 * @param entity		Entity
	 * @return Applied effect record
	 */
	public AppliedEffect apply(Effect effect, Entity entity) {
		final int size = (int) effect.size().evaluate(null);
		return apply(effect, size, entity);
	}

	/**
	 * Applies an effect and creates an applied effect record.
	 * @param effect		Effect descriptor
	 * @param size			Effect magnitude
	 * @param entity		Entity
	 * @return Applied effect
	 */
	public AppliedEffect apply(Effect effect, int size, Entity entity) {
		// Lookup mutator for this modifier
		final EntityModel model = entity.model();
		final ValueModifier mod = model.modifier(effect.modifier());

		// Apply effect
		mod.modify(size);

		// Register applied effect
		return model.new AppliedEffect(effect.name(), mod, size, effect.group());
	}

	/**
	 * Helper - Applies a panic effect.
	 * @param actor			Actor
	 * @param size			Panic level
	 */
	public void panic(Entity actor, int size) {
		// Create panic effect
		final Effect effect = new Effect.Builder()
			.name("effect.panic")
			.modifier(EntityValue.PANIC.key())
			.duration(duration)
			.build();

		// Apply panic effect
		final AppliedEffect applied = apply(effect, size, actor);

		// Register removal
		final Event event = () -> {
			applied.remove();
			if(actor.isPlayer() && actor.model().values().get(EntityValue.PANIC.key()).get() == 0) {
				actor.alert(new Description("panic.removed"));
			}
			return false;
		};
		actor.manager().queue().add(event, duration);
	}
}
