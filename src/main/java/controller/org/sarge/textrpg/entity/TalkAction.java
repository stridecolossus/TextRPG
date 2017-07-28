package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Topic;

/**
 * Action to talk to a {@link CharacterEntity}.
 * @author Sarge
 * @see Entity#getTopic(String)
 */
public class TalkAction extends AbstractAction {
	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Lists conversation topics.
	 */
	@SuppressWarnings("unused")
	public ActionResponse listTopics(ActionContext ctx, Entity actor, Entity entity) throws ActionException {
		final List<Description> topics = entity.getTopics().map(Topic::getName).map(Description::new).collect(toList());
		return new ActionResponse(Description.create("talk.list.topics", topics));
	}

	/**
	 * Discusses a specific topic.
	 */
	@SuppressWarnings("unused")
	public void discussTopic(ActionContext ctx, Entity actor, Entity entity, Topic topic) throws ActionException {
		if(!entity.getTopics().anyMatch(t -> t == topic)) throw new ActionException("talk.unknown.topic");
		topic.getScript().execute(actor);
	}
}
