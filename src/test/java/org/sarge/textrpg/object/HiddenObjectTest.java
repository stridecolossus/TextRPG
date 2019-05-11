package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class HiddenObjectTest {
	private Actor actor;
	private WorldObject obj;
	private HiddenObject hidden;

	@BeforeEach
	public void before() {
		actor = mock(Actor.class);
		obj = ObjectDescriptor.of("object").create();
		obj.parent(TestHelper.parent());
		hidden = HiddenObject.hide(obj, Percentile.HALF, actor);
	}

	@Test
	public void hide() {
		assertEquals(Percentile.HALF, hidden.visibility());
		assertEquals(actor, hidden.owner());
		assertEquals(obj, hidden.object());
	}

	@Test
	public void destroy() {
		final Parent parent = mock(Parent.class);
		final Contents contents = new Contents();
		when(parent.contents()).thenReturn(contents);
		hidden.parent(parent);
		hidden.destroy();
		assertEquals(false, hidden.isAlive());
		assertEquals(false, obj.isAlive());
	}
}
