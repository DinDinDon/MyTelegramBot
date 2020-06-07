package ru.artak.client.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetUpdateTelegram {

    private final List<Result> result = new ArrayList<>();
    
    public GetUpdateTelegram() {
    }
    
    public List<Result> getResult() {
        return result;
    }

}