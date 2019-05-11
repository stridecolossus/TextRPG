package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.util.Percentile;

/**
 * Wrapper for a hidden object.
 * @author Sarge
 */
public class HiddenObject extends WorldObject {
	/**
	 * Hides an object.
	 * @param obj			Object to hide
	 * @param vis			Visibility
	 * @param owner			Owner
	 * @return Hidden object wrapper
	 */
	public static HiddenObject hide(WorldObject obj, Percentile vis, Actor owner) {
		final var hidden = new HiddenObject(obj, vis, owner);
		obj.destroy();
		return hidden;
	}

	private final WorldObject obj;
	private final Percentile vis;
	private final Object owner;

	/**
	 * Constructor.
	 * @param obj			Object to hide
	 * @param vis			Visibility
	 * @param actor			Owner
	 */
	private HiddenObject(WorldObject obj, Percentile vis, Object owner) {
		super(obj.descriptor());
		if(obj.descriptor().isFixture()) throw new IllegalArgumentException("Cannot hide a fixture");
		this.obj = notNull(obj);
		this.vis = notNull(vis);
		this.owner = notNull(owner);
	}

	@Override
	public Percentile visibility() {
		return vis;
	}

	/**
	 * @return Hidden object
	 */
	public WorldObject object() {
		return obj;
	}

	/**
	 * @return Original owner of this hidden object
	 */
	public Object owner() {
		return owner;
	}
}
