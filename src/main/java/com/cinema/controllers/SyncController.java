package com.cinema.controllers;

import com.cinema.models.NguoiDung;
import com.cinema.models.PhienLamViec;
import com.cinema.models.UserSession;
import com.cinema.services.PhienLamViecService;
import com.cinema.services.UserSessionService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SyncManager;

import java.util.List;

/**
 * Controller quản lý đồng bộ hóa
 */
public class SyncController {
    private final UserSessionService userSessionService;
    private final PhienLamViecService phienLamViecService;
    private final SyncManager syncManager;
    
    public SyncController(DatabaseConnection dbConnection) {
        this.userSessionService = new UserSessionService(dbConnection);
        this.phienLamViecService = new PhienLamViecService(dbConnection);
        this.syncManager = SyncManager.getInstance(dbConnection);
    }
    
    /**
     * Khởi tạo phiên làm việc cho người dùng
     * @param userId ID của người dùng
     * @param isNhanVien true nếu người dùng là nhân viên
     * @return true nếu khởi tạo thành công
     */
    public boolean initializeSession(int userId, boolean isNhanVien) {
        return syncManager.initializeSession(userId, isNhanVien);
    }
    
    /**
     * Đóng phiên làm việc hiện tại
     */
    public void closeCurrentSession() {
        syncManager.closeCurrentSession();
    }
    
    /**
     * Kiểm tra xem phiên làm việc có đang hoạt động không
     * @return true nếu phiên đang hoạt động
     */
    public boolean isSessionActive() {
        return syncManager.isSessionActive();
    }
    
    /**
     * Lấy danh sách phiên đang hoạt động
     * @return danh sách phiên đang hoạt động
     */
    public List<UserSession> getActiveSessions() {
        return userSessionService.getActiveSessions();
    }
    
    /**
     * Lấy danh sách phiên của người dùng
     * @param maNguoiDung mã người dùng
     * @return danh sách phiên của người dùng
     */
    public List<UserSession> getSessionsByUser(int maNguoiDung) {
        return userSessionService.getSessionsByUser(maNguoiDung);
    }
    
    /**
     * Đóng các phiên không hoạt động trong khoảng thời gian
     * @param minutes số phút không hoạt động
     * @return số phiên đã đóng
     */
    public int closeInactiveSessions(int minutes) {
        return userSessionService.closeInactiveSessions(minutes);
    }
    
    /**
     * Lấy ID phiên làm việc hiện tại
     * @return ID phiên làm việc
     */
    public String getCurrentSessionId() {
        return syncManager.getCurrentSessionId();
    }
    
    /**
     * Lấy phiên làm việc đang hoạt động của nhân viên
     * @param maNhanVien mã nhân viên
     * @return phiên làm việc đang hoạt động
     */
    public PhienLamViec getActivePhienLamViec(int maNhanVien) {
        return phienLamViecService.getActivePhienLamViec(maNhanVien);
    }
    
    /**
     * Lấy danh sách phiên làm việc của nhân viên
     * @param maNhanVien mã nhân viên
     * @return danh sách phiên làm việc
     */
    public List<PhienLamViec> getPhienLamViecByNhanVien(int maNhanVien) {
        return phienLamViecService.getPhienLamViecByNhanVien(maNhanVien);
    }
    
    /**
     * Tắt SyncManager khi đóng ứng dụng
     */
    public void shutdown() {
        syncManager.shutdown();
    }
}