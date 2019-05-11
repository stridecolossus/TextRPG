package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.oneOrMore;

import java.util.function.Function;
import java.util.function.Predicate;

import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Calculator for perception tests.
 * @author Sarge
 */
@Service
public class PerceptionCalculator {
	private final int multiplier;

	/**
	 * Constructor.
	 * @param multiplier Perception attribute multiplier
	 */
	public PerceptionCalculator(@Value("${perception.calculator.multiplier}") int multiplier) {
		this.multiplier = oneOrMore(multiplier);
	}

	/**
	 * Calculates the modified perception score for the given actor.
	 * @param actor Actor
	 * @return Score
	 */
	public Percentile score(Entity actor) {
		final int base = actor.model().attributes().get(Attribute.PERCEPTION).get() * multiplier;
		final int score = Math.min(base, Percentile.MAX);
		return Percentile.of(score);
	}

	/**
	 * Helper - Creates a filter for perceived objects.
	 * @param score			Perception score
	 * @param mapper		Extracts the difficulty percentile
	 * @return Perception filter
	 */
	public <T> Predicate<T> filter(Percentile score, Function<T, Percentile> mapper) {
		return arg -> {
			final Percentile diff = mapper.apply(arg);
			if(Percentile.ZERO.equals(diff)) return false;
			if(Percentile.ONE.equals(diff)) return true;
			return diff.isLessThan(score);
		};
	}
}
