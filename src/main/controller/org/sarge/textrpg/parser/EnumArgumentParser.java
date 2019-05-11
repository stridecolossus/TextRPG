package org.sarge.textrpg.parser;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.util.WordCursor;

/**
 * An <i>enum</i> argument parser parses an enumeration constant.
 * @author Sarge
 * @param <E> Enumeration
 */
public class EnumArgumentParser<E extends Enum<E> & CommandArgument> implements ArgumentParser<E> {
	private final Map<String, E> map;

	/**
	 * Constructor.
	 * @param clazz Enumeration class
	 */
	public EnumArgumentParser(String prefix, Class<E> clazz) {
		this(prefix, Arrays.asList(clazz.getEnumConstants()));
	}

	/**
	 * Constructor for a sub-set of constants.
	 * @param constants Enum constants
	 */
	public EnumArgumentParser(String prefix, Collection<E> constants) {
		final Function<E, String> mapper = e -> TextHelper.join(prefix, e.name().toLowerCase());
		this.map = constants.stream().collect(toMap(mapper, Function.identity()));
	}

	@Override
	public E parse(WordCursor cursor) {
		final String word = cursor.next();
		final NameStore store = cursor.store();
		for(String key : map.keySet()) {
			if(store.matches(key, word)) {
				return map.get(key);
			}
		}
		return null;
	}
}
