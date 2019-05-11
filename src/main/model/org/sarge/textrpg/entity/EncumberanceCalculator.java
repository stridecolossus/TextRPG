package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Encumberance calculator.
 * <p>
 * Encumberance is represented as the percentile of the actors equipment weight against the maximum weight that the entity can carry.
 * <p>
 * {@link #setThreshold(Percentile)} sets the threshold after which encumberance is applied.
 * <p>
 * @author Sarge
 */
@Component
public class EncumberanceCalculator extends AbstractObject {
	private final float str, end;

	private float threshold = 0;

	/**
	 * Constructor.
	 * @param str Strength modifier
	 * @param end Endurance modifier
	 */
	public EncumberanceCalculator(@Value("${movement.encumberance.strength}") float str, @Value("${movement.encumberance.endurance}") float end) {
		this.str = zeroOrMore(str);
		this.end = zeroOrMore(end);
	}

	/**
	 * Sets the encumberance threshold.
	 * @param threshold Threshold
	 */
	@Autowired
	public void setThreshold(@Value("${movement.encumberance.threshold}") Percentile threshold) {
		if(threshold.equals(Percentile.ONE)) throw new IllegalArgumentException("Threshold must be less-than one");
		this.threshold = threshold.floatValue();
	}

	/**
	 * Calculates the encumberance for the given entity.
	 * @param entity Entity
	 * @return Encumberance
	 */
	public Percentile calculate(Entity entity) {
		// Check for empty inventory
		final int weight = entity.contents().weight();
		if(weight == 0) {
			return Percentile.ZERO;
		}

		// Calculate max weight this entity can carry
		final int max = max(entity.model().attributes());
		if(weight >= max) {
			return Percentile.ONE;
		}

		// Apply threshold
		final float encumberance = weight / (float) max;
		if(encumberance < threshold) {
			return Percentile.ZERO;
		}

		// Calculate encumberance
		return new Percentile((encumberance - threshold) / (1 - threshold));
	}

	/**
	 * Calculates the maximum weight the entity can carry.
	 * @param map Entity attributes
	 * @return Maximum weight
	 */
	private int max(IntegerMap<Attribute> map) {
		return calculate(map, Attribute.STRENGTH, str) + calculate(map, Attribute.ENDURANCE, end);
	}

	/**
	 * Calculates an encumberance component.
	 */
	private static int calculate(IntegerMap<Attribute> map, Attribute attr, float mod) {
		return (int) (map.get(attr).get() * mod);
	}
}
