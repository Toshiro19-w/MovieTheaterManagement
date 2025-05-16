package com.cinema.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ServerTimeService {
    private static final String TIME_SERVER = "time.windows.com";
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @SuppressWarnings("deprecation")
    public static LocalDateTime getServerTime() {
        try {
            URL url = new URL("http://" + TIME_SERVER);
            URLConnection connection = url.openConnection();
            connection.connect();
            
            // Lấy thời gian từ header của response
            String dateStr = connection.getHeaderField("date");
            if (dateStr != null) {
                // Chuyển đổi thời gian sang múi giờ Việt Nam
                return LocalDateTime.parse(dateStr, DateTimeFormatter.RFC_1123_DATE_TIME);
            }
        } catch (IOException e) {
            System.err.println("Không thể kết nối đến server thời gian: " + e.getMessage());
        }
        
        // Fallback: sử dụng thời gian local nếu không kết nối được
        return LocalDateTime.now(VIETNAM_ZONE);
    }

    public static String getFormattedServerDate() {
        return getServerTime().format(DATE_FORMATTER);
    }

    public static boolean isServerTimeAvailable() {
        try {
            URL url = new URL("http://" + TIME_SERVER);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000); // 5 giây timeout
            connection.connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
} 