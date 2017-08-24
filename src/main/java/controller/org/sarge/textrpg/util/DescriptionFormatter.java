package org.sarge.textrpg.util;

import java.util.function.Function;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Description;

/**
 * Formatter for a {@link Description}.
 * @author Sarge
 * TODO - break this into stages?
 */
public class DescriptionFormatter {
	private final Function<String, String> mapper;
	private final TokenReplacer replacer;

	/**
	 * Constructor.
	 * @param mapper	Maps description keys to text
	 */
	public DescriptionFormatter(Function<String, String> mapper) {
		Check.notNull(mapper);
		this.mapper = mapper;
		this.replacer = new TokenReplacer(mapper);
	}

	/**
	 * Formats a description.
	 * @param description Description to format
	 * @return Formatted description string
	 */
	public String format(Description description) {
		final StringBuilder sb = new StringBuilder();
		format(description, sb);
		return sb.toString();
	}

	/**
	 * Recursively formats a description to the given builder.
	 * @param description	Description to format
	 * @param sb			String builder
	 */
	private void format(Description description, StringBuilder sb) {
		// Add line-separator
		if(sb.length() > 0) {
			if(description.isNewLine()) {
				sb.append("\n");
			}
			else {
				sb.append(" ");
			}
		}
		
		// Lookup description from repository
		final String key = description.getKey();
		String text = mapper.apply(key);
		
		// Replace tokens with description arguments
		final TokenReplacer r = new TokenReplacer(description::get);
		text = r.replace(text);
		
		// Recursively replace arguments with descriptions
		text = replacer.replace(text, true);
		
		// Add description
		sb.append(text);
		
		// Recurse to children
		description.getDescriptions().forEach(desc -> format(desc, sb));
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
