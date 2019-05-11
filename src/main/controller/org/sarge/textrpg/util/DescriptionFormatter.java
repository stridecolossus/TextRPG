package org.sarge.textrpg.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Formatter for a {@link Description}.
 * @author Sarge
 */
@Component
public class DescriptionFormatter {
	private static final Logger LOG = LoggerFactory.getLogger(DescriptionFormatter.class);

	/**
	 * Token-replacement pattern.
	 */
	private static final Pattern PATTERN = Pattern.compile("\\{[\\w\\.]+\\}");

	/**
	 * Looks up a description template.
	 * @param key Description key
	 * @return Template
	 */
	private static String lookup(String key, NameStore store) {
		final String template = store.get(key);
		if(template == null) {
			LOG.warn("Unknown description key: {}", key);
			return StringUtils.EMPTY;
		}
		else {
			return template;
		}
	}

	/**
	 * Formats the given description.
	 * @param description 		Description to format
	 * @param store				Name-store
	 * @return Formatted description
	 */
	public String format(Description description, NameStore store) {
		// Lookup description template
		final String template = lookup(description.key(), store);

		// Replace tokens
		final StringBuilder text = new StringBuilder(template);
		final Matcher matcher = PATTERN.matcher(text);
		int current = 0;
		while(matcher.find(current)) {
			// Extract token
			final int start = matcher.start();
			final int end = matcher.end();
			final String token = text.substring(start + 1, end - 1);

			// Lookup argument
			final Description.Entry entry = description.get(token);
			if(entry == null) {
				LOG.warn("Unknown description argument: {}", token);
				current = end;
				continue;
			}

			// Ignore empty arguments
			if(StringUtils.EMPTY.equals(entry.argument())) {
				text.delete(start, end);
				current = start;
				continue;
			}

			// Format argument
			final String value = entry.formatter().format(entry.argument(), store);
			if(value == null) {
				LOG.warn("NULL formatted argument: {}", entry.toString());
				current = end;
				continue;
			}

			// Replace token with argument
			text.replace(start, end, value);

			// Recurse to start of replaced token
			current = start;
		}

		// Remove excess spaces
		return text.toString().replace("  ", " ");
	}
}
