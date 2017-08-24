package org.sarge.textrpg.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.object.EqualsBuilder;
import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StringUtil;

/**
 * Description of an object or entity.
 * @author Sarge
 */
public final class Description {
	private final String key;
	private final Map<String, String> args;
	private final List<Description> descriptions;
	private final boolean newline;
	private final boolean stop;

	/**
	 * Constructor.
	 * @param key				Description key
	 * @param args				Arguments
	 * @param descriptions		Sub-descriptions
	 * @param newline			Whether to start a new line for this description
	 * @param stop				Whether terminated by a full-stop
	 */
	private Description(String key, Map<String, String> args, List<Description> descriptions, boolean newline, boolean stop) {
		Check.notEmpty(key);
		this.key = key.toLowerCase();
		this.args = new StrictMap<>(args);
		this.descriptions = new ArrayList<>(descriptions);
		this.newline = newline;
		this.stop = stop;
	}

	/**
	 * Convenience constructor for a simple description.
	 * @param key Description key
	 */
	public Description(String key) {
		this(key, Collections.emptyMap(), Collections.emptyList(), true, false);
	}

	/**
	 * Convenience constructor for a description with a single (wrapped) argument.
	 * @param key		Description key
	 * @param name		Argument name
	 * @param value		Value
	 */
	public Description(String key, String name, Object value) {
		this(key, Collections.singletonMap(name, wrap(value)), Collections.emptyList(), true, false);
	}

	/**
	 * @return Description key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Looks up a description argument.
	 * @param name Argument name
	 * @return Argument
	 */
	public String get(String name) {
		return args.get(name);
	}

	/**
	 * @return Sub-descriptions
	 */
	public Stream<Description> getDescriptions() {
		return descriptions.stream();
	}

	/**
	 * @return Whether to start a new line for this description
	 */
	public boolean isNewLine() {
		return newline;
	}

	/**
	 * @return Whether terminated by a full-stop
	 */
	public boolean isFullStop() {
		return stop;
	}

	/**
	 * Convert this description to a notification.
	 * @return Notification
	 */
	public Notification toNotification() {
		return new Notification() {
			@Override
			public Description describe() {
				return Description.this;
			}

			@Override
			public void accept(Handler handler) {
				handler.handle(this);
			}
		};
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.equals(this, that);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}

	/**
	 * Convenience factory for a descriptive list.
	 * @param key				Description key
	 * @param descriptions		Descriptions
	 * @see Builder#buildNone()
	 */
	public static Description create(String key, List<Description> descriptions) {
		final Builder builder = new Builder(key);
		descriptions.stream().forEach(builder::add);
		return builder.buildNone();
	}

	/**
	 * Helper - Wraps an argument.
	 * @param args Argument(s) to wrap
	 * @return Wrapped argument
	 */
	public static String wrap(Object... args) {
		final String value = Arrays.stream(args).map(Object::toString).collect(Collectors.joining("."));
		return StringUtil.wrap(value.toLowerCase(), "{", "}");
	}

	/**
	 * Builder for a description.
	 */
	public static class Builder {
		private String key;
		private final Map<String, String> args = new StrictMap<>();
		private final List<Description> descriptions = new ArrayList<>();
		private boolean newline = true;
		private boolean stop;

		/**
		 * Constructor.
		 * @param key Description key
		 */
		public Builder(String key) {
			Check.notEmpty(key);
			this.key = key;
		}

		/**
		 * Adds an argument.
		 * @param key
		 * @param value
		 */
		public Builder add(String key, Object value) {
			args.put(key.toLowerCase(), value.toString().toLowerCase());
			return this;
		}

		/**
		 * Adds an optional argument.
		 * @param key
		 * @param value
		 */
		public <T> Builder add(String key, Optional<T> value) {
			value.ifPresent(val -> add(key, val));
			return this;
		}

		/**
		 * Adds a token.
		 * @param key		Description key
		 * @param values	One-or-more values identifiers that are full-stop concatenated
		 */
		public Builder wrap(String key, Object... values) {
			add(key, Description.wrap(values));
			return this;
		}

		/**
		 * Adds a sub-description.
		 * @param desc Description
		 */
		public Builder add(Description desc) {
			descriptions.add(desc);
			return this;
		}

		/**
		 * @param newline Whether to start a new line
		 */
		public Builder newline(boolean newline) {
			this.newline = newline;
			return this;
		}

		/**
		 * Terminates this description with a full-stop.
		 */
		public Builder stop() {
			stop = true;
			return this;
		}

		/**
		 * Builds this description.
		 * @return Description
		 */
		public Description build() {
			return new Description(key, args, descriptions, newline, stop);
		}

		/**
		 * Builds this description and appends <b>none</b> to the key if the child descriptions are empty.
		 * @return Description
		 */
		public Description buildNone() {
			if(descriptions.isEmpty() && args.isEmpty()) key += ".none";
			return build();
		}
	}
}
