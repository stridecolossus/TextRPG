package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;

/**
 * Manager for transient objects in the world, e.g. plants that can picked, lost purse, contents of a chest, etc.
 * @author Sarge
 */
public class ObjectFactory implements Contents.Listener {
	/**
	 * Event queue for refresh events.
	 */
	public static final EventQueue QUEUE = new EventQueue();

	private final LootFactory factory;
	private final Parent parent;
	private final long reset;
	
	private final Collection<WorldObject> generated = new HashSet<>();

	/**
	 * Constructor.
	 * @param factory		Object factory
	 * @param parent		Parent
	 * @param count			Initial number of objects to generate
	 * @param reset			Reset duration (ms)
	 */
	public ObjectFactory(LootFactory factory, Parent parent, int count, long reset) {
		Check.notNull(factory);
		Check.oneOrMore(count);
		Check.oneOrMore(reset);
		Check.notNull(factory);
		this.factory = factory;
		this.parent = parent;
		this.reset = reset;

		// Register listener
		parent.getContents().add(this);
		
		// Init contents
		for(int n = 0; n < count; ++n) {
			update();
		}
	}
	
	/**
	 * Adds new contents.
	 */
	private void update() {
		final Collection<WorldObject> loot = factory.generate(Actor.SYSTEM).collect(toList());
		loot.forEach(obj -> obj.setParentAncestor(parent));
		generated.addAll(loot);
	}
	
	@Override
	public void notify(boolean add, Thing obj) {
		if(!add && generated.contains(obj)) {
			generated.remove(obj);
			QUEUE.add(this::update, reset);
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
