package org.sarge.textrpg.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.entity.Induction;

/**
 * Response returned by an {@link AbstractAction}.
 * @author Sarge
 */
public final class ActionResponse {
	/**
	 * Default response.
	 */
	public static final ActionResponse OK = new ActionResponse("ok");
	
	private final List<Description> descriptions;
	private final Optional<Induction> induction;
	private final long duration;
	private final boolean repeat;
	
	/**
	 * Constructor.
	 * @param description Response description
	 */
	public ActionResponse(Description description) {
		this(Collections.singletonList(description), null, 0, false);
	}

	/**
	 * Constructor for a compound response.
	 * @param descriptions Response descriptions
	 */
	public ActionResponse(List<Description> descriptions) {
		this(descriptions, null, 0, false);
	}

	/**
	 * Convenience constructor for a simple response.
	 * @param name Response key
	 */
	public ActionResponse(String name) {
		this(new Description(name));
	}

	/**
	 * Constructor for an action with an induction.
	 * @param name			Response key
	 * @param induction		Induction
	 * @param duration		Duration (ms)
	 */
	public ActionResponse(String name, Induction induction, long duration) {
		this(name, induction, duration, false);
	}

	public ActionResponse(String name, Induction induction, long duration, boolean repeat) {
		this(Collections.singletonList(new Description(name)), induction, duration, repeat);
		Check.notNull(induction);
		Check.oneOrMore(duration);
	}

	// TODO - too many ctors
	public ActionResponse(List<Description> descriptions, Induction induction, long duration, boolean repeat) {
		this.descriptions = new ArrayList<>(descriptions);
		this.induction = Optional.ofNullable(induction);
		this.duration = duration;
		this.repeat = repeat;
	}

	/**
	 * @return Response description
	 */
	public Stream<Description> getDescriptions() {
		return descriptions.stream();
	}

	/**
	 * @return Induction
	 */
	public Optional<Induction> getInduction() {
		return induction;
	}

	/**
	 * @return Induction duration (ms)
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @return Whether the induction is repeating
	 */
	public boolean isRepeating() {
		return repeat;
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.equals(this, that);
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
