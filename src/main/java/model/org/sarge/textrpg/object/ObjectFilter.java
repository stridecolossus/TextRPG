package org.sarge.textrpg.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.object.ObjectDescriptor.Equipment;
import org.sarge.textrpg.world.Shop;

/**
 * Object descriptor filter.
 * @see Shop
 */
public final class ObjectFilter implements Predicate<ObjectDescriptor> {
	/**
	 * Standard shop filters.
	 */
	public static final List<ObjectFilter> FILTERS;

	/**
	 * Filter that returns <b>all</b> stock.
	 */
	public static final ObjectFilter ALL = new ObjectFilter("object.all", desc -> true);

	/**
	 * Creates a deployment-slot filter.
	 */
	protected static ObjectFilter create(DeploymentSlot slot) {
		final Predicate<ObjectDescriptor> predicate = desc -> desc.getEquipment().map(Equipment::getDeploymentSlot).map(slot::equals).orElse(false);
		return new ObjectFilter("slot." + slot.name(), predicate);
	}

	/**
	 * Creates a weapon damage-type filter.
	 */
	protected static ObjectFilter create(DamageType type) {
		final Predicate<ObjectDescriptor> predicate = desc -> {
			if(desc instanceof Weapon.Descriptor) {
				final Weapon.Descriptor weapon = (Weapon.Descriptor) desc;
				return weapon.damage().damageType() == type;
			}
			else {
				return false;
			}
		};
		return new ObjectFilter("filter." + type.name(), predicate);
	}

	/**
	 * Build filters.
	 */
	static {
		// Add standard filters
		final List<ObjectFilter> filters = new ArrayList<>(Arrays.asList(
			ALL,
			new ObjectFilter("filter.edible", desc -> desc instanceof Food.Descriptor),
			new ObjectFilter("filter.container", desc -> desc instanceof Container.Descriptor),
			new ObjectFilter("filter.weapon", desc -> desc instanceof Weapon.Descriptor)
		));

		// Add slot filters
		final Collection<DeploymentSlot> slots = new ArrayList<>(Arrays.asList(DeploymentSlot.values()));
		slots.remove(DeploymentSlot.MAIN_HAND);
		slots.remove(DeploymentSlot.OFF_HAND);
		slots.remove(DeploymentSlot.KEYRING);
		slots.remove(DeploymentSlot.BELT);
		slots.stream().map(ObjectFilter::create).forEach(filters::add);

		// Add damage type filters
		Arrays.asList(DamageType.values()).stream().map(ObjectFilter::create).forEach(filters::add);

		// Ensure immutable
		FILTERS = Collections.unmodifiableList(filters);
	}

	private final String name;
	private final Predicate<ObjectDescriptor> predicate;

	/**
	 * Constructor.
	 * @param name			Filter name
	 * @param predicate		Predicate
	 */
	protected ObjectFilter(String name, Predicate<ObjectDescriptor> predicate) {
		Check.notEmpty(name);
		Check.notNull(predicate);
		this.name = name.toLowerCase();
		this.predicate = predicate;
	}

	@Override
	public boolean test(ObjectDescriptor descriptor) {
		return predicate.test(descriptor);
	}

	@Override
	public String toString() {
		return name;
	}
}
