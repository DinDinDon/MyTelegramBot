package ru.artak;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.telegram.TelegramClient;
import ru.artak.server.BotHttpServer;
import ru.artak.service.StravaService;
import ru.artak.service.TelegramService;
import ru.artak.storage.DbMemoryStorage;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // чтение конфигурации, должны быть заданы параметры запуска или переменные окружения. Приоритет у переменных окружения.
        String telegramToken = System.getenv("TELEGRAM_TOKEN");
        String stravaClientSecret = System.getenv("STRAVA_CLIENT_SECRET");
        String stravaBaseRedirectUrl = System.getenv("STRAVA_BASE_REDIRECT_URL");

        String stravaClientIdString = System.getenv("STRAVA_CLIENT_ID");
        int stravaClientId = 0;
        if (StringUtils.isNotBlank(stravaClientIdString)) stravaClientId = Integer.parseInt(stravaClientIdString);

        String portString = System.getenv("PORT");
        int port = 8080;
        if (StringUtils.isNotBlank(portString)) port = Integer.parseInt(portString);

        if (args.length < 3 && (StringUtils.isBlank(telegramToken) && stravaClientId == 0 && StringUtils.isBlank(stravaClientSecret))) {
            throw new IllegalArgumentException("Please define required configuration variables");
        }

        if (StringUtils.isBlank(telegramToken)) telegramToken = args[0];
        if (StringUtils.isBlank(stravaClientSecret)) stravaClientSecret = args[1];
        if (stravaClientId == 0) stravaClientId = Integer.parseInt(args[2]);
        if (StringUtils.isBlank(stravaBaseRedirectUrl)) stravaBaseRedirectUrl = "http://localhost:8080";

        if (StringUtils.isBlank(telegramToken)) {
            throw new IllegalArgumentException("doesn't define telegram token");
        }
        if (StringUtils.isBlank(stravaClientSecret)) {
            throw new IllegalArgumentException("doesn't define strava client secret");
        }
        if (stravaClientId == 0) {
            throw new IllegalArgumentException("doesn't define strava client id");
        }

        String driverName = System.getenv("DRIVER_NAME");
        String jdbcUrl = System.getenv("JDBC_URL");
        String userName = System.getenv("USER_NAME");
        String password = System.getenv("PASSWORD");

        if (args.length < 7 && (StringUtils.isBlank(driverName) && StringUtils.isBlank(jdbcUrl) && StringUtils.isBlank(userName)
                && StringUtils.isBlank(password))) {
            throw new IllegalArgumentException("Please define required DATABASE configuration variables");
        }

        if (StringUtils.isBlank(driverName)) driverName = args[3];
        if (StringUtils.isBlank(jdbcUrl)) jdbcUrl = args[4];
        if (StringUtils.isBlank(userName)) userName = args[5];
        if (StringUtils.isBlank(password)) password = args[6];
        if (StringUtils.isBlank(driverName)) {
            throw new IllegalArgumentException("doesn't define DATABASE driver name");
        }
        if (StringUtils.isBlank(jdbcUrl)) {
            throw new IllegalArgumentException("doesn't define DATABASE JDBC URL");
        }
          if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("doesn't define DATABASE user name");
        }
          if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("doesn't define DATABASE password");
        }

        HikariDataSource dataSource = getHikariDataSource(driverName, jdbcUrl, userName, password);
        SpringLiquibase liquibase = getLiquibase(dataSource);


        try {
            liquibase.afterPropertiesSet();
        } catch (LiquibaseException e) {
            throw new RuntimeException("liquibase error", e);
        }

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        // инициализация зависимостей
        TelegramClient telegramClient = new TelegramClient(telegramToken, stravaClientId, stravaBaseRedirectUrl);

        DbMemoryStorage inMemoryStorage = DbMemoryStorage.getInstance(namedParameterJdbcTemplate);

        StravaClient stravaClient = new StravaClient(stravaClientId, stravaClientSecret, inMemoryStorage);
        StravaService stravaService = new StravaService(telegramClient, inMemoryStorage, stravaClient);
        BotHttpServer botHttpServer = new BotHttpServer(stravaService, port);

        // запуск сервера
        Thread httpServerThread;
        try {
            httpServerThread = new Thread(() -> {
                try {
                    botHttpServer.run();
                } catch (IOException e) {
                    throw new RuntimeException("http server doesn't started", e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("error on create http server thread", e);
        }
        httpServerThread.start();
        System.out.println("started http server");

        // инициализация и запуск обработчика запросов telegram api
        TelegramService telegramService = new TelegramService(telegramClient, inMemoryStorage, stravaService);
        System.out.println("starting telegram bot handler");
        try {
            telegramService.sendGet();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private static HikariDataSource getHikariDataSource(String driverName, String jdbcUrl, String userName, String password) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(userName);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(2);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("myHikariCP");

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        return dataSource;
    }

    private static SpringLiquibase getLiquibase(HikariDataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:liquibase-changeLog.xml");
        liquibase.setDataSource(dataSource);

        return liquibase;
    }
}




