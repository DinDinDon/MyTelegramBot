package ru.artak.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.artak.service.StravaService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class BotHttpServer {
    private static final Logger logger = LogManager.getLogger(BotHttpServer.class);

    private final StravaService stravaService;

    private final int port;

    public BotHttpServer(StravaService stravaService, int port) {
        this.stravaService = stravaService;
        this.port = port;
    }

    public void run() throws IOException {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);

        server.createContext("/", new EchoHandler());
        server.start();
    }

    private class EchoHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> stateAndAuthCode = getStateAndAuthCode(exchange);
            UUID state = UUID.fromString(stateAndAuthCode.get("state"));
            String authorizationCode = stateAndAuthCode.get("code");
            logger.debug("the server received a response from Strava");
            String text = "Authorization failed. StravaBot";

            if (!StringUtils.isBlank(state.toString()) && !StringUtils.isBlank(authorizationCode)) {
                try {

                    stravaService.obtainCredentials(state, authorizationCode);
                    text = "GREAT, YOU ARE AUTHORIZED. StravaBot";
                } catch (Exception e) {
                    logger.error("failed to save credentials ", e);
                }
            }
            writeResponse(exchange, text);
        }

        private Map<String, String> getStateAndAuthCode(HttpExchange exchange) {
            Map<String, String> map = new HashMap<>();
            String query = exchange.getRequestURI().getQuery();
            String[] spliterQuery = query.split("&");

            for (String value : spliterQuery) {
                String[] s = value.split("=");
                map.put(s[0], s[1]);
            }

            return map;
        }

        private void writeResponse(HttpExchange exchange, String text) throws IOException {
            exchange.sendResponseHeaders(200, text.getBytes().length);
            OutputStream os = null;
            try {
                os = exchange.getResponseBody();
                os.write(text.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (os != null)
                        os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
