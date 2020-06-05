package ru.Artak;

import ru.Artak.server.HttpServerLocalHost;
import ru.Artak.telegram.Bot;

import java.io.IOException;


public class Main {


    public static void main(String[] args) throws IOException, InterruptedException {
//        Thread threadServer = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HttpServerLocalHost server  = new HttpServerLocalHost();
//            }
//        });
//        threadServer.start();
//        threadServer.join();
//        Bot bot = new Bot();
//        bot.sendGet();
        String bilder = "state=4968&code=30fc0d87b404e96391768abe6adc746872046224&scope=read,activity:read";
        String[] spliterQuery = bilder.split("&");
        String[] spliterState = spliterQuery[0].split("=");
        String[] spliterAuthorizationCode = spliterQuery[1].split("=");
        String state = spliterState[1];
        String authorizationCode = spliterAuthorizationCode[1];
        System.out.println(state);
        System.out.println(authorizationCode);


    }
}




