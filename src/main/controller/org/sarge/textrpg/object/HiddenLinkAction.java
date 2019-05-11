package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.parser.DefaultArgumentParser;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.HiddenLink;
import org.sarge.textrpg.world.Terrain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to open a {@link HiddenLink}.
 * @author Sarge
 */
@Component
public class HiddenLinkAction extends AbstractAction {
	private final Duration forget;

	/**
	 * Constructor.
	 * @param forget Forget duration
	 */
	public HiddenLinkAction(@Value("${hiddenlink.forget}") Duration forget) {
		super(Flag.LIGHT);
		this.forget = notNull(forget);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		final var hidden = StreamUtil.select(HiddenLink.class, actor.location().exits().stream().map(Exit::link)).collect(toList());
		return ArgumentParser.Registry.of(HiddenLink.class, new DefaultArgumentParser<>(ignore -> hidden.stream(), actor));
	}

	/**
	 * Reveals a hidden link.
	 * @param actor			Actor
	 * @param name			Name of the link to reveal
	 * @throws ActionException if the hidden link is already visible to the given actor
	 */
	@RequiresActor
	@ActionOrder(2)
	public Response reveal(PlayerCharacter actor, HiddenLink link) throws ActionException {
		final Thing hidden = link.controller().get();
		if(actor.perceives(hidden)) throw new ActionException(new Description("reveal.already.known", hidden.name()));
		actor.hidden().add(hidden, forget);
		return Response.OK;
	}
}
