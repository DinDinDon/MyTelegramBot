package ru.artak.utils;

import ru.artak.service.DistanceInterval;
import ru.artak.service.FindIntervalType;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

public class DateUtils {
    
    public static DistanceInterval getWeekRange(LocalDate now, FindIntervalType interval) {
        long from;
        long to;
        
        switch (interval) {
            case CURRENTWEEKDISTANCE:
                from = LocalDateTime.of(now, LocalTime.MIDNIGHT).with(DayOfWeek.MONDAY).minusDays(1).toEpochSecond(ZoneOffset.UTC);
                to = LocalDateTime.of(now, LocalTime.MAX).with(DayOfWeek.SUNDAY).toEpochSecond(ZoneOffset.UTC);
                
                return new DistanceInterval(from, to);
            
            case LASTWEEKDISTANCE:
                from = LocalDateTime.of(now, LocalTime.MIDNIGHT).with(DayOfWeek.MONDAY).minusWeeks(1).minusDays(1).toEpochSecond(ZoneOffset.UTC);
                to = LocalDateTime.of(now, LocalTime.MAX).with(DayOfWeek.SUNDAY).minusWeeks(1).toEpochSecond(ZoneOffset.UTC);
                
                return new DistanceInterval(from, to);
        }
        return null;
    }

    public static DistanceInterval getMonthRange(FindIntervalType interval) {
        long from;
        long to;
        
        switch (interval) {
            case CURRENTMONTHDISTANCE:
                from = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).with(TemporalAdjusters.firstDayOfMonth()).minusDays(1).toEpochSecond(ZoneOffset.UTC);
                to = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).with(TemporalAdjusters.lastDayOfMonth()).toEpochSecond(ZoneOffset.UTC);

                return new DistanceInterval(from, to);
            case LASTMONTHDISTANCE:
                from = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).minusDays(1).toEpochSecond(ZoneOffset.UTC);
                to = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).toEpochSecond(ZoneOffset.UTC);

                return new DistanceInterval(from, to);
        }

        return null;
    }
}