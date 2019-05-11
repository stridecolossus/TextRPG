package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.TextHelper;

/**
 * Controller for carrying objects.
 * @author Sarge
 */
public class CarryController {
	private static final Result FIXTURE = new Result("invalid.fixture");
	private static final Result TOO_LARGE = new Result("too.large");
	private static final Result TOO_HEAVY = new Result("too.heavy");
	private static final Result ALREADY = new Result("already.carried");
	private static final Result CANNOT = new Result("cannot.carry");

	/**
	 * Carry result.
	 */
	public static class Result extends AbstractEqualsObject {
		private final String message;

		/**
		 * Constructor.
		 * @param message Message
		 */
		public Result(String message) {
			this.message = notEmpty(message);
		}

		/**
		 * @return Result message/reason
		 */
		public final String message() {
			return message;
		}

		/**
		 * @return Whether the object can be carried
		 */
		public boolean isCarried() {
			return false;
		}

		/**
		 * Describes this result.
		 * @param obj			Object
		 * @param prefix		Prefix
		 * @return Description
		 */
		public Description describe(WorldObject obj, String prefix) {
			final Description.Builder builder = new Builder(TextHelper.join(prefix, message));
			builder.name(obj.name());
			describe(builder);
			return builder.build();
		}

		/**
		 * Adds additional description arguments for this result (default does nothing).
		 * @param builder Description builder
		 */
		protected void describe(Description.Builder builder) {
			// Does nowt
		}

		/**
		 * Applies this result (drops to current location by default).
		 * @param obj		Object
		 * @param actor		Actor
		 */
		public void apply(WorldObject obj, Entity actor) {
			obj.parent(actor.location());
		}
	}

	/**
	 * Result for a contained object.
	 */
	private static class ContainerResult extends Result {
		private final Container container;

		/**
		 * Constructor.
		 * @param container
		 */
		public ContainerResult(Container container) {
			super("object.container");
			this.container = notNull(container);
		}

		@Override
		public boolean isCarried() {
			return true;
		}

		@Override
		protected void describe(Builder builder) {
			builder.add("container", container.name());
		}

		@Override
		public void apply(WorldObject obj, Entity actor) {
			obj.parent(container);
		}
	}

	/**
	 * Result for an equipped/held object.
	 */
	private static class CarriedResult extends Result {
		private final Slot slot;

		/**
		 * Constructor.
		 * @param slot Deployment slot
		 */
		public CarriedResult(Slot slot) {
			super("object.carried");
			this.slot = notNull(slot);
		}

		@Override
		public boolean isCarried() {
			return true;
		}

		@Override
		public void apply(WorldObject obj, Entity actor) {
			obj.parent(actor);
			actor.contents().equipment().equip(obj, slot);
		}
	}

	/**
	 * Determines whether the given object can be carried.
	 * @param actor		Actor
	 * @param obj		Object to carry
	 * @return Result
	 */
	public Result carry(Entity actor, WorldObject obj) {
		// Check can be carried
		final ObjectDescriptor descriptor = obj.descriptor();
		if(descriptor.isFixture()) {
			return FIXTURE;
		}
		if(actor.size().isLessThan(descriptor.properties().size())) {
			return TOO_LARGE;
		}
		if(obj.weight() > actor.descriptor().race().characteristics().weight()) {
			return TOO_HEAVY;
		}
		if(actor.contents().contains(obj)) {
			return ALREADY;
		}

		// Determine result
		final Inventory inv = actor.contents();
		return inv.reason(obj).map(Result::new)
			.or(() -> inv.container(obj).map(ContainerResult::new))
			.or(() -> hand(inv, obj))
			.orElse(CANNOT);
	}

	/**
	 * Carries the given object in a free hand if available.
	 * @param inv Inventory
	 * @param obj Object to carry
	 * @return Held result
	 */
	private static Optional<Result> hand(Inventory inv, WorldObject obj) {
		final Equipment equipment = inv.equipment();
		// TODO - auto-wear
		return equipment.free().map(hand -> new CarriedResult(hand));
	}
}
