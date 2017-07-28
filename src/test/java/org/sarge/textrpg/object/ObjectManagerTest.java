package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;

public class ObjectManagerTest extends ActionTest {
	private ObjectDescriptor descriptor;
	private Parent parent;
	
	@SuppressWarnings("unused")
	@Before
	public void before() {
		parent = createParent();
		descriptor = new ObjectDescriptor("object");
		new ObjectManager(LootFactory.object(descriptor, 1), parent, 2, 1);
	}
	
	@Test
	public void constructor() {
		assertEquals(2, parent.getContents().stream().count());
		assertNotNull(ObjectManager.QUEUE);
		assertEquals(0, ObjectManager.QUEUE.stream().count());
	}
	
	@Test
	public void remove() {
		// Move one of the contents
		final Thing obj = parent.getContents().stream().iterator().next();
		obj.setParentAncestor(createParent());
		assertEquals(1, parent.getContents().stream().count());
		
		// Check refresh event generated
		assertEquals(1, ObjectManager.QUEUE.stream().count());
		
		// Advance time and check new contents added
		ObjectManager.QUEUE.execute(1);
		assertEquals(2, parent.getContents().stream().count());
	}
	
	@Test
	public void removeOther() {
		final WorldObject other = new WorldObject(new ObjectDescriptor("other"));
		other.setParentAncestor(parent);
		other.setParentAncestor(createParent());
		assertEquals(2, parent.getContents().stream().count());
		assertEquals(0, ObjectManager.QUEUE.stream().count());
	}
}
