package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.ActionException;

public class LimboContentsTest {
	private LimboContents limbo;
	private Thing thing;

	@BeforeEach
	public void before() {
		limbo = new LimboContents();
		thing = mock(Thing.class);
	}

	@Test
	public void add() throws ActionException {
		assertThrows(UnsupportedOperationException.class, () -> limbo.add(thing));
	}

	@Test
	public void remove() {
		limbo.remove(thing);
	}
}
