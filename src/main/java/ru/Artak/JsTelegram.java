package ru.Artak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsTelegram {

    private List<Result> result = new ArrayList<>();


    public JsTelegram() {
    }


    public List<Result> getResult() {
        return result;
    }


}