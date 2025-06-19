package com.cinema.utils;

import com.cinema.controllers.ActivityLogController;
import com.cinema.models.PhienLamViec;
import com.cinema.models.UserSession;
import com.cinema.services.PhienLamViecService;
import com.cinema.services.UserSessionService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý đồng bộ hóa giữa các phiên làm việc
 */
public class SyncManager {
    private static SyncManager instance;
    private final UserSessionService userSessionService;
    private final PhienLamViecService phienLamViecService;
    private String currentSessionId;
    private int currentUserId;
    private int currentPhienLamViecId = -1;
    private final ScheduledExecutorService scheduler;
    private boolean isInitialized = false;
    private UserSession currentSession;
    
    private SyncManager(DatabaseConnection dbConnection) {
        this.userSessionService = new UserSessionService(dbConnection);
        this.phienLamViecService = new PhienLamViecService(dbConnection);
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    public static synchronized SyncManager getInstance(DatabaseConnection dbConnection) {
        if (instance == null) {
            instance = new SyncManager(dbConnection);
        }
        return instance;
    }
    
    /**
     * Khởi tạo phiên làm việc cho người dùng
     * @param userId ID của người dùng
     * @param isNhanVien true nếu người dùng là nhân viên
     * @return true nếu khởi tạo thành công
     */
    public boolean initializeSession(int userId, boolean isNhanVien) {
        if (isInitialized) {
            System.out.println("KIỂM TRA: Phiên đã được khởi tạo trước đó, bỏ qua.");
            return true;
        }
        
        System.out.println("KIỂM TRA: Đang khởi tạo phiên cho người dùng ID=" + userId + ", isNhanVien=" + isNhanVien);
        String deviceInfo = getDeviceInfo();
        System.out.println("KIỂM TRA: Thông tin thiết bị: " + deviceInfo);
        
        String sessionId = userSessionService.createSession(userId, deviceInfo);
        System.out.println("KIỂM TRA: Kết quả tạo phiên: " + (sessionId != null ? "Thành công, ID=" + sessionId : "Thất bại"));
        
        if (sessionId != null) {
            this.currentSessionId = sessionId;
            this.currentUserId = userId;
            
            // Lưu thông tin phiên hiện tại
            try {
                this.currentSession = userSessionService.getSessionByMaPhien(sessionId);
            } catch (Exception e) {
                System.out.println("Lỗi khi lấy thông tin phiên: " + e.getMessage());
            }
            
            // Nếu là nhân viên, tạo thêm phiên làm việc
            if (isNhanVien) {
                System.out.println("KIỂM TRA: Đang tạo phiên làm việc cho nhân viên ID=" + userId);
                PhienLamViec phienLamViec = phienLamViecService.createPhienLamViec(userId);
                if (phienLamViec != null) {
                    this.currentPhienLamViecId = phienLamViec.getMaPhien();
                    System.out.println("KIỂM TRA: Tạo phiên làm việc thành công, ID=" + phienLamViec.getMaPhien());
                } else {
                    System.out.println("KIỂM TRA: Tạo phiên làm việc thất bại");
                }
            }
            
            // Lên lịch cập nhật phiên làm việc mỗi 30 giây
            scheduler.scheduleAtFixedRate(this::updateCurrentSession, 30, 30, TimeUnit.SECONDS);
            System.out.println("KIỂM TRA: Đã lên lịch cập nhật phiên làm việc mỗi 30 giây");
            
            // Lên lịch đóng các phiên không hoạt động sau 10 phút
            scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions, 5, 5, TimeUnit.MINUTES);
            System.out.println("KIỂM TRA: Đã lên lịch đóng các phiên không hoạt động mỗi 5 phút");
            
            isInitialized = true;
            return true;
        }
        
        System.out.println("KIỂM TRA: Khởi tạo phiên thất bại");
        return false;
    }
    
    /**
     * Cập nhật phiên làm việc hiện tại
     */
    private void updateCurrentSession() {
        if (currentSessionId != null) {
            userSessionService.updateSessionActivity(currentSessionId);
        }
    }
    
    /**
     * Dọn dẹp các phiên không hoạt động
     */
    private void cleanupInactiveSessions() {
        userSessionService.closeInactiveSessions(10); // Đóng các phiên không hoạt động sau 10 phút
    }
    
    /**
     * Đóng phiên làm việc hiện tại
     */
    public void closeCurrentSession() {
        if (currentSessionId != null) {
            userSessionService.closeSession(currentSessionId);
            currentSessionId = null;
        }
        
        if (currentPhienLamViecId > 0) {
            // Kết thúc phiên làm việc của nhân viên
            phienLamViecService.endPhienLamViec(currentPhienLamViecId);
            currentPhienLamViecId = -1;
        }
        
        isInitialized = false;
    }
    
    /**
     * Kiểm tra xem phiên làm việc có đang hoạt động không
     * @return true nếu phiên đang hoạt động
     */
    public boolean isSessionActive() {
        if (currentSessionId == null) {
            return false;
        }
        return userSessionService.isSessionActive(currentSessionId);
    }
    
    /**
     * Lấy thông tin thiết bị
     * @return thông tin thiết bị
     */
    private String getDeviceInfo() {
        StringBuilder deviceInfo = new StringBuilder();
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String os = System.getProperty("os.name") + " " + System.getProperty("os.version");
            deviceInfo.append("Hostname: ").append(hostname).append(", OS: ").append(os);
        } catch (UnknownHostException e) {
            deviceInfo.append("Unknown device");
        }
        return deviceInfo.toString();
    }
    
    /**
     * Lấy ID phiên làm việc hiện tại
     * @return ID phiên làm việc
     */
    public String getCurrentSessionId() {
        return currentSessionId;
    }
    
    /**
     * Lấy ID người dùng hiện tại
     * @return ID người dùng
     */
    public int getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * Lấy phiên làm việc hiện tại
     * @return phiên làm việc hiện tại
     */
    public UserSession getCurrentSession() {
        return currentSession;
    }
    
    /**
     * Lấy ID phiên làm việc của nhân viên hiện tại
     * @return ID phiên làm việc của nhân viên
     */
    public int getCurrentPhienLamViecId() {
        return currentPhienLamViecId;
    }
    
    /**
     * Tắt scheduler khi đóng ứng dụng
     */
    public void shutdown() {
        closeCurrentSession();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}