package ru.artak.client.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Chat {
    private Integer id;
    private String first_name;
    private String last_name;
    private Enumer type;

    public Chat() {
    }

    public Integer getId() {
        return id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public Enumer getType() {
        return type;
    }

}