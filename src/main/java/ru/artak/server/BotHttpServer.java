package ru.artak.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.artak.service.StravaService;
import ru.artak.storage.Storage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

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
            String query = exchange.getRequestURI().getQuery();

            String[] spliterQuery = query.split("&");
            String[] spliterState = spliterQuery[0].split("=");
            String[] spliterAuthorizationCode = spliterQuery[1].split("=");
            String state = spliterState[1];
            String authorizationCode = spliterAuthorizationCode[1];
            String text = "";

            if (state != null && authorizationCode != null) {
                try {
                    stravaService.obtainCredentials(state, authorizationCode);
                    text = "GREAT, YOU ARE AUTHORIZED. StravaBot";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                text = "Authorization failed. StravaBot";
            }
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
