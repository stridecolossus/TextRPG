package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataSourceTest {
	private DataSource src;

	@BeforeEach
	public void before() {
		src = new DataSource(new File("src/main/resources").toPath());
	}

	@Test
	public void open() throws IOException {
		try(final Reader r = src.open("config/application.yaml")) {
			assertNotNull(r);
		}
	}

	@Test
	public void folder() throws IOException {
		final DataSource folder = src.folder("world");
		folder.open("world.xml");
	}

	@Test
	public void enumerate() {
		assertNotNull(src.enumerate());
		assertEquals(false, src.enumerate().isEmpty());
	}
}
