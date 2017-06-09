package org.sarge.textrpg.object;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Randomiser;

/**
 * Factory for loot.
 * @author Sarge
 */
public interface LootFactory {
	/**
	 * @param actor Actor generating loot
	 * @return Generated loot
	 */
	Stream<WorldObject> generate(Actor actor);

	/**
	 * Creates a compound loot-factory.
	 * @param factories Loot factories
	 * @return Compound loot-factory
	 */
	static LootFactory compound(Collection<LootFactory> factories) {
		Check.notNull(factories);
		return actor -> factories.stream().flatMap(f -> f.generate(actor));
	}

	/**
	 * Creates a loot-factory that generates object(s) of the given descriptor.
	 * @param descriptor	Descriptor for the object to be generated
	 * @param num			Number to generate
	 * @return Object loot-factory
	 * @see ObjectDescriptor#create()
	 */
	static LootFactory object(ObjectDescriptor descriptor, int num) {
		Check.notNull(descriptor);
		Check.oneOrMore(num);
		return actor -> IntStream.range(0, num).mapToObj(n -> descriptor.create());
	}

	/**
	 * Convenience method to create a loot-factory that generates {@link Money}.
	 * @param value Monetary value
	 * @return Money loot-factory
	 */
	static LootFactory money(Value value) {
		Check.notNull(value);
		return actor -> Stream.of(new Money(value.evaluate(actor)));
	}
	
	/**
	 * Loot-factory that has a random chance to generate loot.
	 * @param chance	Percentile chance to generate loot
	 * @param mod		Optional modifier
	 * @param delegate	Loot-factory
	 * @return Chance loot-factory
	 */
	static LootFactory chance(Percentile chance, Value mod, LootFactory delegate) {
		return actor -> {
			final int result = Randomiser.range(100) + Optional.ofNullable(mod).map(val -> val.evaluate(actor)).orElse(0);
			if(result > chance.intValue()) {
				return delegate.generate(actor);
			}
			else {
				return Stream.empty();
			}
		};
	}
}
