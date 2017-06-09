package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Openable.Operation;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.object.Container.Descriptor;
import org.sarge.textrpg.object.Container.Placement;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.object.TrackedContents.Limit;

public class ContainerTest extends ActionTest {
	private Container container;
	private WorldObject obj;
	
	@Before
	public void before() throws ActionException {
		final ObjectDescriptor desc = new Builder("container").weight(1).reset(42).build();
		final Limit limit = Limit.number(1);
		container = new Container(new Descriptor(desc, Placement.IN, Openable.UNLOCKABLE, Collections.singletonMap(limit, "limit"), null));
		container.getOpenableModel().get().apply(Operation.OPEN);
		obj = new WorldObject(new Builder("object").weight(2).build());
	}
	
	@Test
	public void constructor() {
		assertNotNull(container.getDescriptor());
		assertEquals("container", container.getDescriptor().getDescriptionKey());
		assertNotNull(container.getContents());
		assertEquals(0, container.getContents().size());
		assertTrue(container.getOpenableModel().isPresent());
		assertEquals(1, container.getWeight());
	}
	
	@Test
	public void describe() {
		final Description desc = container.describe();
		assertEquals("{container}", desc.get("name"));
		assertEquals("{container.open}", desc.get("container.open"));
	}
	
	@Test
	public void add() throws ActionException {
		// Open container and add an object
		obj.setParent(container);
		assertEquals(1, container.getContents().size());
		assertEquals(obj, container.getContents().stream().iterator().next());
		assertEquals(1 + 2, container.getWeight());
		
		// Move object elsewhere and check weight reduced
		final Parent parent = mock(Parent.class);
		when(parent.getContents()).thenReturn(new Contents());
		obj.setParent(parent);
		assertEquals(1, container.getWeight());
	}
	
	@Test
	public void addClosed() throws ActionException {
		container.getOpenableModel().get().apply(Operation.CLOSE);
		expect("container.add.closed");
		obj.setParent(container);
	}
	
	@Test
	public void categoryLimit() {
		final Limit catLimit = Container.categoryLimit(Collections.singleton("ok"));
		assertEquals(true, catLimit.exceeds(obj, container.getContents()));
		final WorldObject valid = new WorldObject(new Builder("object").category("ok").build());
		assertEquals(false, catLimit.exceeds(valid, container.getContents()));
	}
	
	@Test
	public void keyring() throws ActionException {
		container = new Container(new Descriptor(new Builder("name").build(), Placement.IN, null, Collections.emptyMap(), DeploymentSlot.KEYRING));
		obj = new WorldObject(new Builder("key").slot(DeploymentSlot.KEYRING).build());
		obj.setParent(container);
		assertEquals(1, container.getContents().size());
		assertEquals(obj, container.getContents().stream().iterator().next());
	}
	
	@Test
	public void keyringInvalidObject() throws ActionException {
		container = new Container(new Descriptor(new Builder("name").build(), Placement.IN, null, Collections.emptyMap(), DeploymentSlot.KEYRING));
		expect("container.add.keyring");
		obj.setParent(container);
	}
	
	@Test
	public void empty() throws ActionException {
		final Parent parent = super.createParent();
		obj.setParent(container);
		container.empty(parent);
		assertEquals(0, container.getContents().size());
		assertEquals(1, parent.getContents().size());
		assertEquals(obj, parent.getContents().stream().iterator().next());
	}
}
