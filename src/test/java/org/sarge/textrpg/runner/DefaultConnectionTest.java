package org.sarge.textrpg.runner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultConnectionTest {
	private Connection con;
	private Socket socket;
	private InputStream in;
	private OutputStream out;

	@BeforeEach
	public void before() throws IOException {
		socket = mock(Socket.class);

		in = mock(InputStream.class);
		when(socket.getInputStream()).thenReturn(in);

		out = mock(OutputStream.class);
		when(socket.getOutputStream()).thenReturn(out);

		con = new DefaultConnection(socket);
	}

	@Test
	public void write() throws IOException {
		con.write("command");
	}

	@Test
	public void close() throws IOException {
		con.close();
		verify(socket).close();
	}
}
