package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.object.RevealObject.Descriptor;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.util.Percentile;

public class RevealObjectTest extends ActionTest {
	private RevealObject reveal;
	private ObjectDescriptor delegate;
	private EventQueue queue;
	
	@Before
	public void before() throws ActionException {
		// Create revealed object
		delegate = new ObjectDescriptor("delegate");
		reveal = new RevealObject(new Descriptor(new Builder("object").reset(42).build(), Collections.singleton(Interaction.MOVE), delegate, true));

		// Add to contents
		final Parent parent = mock(Parent.class);
		when(parent.getContents()).thenReturn(new Contents());
		reveal.setParent(parent);

		// Init reset queue
		queue = new EventQueue();
	}
	
	@Test
	public void constructor() {
		assertEquals(false, reveal.isRevealed());
		assertEquals(true, reveal.isInteraction(Interaction.MOVE));
	}
	
	@Test
	public void reveal() throws ActionException {
		final WorldObject obj = reveal.reveal();
		assertEquals(true, reveal.isRevealed());
		assertNotNull(obj);
		assertEquals(reveal.getParent(), obj.getParent());
		assertEquals(Percentile.ZERO, reveal.getVisibility());
	}

	@Test
	public void reset() throws ActionException {
		final WorldObject obj = reveal.reveal();
		RevealObject.QUEUE.update(42);
		assertEquals(false, reveal.isRevealed());
		assertEquals(true, obj.isDead());
		assertEquals(Percentile.ONE, reveal.getVisibility());
	}
	
	@Test(expected = IllegalStateException.class)
	public void revealAlreadyRevealed() {
		reveal.reveal();
		reveal.reveal();
	}
}
