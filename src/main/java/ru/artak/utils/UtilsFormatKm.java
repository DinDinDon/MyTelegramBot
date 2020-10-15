package ru.artak.utils;


public class UtilsFormatKm {

    public static Number getFormatKm(float distance) {
        float result = distance / 1000;
        if (result - Math.round(result) == 0)
            return Math.round(result);

        return Math.round(result * 10.0) / 10.0;
    }
}