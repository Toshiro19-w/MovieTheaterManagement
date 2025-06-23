package com.cinema.services;

import com.cinema.models.UserSession;
import com.cinema.models.repositories.UserSessionRepository;
import com.cinema.utils.DatabaseConnection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UserSessionService {
    private final UserSessionRepository repository;
    
    public UserSessionService(DatabaseConnection dbConnection) {
        this.repository = new UserSessionRepository(dbConnection);
    }
    
    /**
     * Tạo phiên làm việc mới cho người dùng
     * @param maNguoiDung mã người dùng
     * @param thongTinThietBi thông tin thiết bị
     * @return mã phiên nếu tạo thành công, null nếu thất bại
     */
    public String createSession(int maNguoiDung, String thongTinThietBi) {
        System.out.println("KIỂM TRA: UserSessionService.createSession() được gọi với maNguoiDung=" + maNguoiDung);
        
        String maPhien = UUID.randomUUID().toString();
        System.out.println("KIỂM TRA: Đã tạo maPhien=" + maPhien);
        
        UserSession userSession = new UserSession();
        userSession.setMaPhien(maPhien);
        userSession.setMaNguoiDung(maNguoiDung);
        userSession.setThoiGianBatDau(LocalDateTime.now());
        userSession.setThoiGianHoatDongCuoi(LocalDateTime.now());
        userSession.setTrangThai("active");
        userSession.setThongTinThietBi(thongTinThietBi);
        
        System.out.println("KIỂM TRA: Đang gọi repository.createSession()");
        boolean success = repository.createSession(userSession);
        System.out.println("KIỂM TRA: Kết quả repository.createSession(): " + (success ? "Thành công" : "Thất bại"));
        
        // Kiểm tra xem phiên có được tạo thành công không
        if (success) {
            try {
                UserSession createdSession = repository.findByMaPhien(maPhien);
                if (createdSession != null) {
                    System.out.println("KIỂM TRA: Phiên đã được tạo và có thể truy vấn: " + createdSession.getMaPhien());
                } else {
                    System.out.println("KIỂM TRA: Không thể truy vấn phiên vừa tạo!");
                }
            } catch (Exception e) {
                System.out.println("KIỂM TRA LỖI: Lỗi khi kiểm tra phiên vừa tạo: " + e.getMessage());
            }
        }
        
        return success ? maPhien : null;
    }
    
    /**
     * Cập nhật thời gian hoạt động cuối của phiên
     * @param maPhien mã phiên
     * @return true nếu cập nhật thành công
     */
    public boolean updateSessionActivity(String maPhien) {
        return repository.updateSessionActivity(maPhien);
    }
    
    /**
     * Đóng phiên làm việc
     * @param maPhien mã phiên
     * @return true nếu đóng thành công
     */
    public boolean closeSession(String maPhien) {
        return repository.closeSession(maPhien);
    }
    
    /**
     * Kiểm tra phiên có tồn tại và đang hoạt động
     * @param maPhien mã phiên
     * @return true nếu phiên tồn tại và đang hoạt động
     */
    public boolean isSessionActive(String maPhien) {
        return repository.isSessionActive(maPhien);
    }
    
    /**
     * Lấy danh sách phiên đang hoạt động
     * @return danh sách phiên đang hoạt động
     */
    public List<UserSession> getActiveSessions() {
        return repository.getActiveSessions();
    }
    
    /**
     * Lấy danh sách phiên của người dùng
     * @param maNguoiDung mã người dùng
     * @return danh sách phiên của người dùng
     */
    public List<UserSession> getSessionsByUser(int maNguoiDung) {
        return repository.getSessionsByUser(maNguoiDung);
    }
    
    /**
     * Đóng các phiên không hoạt động trong khoảng thời gian
     * @param minutes số phút không hoạt động
     * @return số phiên đã đóng
     */
    public int closeInactiveSessions(int minutes) {
        return repository.closeInactiveSessions(minutes);
    }
    
    /**
     * Lấy thông tin phiên theo mã phiên
     * @param maPhien mã phiên
     * @return thông tin phiên
     */
    public UserSession getSessionByMaPhien(String maPhien) {
        return repository.findByMaPhien(maPhien);
    }
}