package ru.artak.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.artak.service.StravaService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            String state = null;
            String authorizationCode = null;
            if(getStateAndAuthCode(exchange).containsKey("state"))
                state = getStateAndAuthCode(exchange).get("state");

            if(getStateAndAuthCode(exchange).containsKey("code"))
             authorizationCode = getStateAndAuthCode(exchange).get("code");

            String text = "Authorization failed. StravaBot";

            if (state != null && authorizationCode != null) {
                try {
                    stravaService.obtainCredentials(state, authorizationCode);
                    text = "GREAT, YOU ARE AUTHORIZED. StravaBot";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            writeResponse(exchange,text);

        }

        private Map<String,String> getStateAndAuthCode(HttpExchange exchange){
            Map<String,String> stravaOauthResponse = new ConcurrentHashMap<>();
            String query = exchange.getRequestURI().getQuery();

            String[] spliterQuery = query.split("&");
            String[] spliterState = spliterQuery[0].split("=");
            String[] spliterAuthorizationCode = spliterQuery[1].split("=");

            if(spliterState.length >=2)
                stravaOauthResponse.put(spliterState[0], spliterState[1]);

            if(spliterAuthorizationCode.length >= 2 )
            stravaOauthResponse.put(spliterAuthorizationCode[0],spliterAuthorizationCode[1]);

            return  stravaOauthResponse;
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
