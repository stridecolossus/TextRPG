package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.entity.FerryController;
import org.sarge.textrpg.world.LoaderContext;

public class ObjectLoaderTest {
	private ObjectLoader loader;
	private DefaultObjectDescriptorLoader descriptorLoader;
	private OpenableLockLoader lockLoader;
	private FerryController controller;
	private LoaderContext ctx;

	@BeforeEach
	public void before() {
		descriptorLoader = mock(DefaultObjectDescriptorLoader.class);
		lockLoader = mock(OpenableLockLoader.class);
		controller = mock(FerryController.class);
		loader = new ObjectLoader(descriptorLoader, lockLoader, controller);
		ctx = mock(LoaderContext.class);
	}

	@Test
	public void loadPredefined() {
		// Register pre-defined descriptor
		final Element xml = new Element.Builder("object").attribute("descriptor", "predefined").build();
		final ObjectDescriptor descriptor = ObjectDescriptor.fixture("predefined");
		when(descriptorLoader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE)).thenReturn(descriptor);

		// Load pre-defined object instance
		final WorldObject obj = loader.load(xml, ctx);
		assertNotNull(obj);
		assertEquals(descriptor, obj.descriptor());
	}

	@Test
	public void loadCustom() {
		final Element xml = Element.of("object");
		when(descriptorLoader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE)).thenReturn(ObjectDescriptor.fixture("object"));
		loader.load(xml, ctx);
	}

	@Test
	public void loadContainer() {
		final Element xml = Element.of("container");
		when(descriptorLoader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE)).thenReturn(new Container.Descriptor(ObjectDescriptor.fixture("container"), "in", LimitsMap.EMPTY));
		final Container container = (Container) loader.load(xml, ctx);
		assertNotNull(container);
		assertEquals(0, container.contents().size());
	}

	@Test
	public void loadContainerContents() {
		final Element child = Element.of("object");
		final Element xml = new Element.Builder("container").child("contents").child("dispenser").attribute("refresh", "1m").add(child).end().end().build();
		when(descriptorLoader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE)).thenReturn(new Container.Descriptor(ObjectDescriptor.fixture("container"), "in", LimitsMap.EMPTY));
		when(descriptorLoader.load(child, ObjectDescriptorLoader.Policy.OBJECT)).thenReturn(ObjectDescriptor.of("object"));
		final Container container = (Container) loader.load(xml, ctx);
		assertNotNull(container);
		assertEquals(1, container.contents().size());
	}

	@Test
	public void loadDispenser() {
		final Element child = Element.of("object");
		final Element xml = new Element.Builder("dispenser").attribute("refresh", "1m").add(child).build();
		final ObjectDescriptor descriptor = ObjectDescriptor.of("object");
		when(descriptorLoader.load(child, ObjectDescriptorLoader.Policy.OBJECT)).thenReturn(descriptor);
		final WorldObject dispenser = loader.load(xml, ctx);
		assertNotNull(dispenser);
	}

	@Test
	public void loadKey() {
		final Element child = new Element.Builder("key").attribute("name", "key").build();
		final Element xml = new Element.Builder("dispenser").attribute("refresh", "1m").add(child).build();
		final ObjectDescriptor descriptor = ObjectDescriptor.of("key");
		when(lockLoader.key("key")).thenReturn(descriptor);
		final WorldObject dispenser = loader.load(xml, ctx);
		assertNotNull(dispenser);
	}
}
