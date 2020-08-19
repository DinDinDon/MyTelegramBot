package ru.artak.storage;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.artak.client.strava.StravaCredential;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DbMemoryStorage implements Storage {

    private static volatile DbMemoryStorage instance;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static DbMemoryStorage getInstance(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        if (instance == null) {
            synchronized (DbMemoryStorage.class) {
                if (instance == null) {
                    instance = new DbMemoryStorage(namedParameterJdbcTemplate);
                }
            }
        }

        return instance;
    }

    private DbMemoryStorage(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public void saveStateForUser(UUID state, Long chatId) {
        String sql = "INSERT INTO users (chat_id, state) VALUES (:chat_id, :state) ON CONFLICT DO NOTHING";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);
        params.addValue("state", state);
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public Long getChatIdByState(UUID state) {
        String sql = "SELECT chat_id FROM users WHERE state = :state";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("state", state);

        return DataAccessUtils.singleResult(
                namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("chat_id"))
        );
    }

    @Override
    public void saveStravaCredentials(Long chatId, StravaCredential credential) {
        String accessToken = credential.getAccessToken();
        String refreshToken = credential.getRefreshToken();
        Long timeToExpired = credential.getTimeToExpired();
        String sql = "INSERT INTO user_credentials (access_token, refresh_token,time_to_expired,users_id)" +
                " VALUES (:access_token, :refresh_token, :time_to_expired, (select id from users where chat_id =:chat_id)) " +
                "ON CONFLICT ON CONSTRAINT user_credentials_access_token_key " +
                "DO UPDATE SET access_token = :access_token,refresh_token = :refresh_token,time_to_expired = :time_to_expired";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("access_token", accessToken);
        params.addValue("refresh_token", refreshToken);
        params.addValue("time_to_expired", timeToExpired);
        params.addValue("chat_id", chatId);
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public StravaCredential getStravaCredentials(Long chatId) {
        String sql = " SELECT c.access_token, c.refresh_token, c.time_to_expired FROM user_credentials as c " +
                "INNER JOIN users ON c.users_id = users.id " +
                "WHERE users.id = (SELECT id FROM users WHERE chat_id = :chat_id)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);
        String accessToken = DataAccessUtils.singleResult(
                namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("access_token"))
        );
        String refreshToken = DataAccessUtils.singleResult(
                namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("refresh_token"))
        );
        Long timeToExpired = DataAccessUtils.singleResult(
                namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("time_to_expired"))
        );

        return new StravaCredential(accessToken, refreshToken, timeToExpired);
    }

    @Override
    public void removeUser(Long chatId) {
        String sql = "DELETE FROM users WHERE chat_id = :chat_id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);

        namedParameterJdbcTemplate.update(sql, params);
    }

}
