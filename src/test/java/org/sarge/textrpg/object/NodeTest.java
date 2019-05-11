package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.ActionException;

public class NodeTest {
	private static final Duration DURATION = Duration.ofMinutes(1);

	private Node node;

	@BeforeEach
	public void before() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("node").reset(DURATION).decay(DURATION).fixture().build();
		node = new Node(new Node.Descriptor(descriptor, "res"));
	}

	@Test
	public void constructor() {
		assertEquals("node", node.name());
		assertEquals(false, node.isCollected());
		assertEquals(true, node.isAlive());
		assertEquals(true, node.descriptor().isFixture());
		assertEquals(true, node.descriptor().isResetable());
		assertEquals("res", node.descriptor().resource());
	}

	@Test
	public void collect() throws ActionException {
		node.collect();
		assertEquals(true, node.isCollected());
		assertEquals(false, node.isAlive());
	}

	@Test
	public void reset() throws ActionException {
		node.collect();
		node.reset();
		assertEquals(false, node.isCollected());
		assertEquals(true, node.isAlive());
	}
}
