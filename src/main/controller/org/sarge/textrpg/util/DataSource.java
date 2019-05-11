package org.sarge.textrpg.util;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Application data-source.
 * @author Sarge
 */
public class DataSource extends AbstractEqualsObject {
	private final Path root;

	/**
	 * Constructor.
	 * @param root Data-source root directory
	 */
	public DataSource(Path root) {
		this.root = notNull(root);
	}

	/**
	 * Resolves a sub-folder of this data-source.
	 * @param folder Folder name
	 * @return Sub-folder data-source
	 */
	public DataSource folder(String folder) {
		return new DataSource(root.resolve(Paths.get(folder)));
	}

	/**
	 * Opens a file.
	 * @param filename Filename
	 * @return Reader
	 * @throws IOException if the file cannot be opened
	 */
	public BufferedReader open(String filename) throws IOException {
		return Files.newBufferedReader(root.resolve(filename));
	}

	/**
	 * Enumerates files in this folder.
	 * @return Files
	 */
	public List<String> enumerate() {
		return Arrays.stream(root.toFile().listFiles()).map(File::getName).collect(toList());
	}
}
