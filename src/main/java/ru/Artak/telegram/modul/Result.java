package ru.Artak.telegram.modul;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.Artak.telegram.modul.Message;

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