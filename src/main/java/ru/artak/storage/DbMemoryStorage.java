package ru.artak.storage;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.artak.client.strava.StravaCredential;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DbMemoryStorage implements Storage {
    
    private static volatile DbMemoryStorage instance;
    
    private final Map<Integer, StravaCredential> chatIdToAccessToken = new ConcurrentHashMap<>();
    
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
    public void saveStateForUser(String state, Integer chatId) {
        String sql = "INSERT INTO users (chat_id, state) VALUES (:chat_id, :state)";
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", chatId);
        params.addValue("state", state);
        
        namedParameterJdbcTemplate.update(sql, params);
    }
    
    @Override
    public Integer getChatIdByState(String state) {
        String sql = "SELECT chat_id FROM users WHERE state = :state";
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("state", state);
        
        return DataAccessUtils.singleResult(
            namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getInt("chat_id"))
        );
    }
    
    @Override
    public void saveStravaCredentials(Integer chatId, StravaCredential credential) {
        chatIdToAccessToken.put(chatId, credential);
    }
    
    @Override
    public StravaCredential getStravaCredentials(Integer chatId) {
        return chatIdToAccessToken.get(chatId);
    }
    
    @Override
    public void removeUser(Integer chatId) {
        chatIdToAccessToken.remove(chatId);
    }
    
}
