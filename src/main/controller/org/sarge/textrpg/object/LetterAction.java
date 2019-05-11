package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.entity.CharacterEntity;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Pet;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to write and deliver a letter.
 * @author Sarge
 */
@Component
public class LetterAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public LetterAction() {
		super(Flag.LIGHT, Flag.OUTSIDE);
	}

	/**
	 * Writes a letter.
	 * @param actor			Actor
	 * @param address		Address
	 * @param text			Text
	 * @return Response
	 * TODO - requires paper and a pen?
	 * TODO - how to use string parameters? similar to say action?
	 */
	@RequiresActor
	public Response write(Entity actor, String address, String text) {
		final Skill lang = actor.descriptor().race().gear().language();
		final Letter letter = new Letter(address, text, lang);
		letter.parent(actor);
		return Response.OK;
	}

	/**
	 * Gives a letter to the given pet.
	 * @param letter		Letter
	 * @param pet			Pet
	 * @return Response
	 * @throws ActionException if the pet does not belong to the actor or is already carrying a letter
	 */
	@RequiresActor
	public Response give(CharacterEntity actor, @Carried(auto=true) Letter letter, Pet pet) throws ActionException {
		check(actor, pet);
		// TODO - pet.set(letter);
		return Response.OK;
	}

	/**
	 * Orders a pet to delivers its letter to the given entity.
	 * @param actor			Actor
	 * @param pet			Pet
	 * @param entity		Entity to deliver to
	 * @return Response
	 * @throws ActionException
	 */
	@RequiresActor
	public Response deliver(CharacterEntity actor, Pet pet, Entity entity) throws ActionException {
		// TODO - entity should be a Friend
		check(actor, pet);
		pet.deliver(entity);
		return Response.OK;
	}

	/**
	 * Opens a letter.
	 * @param letter Letter
	 * @return Response
	 * @throws ActionException if the letter has already been opened
	 */
	@ActionOrder(3)
	public Response open(@Carried(auto=true) Letter letter) throws ActionException {
		letter.open();
		return Response.OK;
	}

	/**
	 * Posts a letter to the given recipient.
	 * @param letter		Letter
	 * @param recipient		Recipient
	 * @return Response
	 * @throws ActionException if the letter has already been delivered or opened
	 */
	@RequiresActor
	public Response post(Entity actor, @Carried(auto=true) Letter letter, String recipient) throws ActionException {		// TODO - PlayerName recipient
		if(letter.isOpen()) throw ActionException.of("post.letter.opened");
		// TODO - check delivered
		// TODO - PostModel
		return Response.OK;
	}

	/**
	 * Checks the actor is the owner of the given pet.
	 */
	public static void check(CharacterEntity actor, Pet pet) throws ActionException {
		if(!pet.follower().isFollowing(actor)) throw ActionException.of("pet.not.owner");
	}
}
