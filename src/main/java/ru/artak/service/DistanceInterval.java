package ru.artak.service;

public class DistanceInterval {
    private Long from;
    private Long to;

    public DistanceInterval(Long after, Long befor) {
        this.from = after;
        this.to = befor;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }
}