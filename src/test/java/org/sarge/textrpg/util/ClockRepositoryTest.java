package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClockRepositoryTest {
	private ClockRepository repository;

	@BeforeEach
	public void before() throws IOException {
		final Path dir = Files.createTempDirectory("clock.repository.test");
		repository = new ClockRepository(dir);
	}

	@Test
	public void store() throws IOException {
		final LocalDateTime date = LocalDateTime.now();
		repository.storeDateTime(date);
		assertEquals(date, repository.loadDateTime());
		assertEquals(date.atZone(Clock.ZONE).toEpochSecond() * 1000, repository.load());
	}
}
