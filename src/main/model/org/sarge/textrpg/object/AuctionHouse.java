package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * An <i>auction house</i> is used to trade objects.
 * @author Sarge
 */
@Service
public class AuctionHouse extends AbstractObject {
	/**
	 * Auction house repository.
	 */
	public interface AuctionRepository {
		/**
		 * @return Auction-house entries
		 */
		Stream<Entry> entries();
		// TODO - filtering

		/**
		 * Enumerates expired entries.
		 * @param now Current date-time
		 * @return Expired entries
		 */
		Stream<Entry> expired(LocalDateTime now);

		/**
		 * Enumerates auction-house posts for the given actor.
		 * @param actor Actor
		 * @return Posts
		 */
		Stream<Entry> posts(Actor actor);

		/**
		 * Enumerates auction-house posts bid on by the given actor.
		 * @param actor Actor
		 * @return Posts
		 */
		Stream<Entry> bids(Actor actor);

		/**
		 * Posts a new auction.
		 * @param entry New entry
		 * @throws IllegalArgumentException if the entry has already been posted
		 */
		void post(Entry entry);

		/**
		 * Removes an entry.
		 * @param entry Entry to remove
		 * @throws IllegalArgumentException if the entry is not active
		 */
		void remove(Entry entry);
	}

	/**
	 * In-memory implementation.
	 */
	@Repository
	public static class DefaultAuctionRepository implements AuctionRepository {
		private final List<Entry> entries = new StrictList<>();

		@Override
		public Stream<Entry> entries() {
			return entries.stream();
		}

		@Override
		public Stream<Entry> expired(LocalDateTime now) {
			synchronized(entries) {
				final Collection<Entry> expired = entries.stream().filter(e -> e.post.expiry.isBefore(now)).collect(toList());
				entries.removeAll(expired);
				return expired.stream();
			}
		}

		@Override
		public Stream<Entry> posts(Actor actor) {
			return entries.stream().filter(e -> e.post.actor == actor);
		}

		@Override
		public Stream<Entry> bids(Actor actor) {
			return entries.stream().filter(e -> e.bid().filter(bid -> bid.actor == actor).isPresent());
		}

		@Override
		public void post(Entry entry) {
			entries.add(entry);
		}

		@Override
		public void remove(Entry entry) {
			entries.remove(entry);
		}
	}

	/**
	 * Auction house posting.
	 */
	public static final class Post extends AbstractEqualsObject {
		private final Actor actor;
		private final ObjectDescriptor descriptor;
		private final int min;
		private final Integer buyout;
		private final LocalDateTime expiry;

		/**
		 * Constructor.
		 * @param actor			Actor
		 * @param descriptor	Descriptor for the object being auctioned
		 * @param start			Starting price
		 * @param buyout		Optional buy-out price
		 * @param duration		Duration
		 */
		public Post(Actor actor, ObjectDescriptor descriptor, int min, Integer buyout, LocalDateTime expiry) {
			this.actor = notNull(actor);
			this.descriptor = notNull(descriptor);
			this.min = oneOrMore(min);
			this.buyout = buyout;
			this.expiry = notNull(expiry);
		}
	}

	/**
	 * Auction house entry.
	 */
	public static final class Entry extends AbstractEqualsObject {
		private final Post post;
		private Bid current;
		private int count;

		/**
		 * Constructor.
		 * @param post Post
		 */
		public Entry(Post post) {
			this.post = notNull(post);
		}

		/**
		 * @return Current highest bid
		 */
		public Optional<Bid> bid() {
			return Optional.ofNullable(current);
		}

		/**
		 * Describes this auction entry.
		 * @return Description
		 */
		public Description describe() {
			return new Description.Builder("auction.house.entry")
				.add("item", post.descriptor.name())
				.add("min", post.min)
				.add("buyout", post.buyout == null ? "auction.buyout.none" : String.valueOf(post.buyout))
				.add("bid", current == null ? "auction.bid.none" : String.valueOf(current.amount))
				.add("count", count)
				// TODO - remaining duration
				.build();
		}
	}

	/**
	 * Auction bid.
	 */
	public static final class Bid extends AbstractEqualsObject {
		private final Actor actor;
		private final int amount;

		/**
		 * Constructor.
		 * @param actor			Actor
		 * @param amount		Bid amount
		 */
		public Bid(Actor actor, int amount) {
			this.actor = notNull(actor);
			this.amount = oneOrMore(amount);
		}
	}

	private final AuctionRepository repository;

	/**
	 * Constructor.
	 * @param repository Auction-house repository
	 */
	public AuctionHouse(AuctionRepository repository) {
		this.repository = notNull(repository);
	}

	/**
	 * Lists auction entries.
	 * @param filter Filter --- TODO
	 * @return Auction entries
	 */
	public Stream<Entry> list() {
		return repository.entries();
	}

	/**
	 * Enumerates auction entries posted by the given actor.
	 * @param actor Actor
	 * @return Auction entries
	 */
	public Stream<Entry> posts(Actor actor) {
		return repository.posts(actor);
	}

	/**
	 * Active auction bids for the given actor.
	 * @param actor Actor
	 * @return Auction entries that the given actor has bid on
	 */
	public Stream<Entry> bids(Actor actor) {
		return repository.bids(actor);
	}

	/**
	 * Posts a new auction.
	 * @param post Auction post
	 * @return New entry
	 */
	public Entry post(Post post) {
		final Entry entry = new Entry(post);
		repository.post(entry);
		return entry;
	}

	/**
	 * Posts a bids for an auction entry.
	 * @param entry		Auction entry
	 * @param bid 		Bid
	 * @throws ActionException if the bid cannot be placed
	 */
	public void bid(Entry entry, Bid bid) throws ActionException {
		// Verify bid
		if(entry.post.actor == bid.actor) throw ActionException.of("bid.invalid.actor");
		if(entry.current == null) {
			if(bid.amount < entry.post.min) throw ActionException.of("bid.invalid.minimum");
		}
		else {
			if(bid.amount <= entry.current.amount) throw ActionException.of("bid.invalid.current");
		}

		// Replace bid
		entry.current = bid;
		++entry.count;
	}

	/**
	 * Removes an auction entry.
	 * @param entry Auction entry
	 */
	public void remove(Entry entry) {
		repository.remove(entry);
	}

	/**
	 * Enumerates expired auctions.
	 * @param now Current date-time
	 * @return Expired auctions
	 */
	public Stream<Entry> expired(LocalDateTime now) {
		return repository.expired(now);
	}
}
