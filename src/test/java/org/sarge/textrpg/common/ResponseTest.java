package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;

public class ResponseTest {
	@Test
	public void response() {
		final Description description = Description.of("description");
		final Induction.Instance instance = new Induction.Instance(new Induction.Descriptor.Builder().build(), mock(Induction.class));
		final Response response = new Response.Builder()
			.add(description)
			.add("string")
			.induction(instance)
			.display()
			.build();
		assertArrayEquals(new Description[]{description, Description.of("string")}, response.responses().toArray());
		assertEquals(true, response.isDisplayLocation());
		assertEquals(Optional.of(instance), response.induction());
	}

	@Test
	public void compound() {
		final Description description = Description.of("description");
		final var response = new Response.Builder().add(List.of(description)).build();
		assertArrayEquals(new Description[]{description}, response.responses().toArray());
	}

	@Test
	public void ok() {
		assertArrayEquals(new Description[]{Description.of("ok")}, Response.OK.responses().toArray());
		assertEquals(false, Response.OK.isDisplayLocation());
		assertEquals(Optional.empty(), Response.OK.induction());
	}

	@Test
	public void displayLocation() {
		assertArrayEquals(new Description[]{}, Response.DISPLAY_LOCATION.responses().toArray());
		assertEquals(true, Response.DISPLAY_LOCATION.isDisplayLocation());
		assertEquals(Optional.empty(), Response.DISPLAY_LOCATION.induction());
	}

	@Test
	public void empty() {
		assertEquals(0, Response.EMPTY.responses().count());
		assertEquals(false, Response.EMPTY.isDisplayLocation());
		assertEquals(Optional.empty(), Response.EMPTY.induction());
	}

	@Test
	public void invalidEmptyResponse() {
		assertThrows(IllegalArgumentException.class, () -> new Response.Builder().build());
	}
}
