package ru.Artak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {

    private Integer update_id;
    private Message message;

    public Result() {
    }

    public Integer getUpdate_id() {
        return update_id;
    }


    public Message getMessage() {
        return message;
    }


}