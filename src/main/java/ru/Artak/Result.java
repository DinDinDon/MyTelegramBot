package ru.Artak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {

    private Integer update_id;
    private Message message;

    public Integer getUpdate_id() {
        return update_id;
    }

    public void setUpdate_id(Integer update_id) {
        this.update_id = update_id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Result() {
    }

    @Override
    public String toString() {
        return "Result{" +
                "update_id=" + update_id +
                ", message=" + message +
                '}';
    }
}