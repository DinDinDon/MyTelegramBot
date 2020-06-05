package ru.Artak.server;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HttpServerLocalHost {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);

        HttpContext context = server.createContext("/", new EchoHandler());
        server.start();

    }

    static class EchoHandler implements HttpHandler {
        String authorizationCode;
        String stateID;


        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder builder = new StringBuilder();

            builder.append("<h1>URI: ").append(exchange.getRequestURI()).append("</h1>");
            builder.toString();
//            System.out.println(builder.toString());

            //<h1>URI: /exchange_token?state=&code=68f997b7e03a65668444717b3e996902c8920978&scope=read,activity:read</h1>
//            <h1>URI: /favicon.ico</h1>
///
            String query = exchange.getRequestURI().getQuery().toString();

            String[] spliterQuery = query.split("&");
            String[] spliterState = spliterQuery[0].split("=");
            String[] spliterAuthorizationCode = spliterQuery[1].split("=");
            String state = spliterState[1];
            String authorizationCode = spliterAuthorizationCode[1];



            byte[] bytes = "GREAT, YOU ARE AUTHORIZED. StravaBot".getBytes();
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}