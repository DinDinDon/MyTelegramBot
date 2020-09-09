package ru.artak.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.artak.client.strava.StravaCredential;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class DbStorage implements Storage {
    private static final Logger logger = LogManager.getLogger(DbStorage.class);


    private static volatile DbStorage instance;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final TransactionTemplate transactionTemplate;

    public static DbStorage getInstance(NamedParameterJdbcTemplate namedParameterJdbcTemplate, TransactionTemplate transactionTemplate) {
        if (instance == null) {
            synchronized (DbStorage.class) {
                if (instance == null) {
                    instance = new DbStorage(namedParameterJdbcTemplate, transactionTemplate);
                }
            }
        }

        return instance;
    }

    private DbStorage(NamedParameterJdbcTemplate namedParameterJdbcTemplate, TransactionTemplate transactionTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void saveStateForUser(UUID state, Long chatId) {
        String sql = "INSERT INTO users (chat_id, state) VALUES (:chat_id, :state) ON CONFLICT (chat_id) DO UPDATE SET state = :state, deleted = false";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);
        params.addValue("state", state);
        logger.info("saved user data for user - {}, randomClientID - {}", chatId, state);
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public Long getChatIdByState(UUID state) {
        String sql = "SELECT chat_id FROM users WHERE (state = :state AND deleted IS FALSE)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("state", state);

        return DataAccessUtils.singleResult(
                namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("chat_id"))
        );
    }

    @Override
    public void saveStravaCredentials(Long chatId, StravaCredential credential) {
        Instant instant = Instant.ofEpochSecond(credential.getTimeToExpired());
        Timestamp timeToExparid = Timestamp.valueOf(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));

        String sql = "INSERT INTO user_credentials (access_token, refresh_token, time_to_expired, users_id)" +
                " VALUES (:access_token, :refresh_token, :time_to_expired, (select id from users where chat_id = :chat_id)) " +
                "ON CONFLICT (refresh_token) DO UPDATE SET access_token = :access_token, time_to_expired = :time_to_expired";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("access_token", credential.getAccessToken());
        params.addValue("refresh_token", credential.getRefreshToken());
        params.addValue("time_to_expired", timeToExparid);
        params.addValue("chat_id", chatId);

        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public StravaCredential getStravaCredentials(Long chatId) {
        String sql = " SELECT c.access_token, c.refresh_token, c.time_to_expired, users.deleted FROM user_credentials as c " +
                "INNER JOIN users ON c.users_id = users.id " +
                "WHERE users.id = (SELECT id FROM users WHERE chat_id = :chat_id)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);

        return DataAccessUtils.singleResult(namedParameterJdbcTemplate.query(sql, params, rowMapper));
    }

    private final RowMapper<StravaCredential> rowMapper = (rs, rowNum) -> {
        String accessToken = rs.getString("access_token");
        String refreshToken = rs.getString("refresh_token");
        Timestamp timeToExpiredSql = rs.getTimestamp("time_to_expired");
        Long timeToExpired = timeToExpiredSql.toInstant().getEpochSecond();
        boolean status = rs.getBoolean("deleted");

        return new StravaCredential(accessToken, refreshToken, timeToExpired, status);
    };

    @Override
    public void removeUser(Long chatId) {
        String sqlForUsers = "UPDATE users SET deleted = true WHERE chat_id = :chat_id RETURNING id";
        String sqlForCredentials = "DELETE FROM user_credentials WHERE users_id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Long id = DataAccessUtils.singleResult(namedParameterJdbcTemplate.query(sqlForUsers, params, (rs, rowNum) -> rs.getLong("id")));
                parameterSource.addValue("id", id);
                namedParameterJdbcTemplate.update(sqlForCredentials, parameterSource);
            }
        });
        logger.info("method /deauthorize completed successfully for user - {}", chatId);
    }

}
