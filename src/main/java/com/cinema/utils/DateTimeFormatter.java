package com.cinema.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateTimeFormatter {
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private static final java.time.format.DateTimeFormatter DATE_TIME_FORMATTER = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private static final java.time.format.DateTimeFormatter TIME_FORMATTER = 
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_FORMATTER);
    }
    
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }
    
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
    }
}