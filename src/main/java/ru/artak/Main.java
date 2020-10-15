package ru.artak;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.artak.bot.PoolingBot;
import ru.artak.bot.UpdateHandlerImpl;
import ru.artak.client.strava.StravaClient;
import ru.artak.client.telegram.TelegramClient;
import ru.artak.server.BotHttpServer;
import ru.artak.service.StravaService;
import ru.artak.service.TelegramService;
import ru.artak.storage.DbStorage;

import java.io.IOException;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // чтение конфигурации, должны быть заданы параметры запуска или переменные окружения. Приоритет у переменных окружения.
        logger.debug("Reading of configuration data started");
        String telegramToken = System.getenv("TELEGRAM_TOKEN");
        String telegramBotName = System.getenv("TELEGRAM_BOT_NAME");
        String stravaClientSecret = System.getenv("STRAVA_CLIENT_SECRET");
        String stravaBaseRedirectUrl = System.getenv("STRAVA_BASE_REDIRECT_URL");

        String stravaClientIdString = System.getenv("STRAVA_CLIENT_ID");
        int stravaClientId = 0;
        if (StringUtils.isNotBlank(stravaClientIdString)) stravaClientId = Integer.parseInt(stravaClientIdString);

        String portString = System.getenv("PORT");
        int port = 8080;
        if (StringUtils.isNotBlank(portString)) port = Integer.parseInt(portString);

        if (args.length < 4 && (StringUtils.isBlank(telegramToken) && stravaClientId == 0 && StringUtils.isBlank(stravaClientSecret))) {
            logger.warn("Please define required configuration variables");
            throw new IllegalArgumentException("Please define required configuration variables");
        }

        if (StringUtils.isBlank(telegramToken)) telegramToken = args[0];
        if (StringUtils.isBlank(telegramBotName)) telegramBotName = args[1];
        if (StringUtils.isBlank(stravaClientSecret)) stravaClientSecret = args[2];
        if (stravaClientId == 0) stravaClientId = Integer.parseInt(args[3]);
        if (StringUtils.isBlank(stravaBaseRedirectUrl)) stravaBaseRedirectUrl = "http://localhost:8080";

        if (StringUtils.isBlank(telegramToken)) {
            logger.warn("doesn't define telegram token");
            throw new IllegalArgumentException("doesn't define telegram token");
        }
        if (StringUtils.isBlank(telegramBotName)) {
            logger.warn("doesn't define telegram bot name");
            throw new IllegalArgumentException("doesn't define telegram bot name");
        }
        if (StringUtils.isBlank(stravaClientSecret)) {
            logger.warn("doesn't define strava client secret");
            throw new IllegalArgumentException("doesn't define strava client secret");
        }
        if (stravaClientId == 0) {
            logger.warn("doesn't define strava client id");
            throw new IllegalArgumentException("doesn't define strava client id");
        }

        String driverName = System.getenv("DRIVER_NAME");
        String jdbcUrl = System.getenv("JDBC_URL");
        String userName = System.getenv("USER_NAME");
        String password = System.getenv("PASSWORD");
        int maximumPoolSize = 0;
        String poolSize = System.getenv("MAXIMUM_POOL_SIZE");
        if (StringUtils.isNotBlank(poolSize)) maximumPoolSize = Integer.parseInt(poolSize);

        if (args.length < 9 && (StringUtils.isBlank(driverName) && StringUtils.isBlank(jdbcUrl) && StringUtils.isBlank(userName)
                && StringUtils.isBlank(password) && StringUtils.isNotBlank(poolSize))) {
            logger.warn("Please define required DATABASE configuration variables");
            throw new IllegalArgumentException("Please define required DATABASE configuration variables");
        }
        if (StringUtils.isBlank(driverName)) driverName = args[4];
        if (StringUtils.isBlank(jdbcUrl)) jdbcUrl = args[5];
        if (StringUtils.isBlank(userName)) userName = args[6];
        if (StringUtils.isBlank(password)) password = args[7];
        if (maximumPoolSize == 0) maximumPoolSize = Integer.parseInt(args[8]);
        if (StringUtils.isBlank(driverName)) {
            logger.warn("doesn't define DATABASE driver name");
            throw new IllegalArgumentException("doesn't define DATABASE driver name");
        }
        if (StringUtils.isBlank(jdbcUrl)) {
            logger.warn("doesn't define DATABASE JDBC URL");
            throw new IllegalArgumentException("doesn't define DATABASE JDBC URL");
        }
        if (StringUtils.isBlank(userName)) {
            logger.warn("doesn't define DATABASE user name");
            throw new IllegalArgumentException("doesn't define DATABASE user name");
        }
        if (StringUtils.isBlank(password)) {
            logger.warn("doesn't define DATABASE password");
            throw new IllegalArgumentException("doesn't define DATABASE password");
        }
        if (maximumPoolSize == 0) {
            logger.warn("doesn't define DATABASE maximum pool size");
            throw new IllegalArgumentException("doesn't define DATABASE maximum pool size");
        }

        HikariDataSource dataSource = getHikariDataSource(driverName, jdbcUrl, userName, password, maximumPoolSize);
        SpringLiquibase liquibase = getLiquibase(dataSource);

        try {
            liquibase.afterPropertiesSet();
        } catch (LiquibaseException e) {
            logger.warn("liquibase could not start, check configuration ", e);
            throw new RuntimeException("liquibase could not start, check configuration ", e);
        }
        logger.info("finished reading configuration data");

        // инициализация зависимостей
        logger.debug("dependency initialization");
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = getTransactionTemplate(transactionManager);

        DbStorage dbStorage = DbStorage.getInstance(namedParameterJdbcTemplate, transactionTemplate);

        TelegramClient telegramClient = new TelegramClient(telegramToken);
        StravaClient stravaClient = new StravaClient(stravaClientId, stravaClientSecret, dbStorage);
        StravaService stravaService = new StravaService(telegramClient, dbStorage, stravaClient);
        BotHttpServer botHttpServer = new BotHttpServer(stravaService, port);
        TelegramService telegramService = new TelegramService(dbStorage, stravaService, stravaClientId, stravaBaseRedirectUrl);
        UpdateHandlerImpl updateHandlerImpl = new UpdateHandlerImpl();

        // запуск сервера
        logger.debug("server start");
        Thread httpServerThread;
        try {
            httpServerThread = new Thread(() -> {
                try {
                    botHttpServer.run();
                } catch (IOException e) {
                    logger.warn("http server doesn't started", e);
                    throw new RuntimeException("http server doesn't started", e);
                }
            });
        } catch (Exception e) {
            logger.warn("error on create http server thread", e);
            throw new RuntimeException("error on create http server thread", e);
        }
        httpServerThread.start();
        logger.info("started http server");

        // инициализация и запуск обработчика запросов telegram api
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        PoolingBot poolingBot = new PoolingBot(telegramToken, telegramBotName, updateHandlerImpl, telegramService);
        try {
            telegramBotsApi.registerBot(poolingBot);
        } catch (
                TelegramApiRequestException e) {
            logger.warn("Telegram bot api request exception", e);
        }

    }

    private static HikariDataSource getHikariDataSource(String driverName, String jdbcUrl, String userName, String password, int maximumPoolSize) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(userName);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(maximumPoolSize);

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        return dataSource;
    }

    private static SpringLiquibase getLiquibase(HikariDataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:liquibase-changeLog.xml");
        liquibase.setDataSource(dataSource);

        return liquibase;
    }

    private static TransactionTemplate getTransactionTemplate(DataSourceTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setName("TransactionDeleteUser");

        return transactionTemplate;
    }
}




