package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillSet.MutableSkillSet;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.LocationCache;
import org.sarge.textrpg.world.Location;
import org.springframework.stereotype.Component;

/**
 * Action to learn a new skill from a trainer.
 * @author Sarge
 */
@Component
public class LearnAction extends AbstractAction {
	private final Map<Skill, LocationCache<Topic>> trainers = new HashMap<>();

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		// TODO
		return super.parsers(actor);
	}

	/**
	 * Lists skills that can be trained.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if there is no trainer in the current location
	 */
	public Response list(PlayerCharacter actor) throws ActionException {
		// TODO
		return null;
	}

	/**
	 * Learns a new skill.
	 * @param actor		Actor
	 * @param skill		Skill to learn
	 * @return Response
	 * @throws ActionException if there is no suitable trainer in the current location, the actor already has the skill, or does not sufficient XP points
	 */
	@RequiresActor
	public Response learn(PlayerCharacter actor, Skill skill) throws ActionException {
		// Find trainer that teaches this skill
		final LocationCache<Topic> cache = trainers.computeIfAbsent(skill, key -> new LocationCache<>(mapper(skill)));
		cache.find(actor.location()).orElseThrow(() -> ActionException.of("learn.requires.trainer"));

		// Check not already known
		final MutableSkillSet skills = actor.player().skills();
		if(skills.contains(skill)) {
			return Response.of("learn.already.known");
		}

		// Check required skills
		final var required = skills.validate(skill);
		if(!required.isEmpty()) {
			final Function<String, Description> mapper = name -> new Description.Builder("learn.failed.entry").name(name).build();
			final var failed = required.stream().map(Skill::name).map(mapper).collect(toList());
			return new Response.Builder().add("learn.failed.header").add(failed).build();
		}

		// Consume points
		final Transaction tx = actor.settings().transaction(PlayerSettings.Setting.POINTS, skill.power(), "learn.insufficient.points");
		tx.check();
		tx.complete();

		// Learn skill
		skills.add(skill);
		return Response.OK;
	}

	/**
	 * Creates the cache mapper for the given skill.
	 * @param skill Skill
	 * @return Cache mapper
	 */
	private static Function<Location, Optional<Topic>> mapper(Skill skill) {
		return loc -> loc.contents()
			.select(CharacterEntity.class)
			.map(Entity::descriptor)
			.flatMap(EntityDescriptor::topics)
			.filter(topic -> topic.name().equals(skill.name()))
			.findAny();
	}
}
