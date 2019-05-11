package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.object.AuctionHouse.Bid;
import org.sarge.textrpg.object.AuctionHouse.DefaultAuctionRepository;
import org.sarge.textrpg.object.AuctionHouse.Entry;
import org.sarge.textrpg.object.AuctionHouse.Post;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class AuctionHouseTest {
	private AuctionHouse house;
	private Actor actor, bidder;
	private ObjectDescriptor obj;

	@BeforeEach
	public void before() {
		house = new AuctionHouse(new DefaultAuctionRepository());
		actor = mock(Actor.class);
		bidder = mock(Actor.class);
		obj = ObjectDescriptor.of("object");
	}

	@Test
	public void constructor() {
		assertNotNull(house.list());
		assertNotNull(house.posts(actor));
		assertNotNull(house.bids(actor));
		assertEquals(0, house.list().count());
		assertEquals(0, house.posts(actor).count());
		assertEquals(0, house.bids(actor).count());
	}

	private Entry init() {
		final Post post = new Post(actor, obj, 2, 3, LocalDateTime.now());
		return house.post(post);
	}

	@Test
	public void post() {
		final Entry entry = init();
		assertNotNull(entry);
		assertNotNull(entry.describe());
		assertEquals(Optional.empty(), entry.bid());
		assertArrayEquals(new Entry[]{entry}, house.posts(actor).toArray());
		assertArrayEquals(new Entry[]{entry}, house.list().toArray());
	}

	@Test
	public void remove() {
		final Entry entry = init();
		house.remove(entry);
		assertEquals(0, house.list().count());
	}

	@Test
	public void bid() throws ActionException {
		final Entry entry = init();
		final Bid bid = new Bid(bidder, 2);
		house.bid(entry, bid);
		assertEquals(Optional.of(bid), entry.bid());
		assertArrayEquals(new Entry[]{entry}, house.bids(bidder).toArray());
	}

	@Test
	public void bidLowerThanMinimum() {
		final Entry entry = init();
		TestHelper.expect("bid.invalid.minimum", () -> house.bid(entry, new Bid(bidder, 1)));
		assertEquals(Optional.empty(), entry.bid());
	}

	@Test
	public void bidLowerThanCurrentBid() throws ActionException {
		final Entry entry = init();
		house.bid(entry, new Bid(bidder, 3));
		TestHelper.expect("bid.invalid.current", () -> house.bid(entry, new Bid(bidder, 2)));
	}

	@Test
	public void bidOwnPost() throws ActionException {
		final Entry entry = init();
		TestHelper.expect("bid.invalid.actor", () -> house.bid(entry, new Bid(actor, 2)));
	}

	@Test
	public void expired() {
		final Entry entry = init();
		assertArrayEquals(new Entry[]{entry}, house.expired(LocalDateTime.now().plusHours(1)).toArray());
		assertEquals(0, house.list().count());
	}
}
