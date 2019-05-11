package org.sarge.textrpg.common;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.entity.CalculationLoader;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Registry;

/**
 * Loader for skill definitions.
 * @author Sarge
 */
public class SkillLoader {
	private final Registry.Builder<Skill> registry = new Registry.Builder<>(Skill::name);

	private final CalculationLoader loader;

	/**
	 * Constructor.
	 * @param loader Calculation loader
	 */
	public SkillLoader(CalculationLoader loader) {
		this.loader = notNull(loader);
	}

	/**
	 * @return Skills registry
	 */
	public Registry<Skill> registry() {
		return registry.build();
	}

	/**
	 * Loads a skill.
	 */
	public Skill load(Element xml) {
		if(xml.name().equals("group")) {
			Skill prev = null;
			for(Element e : xml.children().collect(toList())) {
				prev = load(e, prev);
			}
			return prev;
		}
		else {
			return load(xml, null);
		}
	}

	private Skill load(Element xml, Skill prev) {
		// Start skill
		final Skill.Builder builder = new Skill.Builder();

		// Load skill descriptor
		builder
			.name(xml.attribute("name").toText())
			.power(xml.attribute("power").toInteger(0))
			.score(xml.attribute("score").toValue(Percentile.CONVERTER))
			.duration(xml.attribute("duration").toValue(Duration.ZERO, DurationConverter.CONVERTER))
			.scale(xml.attribute("scale").toInteger(1))
			.cost(xml.attribute("cost").toInteger())
			.previous(prev);

		// Load modifier calculation
		xml.find("modifier").map(Element::child).map(loader::load).ifPresent(builder::modifier);

		// Load default score
		xml.attribute("default").optional(Percentile.CONVERTER).ifPresent(builder::defaultScore);

		// Load required skills
		xml.children("required").map(Element::text).map(registry::get).forEach(builder::required);

		// Create skill
		final Skill skill = builder.build();

		// Register skill
		registry.add(skill);

		return skill;
	}
}
