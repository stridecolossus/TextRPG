package org.sarge.textrpg.object;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Event;
import org.sarge.textrpg.common.EventQueue;

/**
 * Auction house model.
 * @author Sarge
 * TODO
 * - responses should be by letter/post-box
 */
public class AuctionHouse {
	/**
	 * Auction posting.
	 */
	public class Post {
		private final Actor poster;
		private final ObjectDescriptor descriptor;
		private final int min;
		private final long time;
		private final Event expiry = this::complete;
		
		private int bid;
		private Actor bidder;
		
		/**
		 * Constructor.
		 * @param poster			Poster
		 * @param descriptor		Object descriptor
		 * @param min				Minimum price
		 * @param time				Duration
		 */
		private Post(Actor poster, ObjectDescriptor descriptor, int min, long time) {
			Check.notNull(poster);
			Check.notNull(descriptor);
			Check.oneOrMore(min);
			Check.oneOrMore(time);
			this.poster = poster;
			this.descriptor = descriptor;
			this.min = min;
			this.time = time;
		}

		/**
		 * Describes this post.
		 * @return Description
		 */
		public Description describe() {
			final Description.Builder builder = new Description.Builder("auction.post")
				.add("poster", poster)
				.add("descriptor", descriptor)
				.add("min", min)
				.add("time", time);
			
			if(bidder != null) {
				builder.add("bidder", bidder);
				builder.add("bid", bid);
			}
			
			return builder.build();
		}
		
		/**
		 * Completes this auction post on expiry.
		 */
		private void complete() {
			if(bidder == null) {
				// Auction expired, mail object to poster
				// TODO
			}
			else {
				// Mail object to wining bidder
				// TODO
			}
		}
		
		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}

	// TODO - should be mailed
//	/**
//	 * Notification for a returned sum of money.
//	 */
//	public class RestoreNotification implements Notification {
//		private final String name;
//		private final int value;
//
//		/**
//		 * Constructor.
//		 * @param name		Notification identifier
//		 * @param value		Sum to return
//		 */
//		private RestoreNotification(String name, int value) {
//			Check.notEmpty(name);
//			Check.oneOrMore(value);
//			this.name = name;
//			this.value = value;
//		}
//
//		/**
//		 * @return Sum to return
//		 */
//		public int getValue() {
//			return value;
//		}
//
//		@Override
//		public Description describe() {
//			return new Description("value", value);
//		}
//
//		@Override
//		public String toString() {
//			return ToString.toString(this);
//		}
//	}
	
	private final List<Post> posts = new StrictList<>();
	
	/**
	 * @return Auction postings
	 */
	public Stream<Post> stream() {
		return posts.stream();
	}

	/**
	 * Creates a new auction posting.
	 * @param actor			Actor
	 * @param queue			Queue
	 * @param obj			Object being auctioned
	 * @param min			Minimum bid
	 * @param time			Expiry duration (ms)
	 * @return Posting
	 * @throws ActionException if the object is damaged or is not owned by the given actor
	 */
	public Post post(Actor actor, EventQueue queue, WorldObject obj, int min, long time) throws ActionException {
		// Check can auction this object
		if(obj.isDamaged()) throw new ActionException("post.damaged.object");
		if(obj.getOwner() != actor) throw new ActionException("post.not.carried");
		
		// Create new auction post
		final Post post = new Post(actor, obj.descriptor, min, time);
		posts.add(post);

		// Register expiry event
		queue.add(post.expiry, time);

		// Remove from inventory
		obj.destroy();
		
		return post;
	}
	
	/**
	 * Cancels an auction posting.
	 * @param actor		Actor
	 * @param queue		Queue
	 * @param post		Posting to cancel
	 * @throws ActionException if the actor is not the original poster
	 */
	public void cancel(Actor actor, EventQueue queue, Post post) throws ActionException {
		if(post.poster != actor) throw new ActionException("cancel.not.poster");
		restore(post);
		// TODO
		//queue.cancel(post.expiry);
		posts.remove(post);
		// TODO
		//actor.notify(new RestoreNotification("post.restore.cancelled", post.min));
	}

	/**
	 * Bids for an auction posting.
	 * @param actor		Actor
	 * @param post		Posting to bid on
	 * @param bid		Bid offer
	 * @throws ActionException if the bid is lower than the current bid or the given actor is the original poster
	 */
	public void bid(Actor actor, Post post, int bid) throws ActionException {
		// Check offer
		if(post.poster == actor) throw new ActionException("offer.own.post");
		if(bid <= post.bid) throw new ActionException("offer.invalid.value");

		// Return previous offer
		restore(post);
		
		// Register new offer
		post.bid = bid;
		post.bidder = actor;
	}
	
	/**
	 * Buys the given post.
	 * @param actor
	 * @param queue
	 * @param post
	 */
	public void buyout(Actor actor, EventQueue queue, Post post) {
		// TODO
	}

	/**
	 * Restores a previous bid.
	 * @param post Post to restore
	 */
	private void restore(Post post) {
		if(post.bidder != null) {
			// TODO
			//post.bidder.notify(new RestoreNotification("post.restore.cancelled", post.bid));
			post.bidder = null;
		}
	}
}
