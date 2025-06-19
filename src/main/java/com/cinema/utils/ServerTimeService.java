package com.cinema.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service để lấy và quản lý thời gian server
 * Sử dụng nhiều time server để đảm bảo độ tin cậy
 */
public class ServerTimeService {
    private static final Logger LOGGER = Logger.getLogger(ServerTimeService.class.getName());
    
    // Danh sách các time server, sẽ thử lần lượt nếu server trước không phản hồi
    private static final List<String> TIME_SERVERS = Arrays.asList(
        "time.windows.com",
        "time.google.com",
        "pool.ntp.org",
        "time.apple.com"
    );
    
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int CONNECT_TIMEOUT = 3000; // 3 seconds
    private static final int READ_TIMEOUT = 3000;    // 3 seconds
    private static final long CACHE_DURATION = 60000; // 1 minute
    
    private static final AtomicReference<TimeInfo> cachedTime = new AtomicReference<>();
    private static volatile Duration offsetFromServer = Duration.ZERO;
    
    /**
     * Class để lưu trữ thông tin thời gian và thời điểm cache
     */
    private static class TimeInfo {
        final LocalDateTime serverTime;
        final long timestamp;
        
        TimeInfo(LocalDateTime serverTime) {
            this.serverTime = serverTime;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
    
    /**
     * Lấy thời gian server hiện tại
     * @return LocalDateTime đã được điều chỉnh theo múi giờ Việt Nam
     */
    public static LocalDateTime getServerTime() {
        // Kiểm tra cache
        TimeInfo cached = cachedTime.get();
        if (cached != null && !cached.isExpired()) {
            return LocalDateTime.now().plus(offsetFromServer);
        }
        
        // Thử kết nối đến các time server
        for (String server : TIME_SERVERS) {
            try {
                // Đo thời gian bắt đầu để tính network latency
                long startTime = System.nanoTime();
                
                // Ping trước để kiểm tra server có phản hồi không
                if (!pingServer(server)) {
                    continue;
                }
                
                URL url = new URL("http://" + server);
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.connect();
                
                // Tính network latency
                long endTime = System.nanoTime();
                Duration latency = Duration.ofNanos((endTime - startTime) / 2); // RTT/2
                
                String dateStr = conn.getHeaderField("date");
                if (dateStr != null) {
                    LocalDateTime serverTime = LocalDateTime.parse(dateStr, 
                            DateTimeFormatter.RFC_1123_DATE_TIME)
                            .atZone(ZoneId.systemDefault())
                            .withZoneSameInstant(VIETNAM_ZONE)
                            .toLocalDateTime()
                            .plus(latency); // Bù độ trễ mạng
                    
                    // Cập nhật offset và cache
                    offsetFromServer = Duration.between(LocalDateTime.now(), serverTime);
                    cachedTime.set(new TimeInfo(serverTime));
                    
                    LOGGER.log(Level.INFO, "Synchronized with time server: {0}, latency: {1}ms", 
                             new Object[]{server, latency.toMillis()});
                    
                    return serverTime;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to connect to time server: " + server, e);
            }
        }
        
        // Fallback: sử dụng thời gian local
        LocalDateTime localTime = LocalDateTime.now(VIETNAM_ZONE);
        LOGGER.warning("Using local time as fallback");
        return localTime;
    }
    
    /**
     * Lấy ngày hiện tại theo định dạng dd/MM/yyyy
     */
    public static String getFormattedServerDate() {
        return getServerTime().format(DATE_FORMATTER);
    }
    
    /**
     * Kiểm tra xem có thể kết nối đến ít nhất một time server không
     */
    public static boolean isServerTimeAvailable() {
        return TIME_SERVERS.stream().anyMatch(ServerTimeService::pingServer);
    }
    
    /**
     * Ping một server để kiểm tra kết nối
     */
    private static boolean pingServer(String server) {
        try {
            return InetAddress.getByName(server).isReachable(CONNECT_TIMEOUT);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Lấy độ lệch thời gian hiện tại so với server
     */
    public static Duration getOffset() {
        return offsetFromServer;
    }
    
    /**
     * Force cập nhật thời gian từ server, bỏ qua cache
     */
    public static LocalDateTime forceSync() {
        cachedTime.set(null);
        return getServerTime();
    }
    
    /**
     * Kiểm tra xem thời gian có đang được đồng bộ với server không
     */
    public static boolean isSynchronized() {
        TimeInfo cached = cachedTime.get();
        return cached != null && !cached.isExpired();
    }
}