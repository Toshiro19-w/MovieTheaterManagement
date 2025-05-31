package com.cinema.utils;

public class TimeFormatter {
    public static String formatMinutesToHoursAndMinutes(int minutes) {
        if (minutes < 60) {
            return minutes + " phút";
        }
        
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        
        if (remainingMinutes == 0) {
            return hours + " giờ";
        } else {
            return hours + " giờ " + remainingMinutes + " phút";
        }
    }
}