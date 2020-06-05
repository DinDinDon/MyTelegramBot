package ru.artak.client.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Enumer {
    @JsonProperty("private")
    PRIVATE,
    @JsonProperty("group")
    GROUP,
    @JsonProperty("supergroup")
    SPUERGROUP,
    @JsonProperty("channel")
    CHANNEL;
}
