package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.sarge.lib.collection.Cache;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Readable.Section;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextMangler;
import org.sarge.textrpg.world.LightLevelProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to read something.
 * @author Sarge
 * @see TextMangler
 */
@Component
@RequiresActor
public class ReadAction extends AbstractAction {
	private final Skill advanced;
	private final LightLevelProvider light;

	private ArgumentFormatter low = ArgumentFormatter.PLAIN;
	private ArgumentFormatter high = ArgumentFormatter.PLAIN;
	private int max;

	/**
	 * Constructor.
	 * @param advanced 		Advanced languages skill
	 * @param light			Light-level provider
	 */
	public ReadAction(@Value("#{skills.get('advanced.languages')}") Skill advanced, LightLevelProvider light) {
		super(Flag.LIGHT, Flag.OUTSIDE);
		this.advanced = notNull(advanced);
		this.light = notNull(light);
	}

	/**
	 * Sets the formatter for a low-language skill.
	 * @param low Low language skill-score
	 */
	@Autowired
	public void setLowLanguageFormatter(@Value("${mangler.low}") Percentile low) {
		this.low = mangler(low);
	}

	/**
	 * Sets the formatter for a high-language skill.
	 * @param high High language skill-score
	 */
	@Autowired
	public void setHighLanguageFormatter(@Value("${mangler.high}") Percentile high) {
		this.high = mangler(high);
	}

	/**
	 * Sets the maximum cache size for mangled text
	 * @param max Maximum cache size
	 */
	@Autowired
	public void setCacheSize(@Value("${mangler.cache.size}") int max) {
		this.max = oneOrMore(max);
	}

	/**
	 * Creates a text-mangler formatter for the given language score.
	 * @param score Language score
	 * @return Text-mangler formatter
	 */
	private ArgumentFormatter mangler(Percentile score) {
		// Create mangler
		final TextMangler mangler = new TextMangler(score);

		// Create cache for mangled text
		final var cache = new Cache.Builder<String, String>()
			.constraint(Cache.Constraint.size(max))
			.policy(Cache.EvictionPolicy.LEAST_RECENTLY_USED)
			.factory(mangler::mangle)
			.build();

		// Create formatter
		return (arg, store) -> cache.get(store.get(arg));
	}

	/**
	 * Reads a letter.
	 * @param actor			Actor
	 * @param letter		Letter
	 * @return Response
	 * @throws ActionException if the letter has not been opened or the actor does not possess the required language
	 */
	@ActionOrder(2)
	public Response read(Entity actor, Letter letter) throws ActionException {
		if(!letter.isOpen()) throw ActionException.of("letter.not.opened");
		final Section section = letter.descriptor().sections().get(0);
		final ArgumentFormatter formatter = formatter(actor, letter);
		return Response.of(section.describe(formatter));
	}

	/**
	 * Reads a readable object or lists the table-of-contents of a book.
	 * @param actor			Actor
	 * @param readable		Readable
	 * @return Response
	 * @throws ActionException if the actor cannot understand the language
	 */
	public Response read(Entity actor, Readable readable) throws ActionException {
		final ArgumentFormatter formatter = formatter(actor, readable);
		final Readable.Descriptor descriptor = readable.descriptor();
		if(descriptor.isBook()) {
			// Enumerate chapters
			final AtomicInteger index = new AtomicInteger(1);
			final Function<String, Description> mapper = title -> new Description.Builder("read.book.index")
				.add("index", index.getAndIncrement())
				.add("title", title, formatter)
				.build();

			// Build table of contents
			final Response.Builder builder = new Response.Builder();
			builder.add(new Description("read.book.title", readable.name()));
			descriptor.sections().stream().map(Section::title).map(mapper).forEach(builder::add);
			return builder.build();
		}
		else {
			// Read default readable
			final boolean reveal = isRevealed();
			final Predicate<Section> visible = section -> !section.isHidden() || reveal;
			final var text = descriptor.sections().stream().filter(visible).map(section -> section.describe(formatter)).collect(toList());
			// TODO - empty
			return Response.of(text);
		}
	}

	/**
	 * @return Whether to reveal hidden text
	 */
	private boolean isRevealed() {
		// TODO - weather
		return !light.isDaylight();
	}

	/**
	 * Reads the specified chapter of a book.
	 * @param actor			Actor
	 * @param book			Book
	 * @param index			Chapter index 1..n
	 * @return Response
	 * @throws ActionException if the actor does not possess the required language or the index is not valid
	 */
	public Response read(Entity actor, Readable book, Integer index) throws ActionException {
		// Check index
		if(!book.descriptor().isBook()) throw ActionException.of("read.not.book");
		final var sections = book.descriptor().sections();
		if((index < 1) || (index > sections.size())) throw new ActionException(new Description("read.invalid.index", book.name()));

		// Read chapter
		final ArgumentFormatter formatter = formatter(actor, book);
		final Section section = sections.get(index - 1);
		final Description description = new Description.Builder("read.book.chapter")
			.add("title", section.title(), formatter)
			.add("text", section.text(), formatter)
			.build();

		// Build response
		return Response.of(description);
	}

	/**
	 * Determines the text formatter for the given actor.
	 * @param actor			Actor
	 * @param readable		Readable
	 * @return Text formatter
	 * @throws ActionException if the actor does not possess the required language
	 */
	private ArgumentFormatter formatter(Entity actor, Readable readable) throws ActionException {
		// Lookup actor language skills
		final boolean lang = actor.skills().contains(readable.descriptor().language());
		final boolean adv = actor.skills().contains(advanced);

		// Determine text formatter for this actor
		if(lang) {
			if(adv) {
				// Text rendered fully
				return ArgumentFormatter.TOKEN;
			}
			else {
				// High language skill
				return high;
			}
		}
		else {
			if(adv) {
				// Low language skill
				return low;
			}
			else {
				// Unknown language
				throw new ActionException(new Description("read.unknown.language", readable.name()));
			}
		}
	}
}
