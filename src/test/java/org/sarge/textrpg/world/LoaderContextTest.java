package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class LoaderContextTest {
	private Area area;

	@BeforeEach
	public void before() {
		area = new Area.Builder("area").build();
	}

	@Nested
	class ContextTests {
		private LoaderContext ctx;

		@BeforeEach
		public void before() {
			ctx = new LoaderContext(area, Terrain.FARMLAND, Route.BRIDGE, mock(Faction.class));
		}

		@Test
		public void constructor() {
			assertEquals(area, ctx.area());
			assertEquals(Terrain.FARMLAND, ctx.terrain());
			assertEquals(Route.BRIDGE, ctx.route());
		}

		@Test
		public void junction() {
			final DefaultLocation loc = mock(DefaultLocation.class);
			when(loc.name()).thenReturn("name");
			ctx.add(loc);
			assertEquals(loc, ctx.junction("name"));
		}

		@Test
		public void grid() {
			final Grid grid = mock(Grid.class);
			ctx.add("ref", grid);
			assertEquals(grid, ctx.grid("ref"));
		}
	}

	@Nested
	class StackTests {
		private LoaderContext.Stack stack;

		@BeforeEach
		public void before() {
			stack = new LoaderContext.Stack();
		}

		@Test
		public void constructor() {
			assertEquals(null, stack.parent());
		}

		@Test
		public void push() {
			final LoaderContext wrapper = mock(LoaderContext.class);
			stack.push(wrapper);
			assertEquals(wrapper, stack.parent());
		}

		@Test
		public void pop() {
			stack.push(mock(LoaderContext.class));
			stack.pop();
		}
	}
}
