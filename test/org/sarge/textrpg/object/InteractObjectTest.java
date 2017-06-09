package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.object.InteractObject.Descriptor;
import org.sarge.textrpg.object.WorldObject.Interaction;

public class InteractObjectTest extends ActionTest {
	private InteractObject obj;
	
	@Before
	public void before() {
		final Descriptor descriptor = new Descriptor(new ObjectDescriptor.Builder("object").reset(1).build(), Collections.singleton(Interaction.MOVE), 1, true);
		obj = descriptor.create();
	}
	
	@After
	public void after() {
		Control.QUEUE.reset();
	}
	
	@Test
	public void constructor() {
		assertNotNull(obj.getOpenableModel());
		assertEquals(true, obj.getOpenableModel().isPresent());
		assertEquals(false, obj.getOpenableModel().get().isOpen());
	}
	
	@Test
	public void interact() throws ActionException {
		final Parent parent = ActionTest.createParent();
		obj.interact(Interaction.MOVE, parent);
		assertEquals(true, obj.getOpenableModel().get().isOpen());
		assertEquals(Thing.LIMBO, obj.getParent());
	}
	
	@Test
	public void reset() throws ActionException {
		final Parent parent = super.createParent();
		obj.interact(Interaction.MOVE, parent);
		assertEquals(1, Control.QUEUE.stream().count());
		Control.QUEUE.update(System.currentTimeMillis());
		assertEquals(false, obj.getOpenableModel().get().isOpen());
		assertEquals(parent, obj.getParent());
	}
	
	@Test
	public void interactInvalidAction() throws ActionException {
		expect("interact.invalid.action");
		obj.interact(Interaction.PULL, null);
	}
	
	@Test
	public void interactAlreadyInteracted() throws ActionException {
		obj.interact(Interaction.MOVE, super.createParent());
		expect("interact.done.move");
		obj.interact(Interaction.MOVE, null);
	}
}
