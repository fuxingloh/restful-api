package munch.restful;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.time.Duration;

/**
 * Created By: Fuxing Loh
 * Date: 17/4/2017
 * Time: 10:55 PM
 * Project: munch-core
 */
public final class WaitFor {
    private static final Logger logger = LoggerFactory.getLogger(WaitFor.class);

    public static void localstack(String dashboardUrl, Duration timeout) {
        logger.info("Waiting for localstack with url: {} with timeout duration of {}", dashboardUrl, timeout);
        long startMillis = System.currentTimeMillis();
        long endMillis = startMillis + timeout.toMillis();

        CloseableHttpClient client = HttpClients.createDefault();
        while (System.currentTimeMillis() < endMillis) {
//            int connectionTimeout = (int) (endMillis - System.currentTimeMillis());

            HttpPost httpPost = new HttpPost(dashboardUrl + "/graph");
            StringEntity entity = new StringEntity("{\"nameFilter\":\".*\",\"awsEnvironment\":\"dev\"}", "utf-8");
            httpPost.setEntity(entity);

            try {
                if (client.execute(httpPost).getStatusLine().getStatusCode() == 200) return;
            } catch (IOException ignored) {
            }
        }
    }

    public static void statusOk(String url, Duration timeout) {
        logger.info("Waiting for {} with timeout duration of {}", url, timeout);
        try {
            code(url, 200, (int) timeout.toMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
            if (!ping(uri.getHost(), resolvePort(uri), (int) timeout.toMillis())) {
                throw new RuntimeException(url + " is unreachable.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int resolvePort(URI uri) {
        if (uri.getPort() != -1) return uri.getPort();
        if (uri.getScheme().startsWith("https")) return 443;
        if (uri.getScheme().startsWith("http")) return 80;
        return -1;
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

    private static boolean code(String urlSting, int code, int timeout) {
        long startMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() < startMillis + timeout) {
            try {
                URL url = new URL(urlSting);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(timeout);
                connection.setConnectTimeout(timeout);
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == code) return true;
                Thread.sleep(1000);
            } catch (Exception ignored) {

            }
        }
        return false;
    }
}
