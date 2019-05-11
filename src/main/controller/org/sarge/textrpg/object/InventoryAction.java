package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.Comparator;
import java.util.Map;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Equipment;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Terrain;
import org.springframework.stereotype.Component;

/**
 * Lists inventory.
 * @author Sarge
 * @see Equipment
 */
@Component
public class InventoryAction extends AbstractAction {
	private static final Comparator<Map.Entry<Slot, WorldObject>> ORDER = Comparator.comparing(Map.Entry::getKey);

	private final ArgumentFormatter.Registry formatters;

	/**
	 * Constructor.
	 * @param formatters Argument formatters
	 */
	public InventoryAction(ArgumentFormatter.Registry formatters) {
		super(Flag.OUTSIDE);
		this.formatters = notNull(formatters);
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
	 * Lists inventory/equipment.
	 * @param actor Actor
	 * @return Equipment response
	 */
	@RequiresActor
	public Response inventory(Entity actor) {
		// List equipped objects by slot order
		final var equipment = actor.contents().equipment().equipment().entrySet().stream()
			.sorted(ORDER)
			.map(this::describe)
			.collect(toList());

		// Build response
		if(equipment.isEmpty()) {
			return Response.of("list.inventory.empty");
		}
		else {
			final Response.Builder response = new Response.Builder();
			response.add("list.inventory.header");
			response.add(equipment);
			return response.build();
		}
	}

	/**
	 * Describes an equipped object.
	 * @param slot		Deployment slot
	 * @param obj		Equipped object
	 * @return Description
	 */
	private Description describe(Map.Entry<Slot, WorldObject> entry) {
		// Build inventory entry
		final Slot slot = entry.getKey();
		final WorldObject obj = entry.getValue();
		final Description.Builder builder;
		// TODO - worn as shield?
		// TODO - ignore two-handed
		if(slot.isHanded()) {
			// Build short entry for wielded or held objects
			final String suffix = obj.descriptor().equipment().slot() == Slot.MAIN ? "wielded" : "held";
			builder = new Description.Builder("list.inventory.short").add("verb", TextHelper.join("equip.verb", suffix));
		}
		else {
			// Build full entry for equipment
			builder = new Description.Builder("list.inventory.equipment")
				.add("verb", TextHelper.join("equip.verb", slot.verb()))
				.add("placement", TextHelper.join("slot.place", slot.placement()))
				.add("slot", TextHelper.prefix(slot));
		}

		// Add object specific arguments
		builder.name(obj.name());
		obj.describe(true, builder, formatters);
		return builder.build();
	}
}
