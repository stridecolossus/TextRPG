package org.sarge.textrpg.runner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Console connection.
 * @author Sarge
 */
public class ConsoleConnection extends AbstractConnection {
	private final PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));

	/**
	 * Constructor.
	 */
	public ConsoleConnection() {
		super(new BufferedReader(new InputStreamReader(System.in)));
	}

	@Override
	public void write(String str) {
		out.println(str);
		out.flush();
	}

	@Override
	public String toString() {
		return "console";
	}
}
