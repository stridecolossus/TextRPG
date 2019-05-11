package org.sarge.textrpg.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Description;

/**
 * A response is the result of an action consisting of:
 * <ul>
 * <li>none-or-more descriptions accessed using {@link #responses()}</li>
 * <li>a {@link #isDisplayLocation()} flag indicating whether the current location should be rendered</li>
 * <li>an optional {@link Induction.Instance}</li>
 * </ul>
 * @author Sarge
 */
public final class Response extends AbstractEqualsObject {
	/**
	 * Simple response to display the location description.
	 */
	public static final Response DISPLAY_LOCATION = new Builder().display().build();

	/**
	 * Cache of message responses.
	 */
	private static final Map<String, Response> CACHE = new HashMap<>();

	/**
	 * Default <i>ok</i> response.
	 */
	public static final Response OK = of("ok");

	/**
	 * Empty response.
	 */
	public static final Response EMPTY = new Response();

	/**
	 * Creates a simple response.
	 * @param response Response description
	 * @return Simple response
	 */
	public static Response of(Description response) {
		return new Builder().add(response).build();
	}

	/**
	 * Creates a compound response.
	 * @param responses Response descriptions
	 * @return Compound response
	 */
	public static Response of(List<Description> responses) {
		return new Builder().add(responses).build();
	}

	/**
	 * Convenience factory for a simple message response.
	 * @param message Message
	 * @return Simple response
	 */
	public static Response of(String message) {
		return CACHE.computeIfAbsent(message, str -> Response.of(Description.of(str)));
	}

	/**
	 * Creates an induction response.
	 * @param induction Induction descriptor
	 * @return Induction response
	 */
	public static Response of(Induction.Instance induction) {
		return new Builder().induction(induction).build();
	}

	private final List<Description> responses;
	private final boolean display;
	private final Optional<Induction.Instance> induction;

	/**
	 * Constructor.
	 * @param response			Response description
	 * @param induction			Induction descriptor
	 */
	private Response(List<Description> responses, boolean display, Induction.Instance induction) {
		this.responses = List.copyOf(responses);
		this.display = display;
		this.induction = Optional.ofNullable(induction);
		verify();
	}

	/**
	 * Empty constructor.
	 */
	private Response() {
		this.responses = List.of();
		this.display = false;
		this.induction = Optional.empty();
	}

	/**
	 * Verifies this response.
	 */
	private void verify() {
		if(responses.isEmpty() && !display && !induction.isPresent()) {
			throw new IllegalArgumentException("Response cannot be empty");
		}
	}

	/**
	 * @return Response description
	 */
	public Stream<Description> responses() {
		return responses.stream();
	}

	/**
	 * @return Whether to display the current location
	 */
	public boolean isDisplayLocation() {
		return display;
	}

	/**
	 * @return Induction instance
	 */
	public Optional<Induction.Instance> induction() {
		return induction;
	}

	/**
	 * Builder for a response.
	 */
	public static class Builder {
		private final List<Description> responses = new ArrayList<>();
		private Induction.Instance induction;
		private boolean display;

		/**
		 * Adds a response.
		 * @param response Response
		 */
		public Builder add(Description response) {
			responses.add(response);
			return this;
		}

		/**
		 * Adds multiple responses.
		 * @param responses Responses
		 */
		public Builder add(List<Description> responses) {
			this.responses.addAll(responses);
			return this;
		}

		/**
		 * Adds a message response.
		 * @param message Message
		 */
		public Builder add(String message) {
			responses.add(Description.of(message));
			return this;
		}

		/**
		 * Sets the induction for this response.
		 * @param induction Induction instance
		 */
		public Builder induction(Induction.Instance induction) {
			this.induction = induction;
			return this;
		}

		/**
		 * Sets this response to display the current location.
		 */
		public Builder display() {
			display = true;
			return this;
		}

		/**
		 * @return New response
		 */
		public Response build() {
			return new Response(responses, display, induction);
		}
	}
}
