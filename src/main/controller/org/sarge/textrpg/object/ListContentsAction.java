package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.stereotype.Component;

/**
 * Action to list the contents of a parent.
 * @author Sarge
 * @see Contents.EnumerationPolicy
 */
@Component
public class ListContentsAction extends AbstractAction {
	private final ArgumentFormatter.Registry formatters;

	/**
	 * Constructor.
	 * @param formatters Argument formatters
	 */
	public ListContentsAction(ArgumentFormatter.Registry formatters) {
		super(Flag.OUTSIDE);
		this.formatters = notNull(formatters);
	}

	/**
	 * Lists the contents of the given parent.
	 * @param actor			Actor
	 * @param parent		Parent
	 * @return Contents
	 * @throws ActionException if the contents of the given parent cannot be listed
	 * @see Contents.EnumerationPolicy
	 */
	@RequiresActor
	public Response contents(Entity actor, Parent parent) throws ActionException {
		// Check contents can be enumerated
		final Contents contents = parent.contents();
		switch(contents.policy()) {
		case NONE:		throw ActionException.of("list.contents.invalid");
		case CLOSED:	throw ActionException.of("list.contents.closed");
		}

		// Enumerate contents
		final Stream<? extends Thing> stream;
		if(contents.policy() == Contents.EnumerationPolicy.PERCEIVED) {
			stream = contents.stream().filter(actor::perceives);
		}
		else {
			stream = contents.stream();
		}

		// Convert to descriptions
		final List<Description> results = stream.map(t -> t.describe(formatters)).collect(toList());

		// Build response
		if(results.isEmpty()) {
			final String prep = TextHelper.join("contents", contents.placement());
			final Description description = new Description.Builder("list.contents.empty").name(parent.name()).add("prep", prep).build();
			return Response.of(description);
		}
		else {
			return new Response.Builder().add(results).build();
		}
	}
}
