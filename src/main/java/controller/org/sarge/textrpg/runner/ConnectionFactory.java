package org.sarge.textrpg.runner;

import java.net.Socket;

public interface ConnectionFactory {
    Connection create(Socket socket);
}
