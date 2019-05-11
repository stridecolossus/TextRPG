package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notEmpty;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Description;

/**
 * Inventory controller.
 * @author Sarge
 */
public class InventoryController {
	private final CarryController carry = new CarryController();
	private final String prefix;

	/**
	 * Constructor.
	 * @param prefix Description prefix
	 */
	public InventoryController(String prefix) {
		this.prefix = notEmpty(prefix);
	}

	/**
	 * Takes a set of objects.
	 * @param actor			Actor
	 * @param objects		Objects
	 * @return Descriptions
	 */
	public List<Description> take(Entity actor, Stream<WorldObject> objects) {
		return objects.map(obj -> take(actor, obj)).collect(toList());
	}

	/**
	 * Takes an object.
	 * @param actor		Actor
	 * @param obj		Object to take
	 * @return Description
	 */
	public Description take(Entity actor, WorldObject obj) {
		final CarryController.Result result = carry.carry(actor, obj);
		take(actor, obj, result);
		return result.describe(obj, prefix);
	}

	/**
	 * Takes an object.
	 * @param actor			Actor
	 * @param obj			Object
	 * @param result		Carry result
	 * @see CarryController.Result#apply(WorldObject, Entity)
	 */
	protected void take(Entity actor, WorldObject obj, CarryController.Result result) {
		result.apply(obj, actor);
	}
}
