package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.object.Light.Descriptor;
import org.sarge.textrpg.object.Light.Operation;
import org.sarge.textrpg.object.Light.Type;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.util.Percentile;

public class LightTest extends ActionTest {
	private static final Emission LIGHT = Emission.light(Percentile.HALF);
	private Light light;
	private Receptacle oil;

	private static Light create(Type type) {
		final ObjectDescriptor obj = new Builder("light").emission(LIGHT).build();
		final Descriptor descriptor = new Descriptor(obj, 3, type);
		return new Light(descriptor);
	}

	@Before
	public void before() {
		light = create(Type.LANTERN);
		light.setParentAncestor(actor);
		oil = new Receptacle(new Receptacle.Descriptor(new ObjectDescriptor("oil"), Liquid.OIL, 4, false));
	}

	@Test
	public void constructor() {
		assertEquals(3, light.getLifetime());
		assertEquals(false, light.isLit());
		assertEquals(false, light.isCovered());
		assertEquals(Optional.empty(), light.getEmission(Emission.Type.LIGHT));
		assertEquals("light", light.getDescriptor().getDescriptionKey());
	}

	@Test
	public void lampPost() {
		final Light lamp = new Light(new Descriptor(new Builder("lamp,post").emission(LIGHT).build(), 1, Type.LAMP_POST));
		Light.toggleLampPosts(false);
		assertEquals(true, lamp.isLit());
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void descriptorMissingLightEmission() {
		new Light(new Descriptor(new ObjectDescriptor("invalid"), 3, Type.GENERAL));
	}

	@Test
	public void describe() {
		final Description desc = light.describe();
		assertEquals("{light}", desc.get("name"));
		assertEquals("3", desc.get("light.lifetime"));
		assertEquals(null, desc.get("lit"));
		assertEquals(null, desc.get("covered"));
	}

	@Test
	public void light() throws ActionException {
		light.execute(Operation.LIGHT);
		assertEquals(true, light.isLit());
		assertEquals(Optional.of(LIGHT), light.getEmission(Emission.Type.LIGHT));
		assertEquals("{light.lit}", light.describe().get("light.lit"));
	}

	@Test
	public void lightAlreadyLit() throws ActionException {
		light.execute(Operation.LIGHT);
		expect("light.already.lit");
		light.execute(Operation.LIGHT);
	}

	@Test
	public void snuff() throws ActionException {
		light.execute(Operation.LIGHT);
		light.execute(Operation.SNUFF);
		assertEquals(false, light.isLit());
		assertEquals(Optional.empty(), light.getEmission(Emission.Type.LIGHT));
	}

	@Test
	public void snuffNotLit() throws ActionException {
		expect("snuff.not.lit");
		light.execute(Operation.SNUFF);
	}

	@Test
	public void cover() throws ActionException {
		light.execute(Operation.COVER);
		assertEquals(true, light.isCovered());
		assertEquals("{light.covered}", light.describe().get("light.covered"));
	}

	@Test
	public void coverAlreadyCovered() throws ActionException {
		light.execute(Operation.COVER);
		expect("cover.already.covered");
		light.execute(Operation.COVER);
	}

	@Test
	public void coverNotLantern() throws ActionException {
		light = create(Type.GENERAL);
		light.setParentAncestor(actor);
		expect("cover.not.lantern");
		light.execute(Operation.COVER);
	}

	@Test
	public void expiry() throws ActionException {
		light.execute(Operation.LIGHT);
		assertEquals(2, Light.QUEUE.stream().count());
        Light.QUEUE.execute(Light.QUEUE.time() + 3);
		assertEquals(false, light.isLit());
		assertEquals(0, light.getLifetime());
		assertEquals(0, Light.QUEUE.stream().count());
	}

	@Test
	public void expiryCancelled() throws ActionException {
		light.execute(Operation.LIGHT);
		Light.QUEUE.execute(Light.QUEUE.time() + 2);
		light.execute(Operation.SNUFF);
		assertEquals(false, light.isLit());
		assertEquals(3 - 2, light.getLifetime());
	}

	@Test
	public void destroy() throws ActionException {
		light.execute(Operation.LIGHT);
		light.destroy();
		assertEquals(0, light.getLifetime());
		assertEquals(false, light.isLit());
	}

	@Test
	public void fill() throws ActionException {
		// Consume light and then re-fuel from an oil receptacle
		light.empty();
		light.fill(oil);
		assertEquals(3, light.getLifetime());

		// Repeat to consume remainder of receptacle contents
		light.empty();
		light.fill(oil);
		assertEquals(1, light.getLifetime());
	}

	@Test
	public void fillAlreadyFull() throws ActionException {
		expect("light.fill.full");
		light.fill(oil);
	}

	@Test
	public void fillEmptyReceptacle() throws ActionException {
		light.empty();
		oil.empty();
		expect("light.fill.empty");
		light.fill(oil);
	}

	@Test
	public void fillNotLantern() throws ActionException {
		light = create(Type.CAMPFIRE);
		light.empty();
		expect("fill.not.lantern");
		light.fill(oil);
	}

	@Test
	public void fillInvalidReceptacle() throws ActionException {
		light.empty();
		expect("light.fill.oil");
		light.fill(Receptacle.WATER);
	}
}
