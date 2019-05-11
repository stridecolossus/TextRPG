package org.sarge.textrpg.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

/**
 * Repository for the current date-time.
 * @author Sarge
 */
@Repository
@ConditionalOnProperty(name=Clock.START_TIME, matchIfMissing=true)
public class ClockRepository {
	private static final long SECONDS_TO_MILLIS = Duration.ofSeconds(1).toMillis();

	private final Path path;

	/**
	 * Constructor.
	 * @param path File-path
	 */
	public ClockRepository(@Value("${database.location}") Path path) {
		this.path = path.resolve("game.clock");
	}

	/**
	 * Loads the persisted date-time as an <i>epoch</i> value.
	 * @return Current time
	 * @throws IOException if the time cannot be loaded
	 */
//	@Bean(Clock.START_TIME)
	public long load() throws IOException {
		final LocalDateTime date = loadDateTime();
		return date.atZone(Clock.ZONE).toEpochSecond() * SECONDS_TO_MILLIS;
	}

	/**
	 * Loads the current date-time.
	 * @return Date-time
	 * @throws IOException if the date cannot be loaded
	 */
	public LocalDateTime loadDateTime() throws IOException {
		try(final BufferedReader in = Files.newBufferedReader(path)) {
			return LocalDateTime.parse(in.readLine());
		}
	}

	/**
	 * Stores the given date-time to the repository.
	 * @param date Date-time
	 * @throws IOException if the date cannot be written
	 */
	public void storeDateTime(LocalDateTime date) throws IOException {
		try(final BufferedWriter out = Files.newBufferedWriter(path)) {
			out.write(date.toString());
			out.flush();
		}
	}
}
