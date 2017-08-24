package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.Util;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.loader.ConditionLoader;
import org.sarge.textrpg.loader.World;

/**
 * Loader for a {@link Script}.
 * @author Sarge
 */
public class ScriptLoader {
	private static final Converter<Openable.Operation> OPENABLE = Converter.enumeration(Openable.Operation.class);
	
	private final ConditionLoader conditionLoader;
	private final World world;

	/**
	 * Constructor.
	 * @param world				World model
	 * @param conditionLoader	Condition loader
	 */
	public ScriptLoader(World world, ConditionLoader conditionLoader) {
		Check.notNull(world);
		Check.notNull(conditionLoader);
		this.world = world;
		this.conditionLoader = conditionLoader;
	}

	/**
	 * Loads a script.
	 * @param node XML
	 * @return Script
	 */
	public Script load(Element node) {
		final Loader loader = Util.getEnumConstant(node.name(), Loader.class, () -> node.exception("Invalid script loader: " + node.name()));
		return loader.load(node, this);
	}

	/**
	 * Script loaders.
	 */
	private enum Loader {
		MESSAGE {
			@Override
			protected Script load(Element node, ScriptLoader loader) {
				return Script.message(node.text());
			}
		},
		
		COMPOUND {
			@Override
			protected Script load(Element node, ScriptLoader loader) {
				return Script.compound(node.children().map(loader::load).collect(toList()));
			}
		},
		
		CONDITION {
			@Override
			protected Script load(Element node, ScriptLoader loader) {
				final Condition condition = loader.conditionLoader.load(node.child("condition"));
				final Script trueScript = loader.load(node.child("true"));
				final Script falseScript = node.optionalChild("false").map(loader::load).orElse(null);
				return Script.condition(condition, trueScript, falseScript);
			}
		},
		
		PORTAL {
			@Override
			protected Script load(Element node, ScriptLoader loader) {
				final WorldObject obj = loader.world.getObjects().find(node.attributes().toString("name"));
				final Openable.Operation op = node.attributes().toValue("op", null, OPENABLE);
				return new PortalScript(obj, op);
			}
		},
		
		REVEAL {
			@Override
			protected Script load(Element node, ScriptLoader loader) {
				final String name = node.attributes().toString("name", null);
				final WorldObject obj = loader.world.getObjects().find(name);
				if(obj == null) throw node.exception("Unknown object: " + name);
				final String key = node.attributes().toString("message", null);
				return new RevealScript(obj, key);
			}
		};
		
		// TODO
		// - hide?
		// - spawn entity
		// - create loot
		// - destroy?
		// - bestow/complete/advance quest
		// - control?
		// - resetable?
		// - teleport location
		// - money?
	
		/**
		 * Loads a script.
		 * @param node		XML
		 * @param loader	Loader
		 * @return Script
		 */
		protected abstract Script load(Element node, ScriptLoader loader);
	}
}
	