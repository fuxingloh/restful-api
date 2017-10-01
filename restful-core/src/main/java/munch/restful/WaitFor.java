package munch.restful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;

/**
 * Created By: Fuxing Loh
 * Date: 17/4/2017
 * Time: 10:55 PM
 * Project: munch-core
 */
public final class WaitFor {
    private static final Logger logger = LoggerFactory.getLogger(WaitFor.class);

    /**
     * Wait for host until given timeout
     *
     * @param host    hostname to wait for
     * @param port    hostname to wait for
     * @param timeout timeout in duration
     */
    public static void host(String host, int port, Duration timeout) {
        logger.info("Waiting for {}:{} with timeout duration of {}", host, port, timeout);
        try {
            if (!ping(host, port, (int) timeout.toMillis())) {
                throw new RuntimeException(host + ":" + port + " is unreachable.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait for host until given timeout
     * Port is required in the url
     *
     * @param url     to wait for
     * @param timeout timeout in duration
     */
    public static void host(String url, Duration timeout) {
        logger.info("Waiting for {} with timeout duration of {}", url, timeout);
        try {
            URI uri = new URI(url);
            if (!ping(uri.getHost(), uri.getPort(), (int) timeout.toMillis())) {
                throw new RuntimeException(url + " is unreachable.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Keep trying with allowed duration
     */
    private static boolean ping(String host, int port, int timeout) throws InterruptedException {
        long startMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() < startMillis + timeout) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeout);
                return true;
            } catch (IOException ignored) {
            }
            Thread.sleep(1000);
        }
        return false;
    }
}
