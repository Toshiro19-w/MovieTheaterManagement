package com.cinema.models.repositories.Interface;

import com.cinema.models.UserSession;
import java.util.List;

public interface IUserSessionRepository extends IRepository<UserSession> {
    
    /**
     * Tạo phiên làm việc mới
     * @param userSession thông tin phiên làm việc
     * @return true nếu tạo thành công
     */
    boolean createSession(UserSession userSession);
    
    /**
     * Cập nhật thời gian hoạt động cuối của phiên
     * @param maPhien mã phiên cần cập nhật
     * @return true nếu cập nhật thành công
     */
    boolean updateSessionActivity(String maPhien);
    
    /**
     * Đóng phiên làm việc
     * @param maPhien mã phiên cần đóng
     * @return true nếu đóng thành công
     */
    boolean closeSession(String maPhien);
    
    /**
     * Kiểm tra phiên có tồn tại và đang hoạt động
     * @param maPhien mã phiên cần kiểm tra
     * @return true nếu phiên tồn tại và đang hoạt động
     */
    boolean isSessionActive(String maPhien);
    
    /**
     * Lấy danh sách phiên đang hoạt động
     * @return danh sách phiên đang hoạt động
     */
    List<UserSession> getActiveSessions();
    
    /**
     * Lấy danh sách phiên của người dùng
     * @param maNguoiDung mã người dùng
     * @return danh sách phiên của người dùng
     */
    List<UserSession> getSessionsByUser(int maNguoiDung);
    
    /**
     * Đóng các phiên không hoạt động trong khoảng thời gian
     * @param minutes số phút không hoạt động
     * @return số phiên đã đóng
     */
    int closeInactiveSessions(int minutes);
}