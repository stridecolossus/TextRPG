package org.sarge.textrpg.common;

import java.util.List;
import java.util.logging.Logger;

/**
 * Script operation.
 * @author Sarge
 */
@FunctionalInterface
public interface Script {
	/**
	 * Log for failed scripts.
	 */
	Logger LOG = Logger.getLogger(Script.class.getName());
	
	/**
	 * Performs this script.
	 * @param actor Actor
	 */
	void execute(Actor actor);
	
	/**
	 * Script operation that does nothing.
	 */
	Script NONE = actor -> {
		// Does nowt
	};

	/**
	 * Notification message script.
	 * @param text Message
	 * @return Message script
	 */
	static Script message(String text) {
		return actor -> actor.alert(new Message(text));
	}

	/**
	 * Creates a compound script operation.
	 * @param script List of operations
	 * @return Compound script
	 */
	static Script compound(List<Script> script) {
		return actor -> script.stream().forEach(step -> step.execute(actor));
	}
	
	/**
	 * Creates a conditional script.
	 * @param condition		Condition
	 * @param trueScript	Script to execute if the condition is <tt>true</tt>
	 * @param falseScript	Optional script to execute if <tt>false</tt>
	 * @return Conditional script
	 */
	static Script condition(Condition condition, Script trueScript, Script falseScript) {
		return actor -> {
			if(condition.evaluate(actor)) {
				trueScript.execute(actor);
			}
			else
			if(falseScript != null) {
				falseScript.execute(actor);
			}
		};
	}
}
