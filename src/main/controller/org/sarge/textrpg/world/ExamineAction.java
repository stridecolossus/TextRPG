package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.CommandArgumentFactory;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.Control;
import org.sarge.textrpg.object.Intangible;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.parser.DefaultArgumentParser;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.stereotype.Component;

/**
 * Examines an object or entity.
 * @author Sarge
 */
@Component
public class ExamineAction extends AbstractAction {
	private final ArgumentFormatter.Registry formatters;

	/**
	 * Constructor.
	 * @param formatters Argument formatters
	 */
	public ExamineAction(ArgumentFormatter.Registry formatters) {
		super(Flag.OUTSIDE);
		this.formatters = notNull(formatters);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	public boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Decoration pseudo-object.
	 */
	class Decoration implements CommandArgument {
		private final String name;

		/**
		 * Constructor.
		 * @param name Location name
		 */
		private Decoration(String name) {
			this.name = TextHelper.join(name, "decorations");
		}

		@Override
		public String name() {
			return name;
		}
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		final Decoration decoration = new Decoration(actor.location().name());
		final CommandArgumentFactory<?> factory = CommandArgumentFactory.of(decoration);
		return ArgumentParser.Registry.of(Decoration.class, new DefaultArgumentParser<>(factory, actor));
	}

	/**
	 * Examines an object.
	 * @param actor		Actor
	 * @param obj 		Object to describe
	 * @return Object description
	 */
	@RequiresActor
	public Response examine(Entity actor, WorldObject obj) {
		final boolean carried = actor.contents().contains(obj);
		final Description description = obj.describe(carried, formatters);
		return Response.of(description);
	}

	/**
	 * Examines an intangible object.
	 * @param obj Intangible to describe
	 * @return Intangible description
	 */
	@ActionOrder(3)
	public Response examine(Intangible obj) {
		final Description description = obj.describe(formatters);
		return Response.of(description);
	}

	/**
	 * Examines an entity.
	 * @param entity Entity to describe
	 * @return Entity description
	 */
	@ActionOrder(2)
	public Response examine(Entity entity) {
		final Description description = entity.describe(formatters);
		return Response.of(description);
	}

	/**
	 * Examines a control.
	 * @param actor			Actor
	 * @param control 		Control
	 * @return Response
	 */
	@RequiresActor
	@ActionOrder(3)
	public Response examine(Entity actor, Control control) throws ActionException {
		if(control.interaction() == Interaction.EXAMINE) {
			// Examine control
			final Description result = control.interact(actor, Interaction.EXAMINE);
			final Description description = control.describe(formatters);
			return new Response.Builder().add(result).add(description).build();
		}
		else {
			// Delegate
			return examine(actor, (WorldObject) control);
		}
	}

	/**
	 * Examines a decoration.
	 * @param decoration Decoration
	 * @return Response
	 */
	@ActionOrder(3)
	public Response examine(Decoration decoration) {
		return Response.of("examine.decoration");
	}
}
