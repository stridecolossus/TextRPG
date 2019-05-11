package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.EffectLoader;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.util.RegistryLoader;

public class ArchetypeLoaderTest {
	private ArchetypeLoader loader;
	private ObjectDescriptorLoader descriptorLoader;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		// Create liquid loader
		final RegistryLoader<Liquid> liquids = mock(RegistryLoader.class);
		when(liquids.load(any(Element.class))).thenReturn(Liquid.WATER);

		// Create effects loader
		final EffectLoader effect = mock(EffectLoader.class);
		when(effect.load(any(Element.class))).thenReturn(Effect.NONE);
		when(effect.loadDamage(any(Element.class))).thenReturn(Damage.DEFAULT);

		// Create underlying descriptor loader
		descriptorLoader = mock(ObjectDescriptorLoader.class);

		// Create lock loader
		final OpenableLockLoader lockLoader = mock(OpenableLockLoader.class);
		when(lockLoader.load(any(Element.class))).thenReturn(Openable.Lock.DEFAULT);

		// Create skills registry
		final Registry<Skill> skills = mock(Registry.class);
		when(skills.get(anyString())).thenReturn(Skill.NONE);

		// Create loader
		loader = new ArchetypeLoader();
		loader.setDescriptorLoader(descriptorLoader);
		loader.setLockLoader(lockLoader);
		loader.setLiquidLoader(liquids);
		loader.setEffectLoader(effect);
		loader.setSkillsRegistry(skills);
	}

	private void init(Element xml) {
		when(descriptorLoader.load(xml, ObjectDescriptorLoader.Policy.ANY)).thenReturn(ObjectDescriptor.of(xml.name()));
	}

	/**
	 * Makes the object descriptor a resetable fixture.
	 */
	private void resetable(Element xml) {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("resetable").reset(Duration.ofSeconds(1)).build();
		when(descriptorLoader.load(xml, ObjectDescriptorLoader.Policy.ANY)).thenReturn(descriptor);
	}

	@Test
	public void object() {
		final Element xml = Element.of("object");
		init(xml);
		assertNotNull(loader.load(xml, ObjectDescriptorLoader.Policy.ANY));
	}

	@Test
	public void receptacle() {
		final Element xml = new Element.Builder("receptacle").attribute("level", 42).build();
		init(xml);
		final Receptacle.Descriptor result = (Receptacle.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertEquals(Liquid.WATER, result.liquid());
	}

	@Test
	public void light() {
		final Element xml = new Element.Builder("light")
			.attribute("light", "1")
			.attribute("smoke", "2")
			.attribute("lifetime", "3s")
			.attribute("type", "lantern")
			.build();
		init(xml);
		final Light.Descriptor light = (Light.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertNotNull(light);
		assertEquals(Light.Type.LANTERN, light.type());
	}

	@Test
	public void portal() {
		final Element xml = Element.of("portal");
		resetable(xml);
		final var result = (Portal.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertEquals(true, result.isFixture());
		assertEquals(true, result.isResetable());
	}

	@Test
	public void gate() {
		// TODO
	}

	@Test
	public void lamp() {
		// TODO
	}

	@Test
	public void readable() {
		final Element xml = new Element.Builder("readable").attribute("lang", "skill").build();
		init(xml);
		final var result = (Readable.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertEquals(Skill.NONE, result.language());
	}

	@Test
	public void book() {
		final Element xml = new Element.Builder("book")
			.attribute("lang", "skill")
			.child("chapter")
				.attribute("title", "title")
				.attribute("text", "text")
				.end()
			.build();
		init(xml);
		final var result = (Readable.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertEquals(1, result.sections().size());
		assertEquals(Skill.NONE, result.language());
	}

	@Test
	public void durable() {
		final var xml = new Element.Builder("durable").attribute("durability", "75").build();
		init(xml);
		loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
	}

	@Test
	public void rope() {
		final var xml = new Element.Builder("rope")
			.attribute("length", 42)
			.attribute("magical", true)
			.attribute("durability", 1)
			.build();
		init(xml);
		final var result = (Rope.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertEquals(42, result.length());
		assertEquals(true, result.isMagical());
	}

	@Test
	public void container() {
		final var xml = new Element.Builder("container").attribute("content-slot", Slot.KEYRING).build();
		init(xml);
		final var result = (Container.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertNotNull(result);
	}

	@Test
	public void utensil() {
		final var xml = new Element.Builder("utensil").attribute("capacity", 42).build();
		init(xml);
		final var result = (Utensil.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertNotNull(result);
	}

	@Test
	public void food() {
		final var xml = new Element.Builder("food").attribute("nutrition", 2).attribute("cook", 3).build();
		init(xml);
		final var result = (Food.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertEquals(2, result.nutrition());
	}

	@Test
	public void weapon() {
		// Build XML
		final Element xml = new Element.Builder("weapon")
			.attribute("speed", 42)
			.attribute("ammo", Ammo.Type.ARROW)
			.attribute("skill", "skill")
			.attribute("durability", 1)
			.child("damage")
				.attribute("type", Damage.Type.CRUSHING)
				.end()
			.build();

		// Init descriptor
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("weapon").slot(Slot.MAIN).build();
		when(descriptorLoader.load(xml, ObjectDescriptorLoader.Policy.ANY)).thenReturn(descriptor);

		// Load weapon
		final Weapon.Descriptor weapon = (Weapon.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertEquals(42, weapon.speed());
		assertEquals(Skill.NONE, weapon.skill());
		assertEquals(Optional.of(Ammo.Type.ARROW), weapon.ammo());
		assertEquals(Damage.DEFAULT, weapon.damage());
	}

	@Test
	public void sheath() {
		final Element xml = new Element.Builder("sheath").attribute("sheath-category", "weapon").build();
		init(xml);
		assertNotNull(loader.load(xml, ObjectDescriptorLoader.Policy.ANY));
	}

	@Test
	public void vehicle() {
		// TODO
	}

	@Test
	public void ferry() {
		// TODO
	}

	@Test
	public void furniture() {
		// TODO
	}

	@Test
	public void window() {
		final Element xml = new Element.Builder("window").attribute("drape", "curtains").attribute("view", "view").build();
		resetable(xml);
		final Window.Descriptor window = (Window.Descriptor) loader.load(xml, ObjectDescriptorLoader.Policy.ANY);
		assertEquals("curtains", window.drape());
	}
}
