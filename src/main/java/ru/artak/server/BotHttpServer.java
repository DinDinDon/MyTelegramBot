package ru.artak.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringUtils;
import ru.artak.service.StravaService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BotHttpServer {

    private final StravaService stravaService;

    public BotHttpServer(StravaService stravaService) {
        this.stravaService = stravaService;
    }

    public void run() throws IOException {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);

        server.createContext("/", new EchoHandler());
        server.start();
    }

    private class EchoHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> stateAndAuthCode = getStateAndAuthCode(exchange);
            String state = stateAndAuthCode.get("state");
            String authorizationCode = stateAndAuthCode.get("code");
            String text = "Authorization failed. StravaBot";

            if (!StringUtils.isBlank(state) && !StringUtils.isBlank(authorizationCode)) {
                try {
                    stravaService.obtainCredentials(state, authorizationCode);
                    text = "GREAT, YOU ARE AUTHORIZED. StravaBot";
                } catch (Exception e) {
                    e.printStackTrace();
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
