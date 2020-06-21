package ru.artak.service;

public class WeekInterval {
    private Long after;
    private Long befor;

    public WeekInterval(Long after, Long befor) {
        this.after = after;
        this.befor = befor;
    }

    public Long getAfter() {
        return after;
    }

    public void setAfter(Long after) {
        this.after = after;
    }

    public Long getBefor() {
        return befor;
    }

    public void setBefor(Long befor) {
        this.befor = befor;
    }
}