package ru.artak.client.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    
    @JsonProperty("update_id")
    private Integer updateId;
    private Message message;

    public Result() {
    }

    public Integer getUpdateId() {
        return updateId;
    }


    public Message getMessage() {
        return message;
    }


}