package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.DurableObject.Descriptor;

public class DurableObjectTest extends ActionTest {
	private DurableObject obj;
	
	@Before
	public void before() {
		obj = new DurableObject(new Descriptor(new ObjectDescriptor("durable"), 2));
	}
	
	@Test
	public void constructor() {
		assertEquals(0, obj.getWear());
		assertEquals(false, obj.isDamaged());
		assertEquals(false, obj.isBroken());
		assertEquals("durable", obj.getDescriptor().getDescriptionKey());
	}
	
	@Test
	public void describe() {
		final Description desc = obj.describe();
		assertEquals("{durable}", desc.get("name"));
		assertEquals("{wear.new}", desc.get("wear"));
	}
	
	@Test
	public void wear() throws ActionException {
		obj.wear();
		assertEquals(true, obj.isDamaged());
		assertEquals(false, obj.isBroken());
		assertEquals(1, obj.getWear());
		assertEquals("{wear.damaged}", obj.describe().get("wear"));

		obj.wear();
		assertEquals(2, obj.getWear());
		assertEquals(true, obj.isBroken());
		assertEquals("{wear.broken}", obj.describe().get("wear"));
	}
	
	@Test
	public void consumeBroken() throws ActionException {
		obj.wear();
		obj.wear();
		expect("durable.object.broken");
		obj.wear();
	}
	
	@Test
	public void repair() throws ActionException {
		obj.wear();
		obj.repair();
		assertEquals(0, obj.getWear());
	}
	
	@Test
	public void repairNotDamaged() throws ActionException {
		expect("repair.not.damaged");
		obj.repair();
	}
}
