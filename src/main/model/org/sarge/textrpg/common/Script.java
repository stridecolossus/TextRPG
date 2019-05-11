package org.sarge.textrpg.common;

import java.util.List;

/**
 * Script.
 * @author Sarge
 */
public interface Script {
	/**
	 * Executes this script.
	 * @param actor Actor
	 */
	void execute(Actor actor);

	/**
	 * Script that does nothing.
	 */
	Script NONE = actor -> {
		// Does nowt
	};

	/**
	 * Creates a compound script.
	 * @param scripts Scripts
	 * @return Compound script
	 */
	static Script compound(List<Script> scripts) {
		return actor -> {
			for(Script script : scripts) {
				script.execute(actor);
			}
		};
	}
}
