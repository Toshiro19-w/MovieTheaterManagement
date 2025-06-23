package com.cinema.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cinema.models.ActivityLog;
import com.cinema.services.ActivityLogService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ServerTimeService;
import com.cinema.utils.TransactionManager;

/**
 * Controller xử lý các hoạt động liên quan đến log hệ thống
 */
public class ActivityLogController implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(ActivityLogController.class.getName());
    private final ActivityLogService activityLogService;
    private final TransactionManager transactionManager;
    private final DatabaseConnection dbConnection;
    
    public ActivityLogController() throws IOException {
        this.dbConnection = new DatabaseConnection();
        this.activityLogService = new ActivityLogService(dbConnection);
        this.transactionManager = new TransactionManager(dbConnection);
    }
    
    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
        this.transactionManager = null;
        this.dbConnection = null;
    }

    /**
     * Lấy thời gian hiện tại từ server
     * @return LocalDateTime của server
     */
    private LocalDateTime getCurrentServerTime() {
        return ServerTimeService.getServerTime();
    }
    
    /**
     * Lấy danh sách log gần đây nhất
     * @param limit số lượng log tối đa cần lấy
     * @return danh sách các log
     * @throws SQLException nếu có lỗi truy vấn database
     */
    public List<ActivityLog> getRecentLogs(int limit) throws SQLException {
        return activityLogService.getRecentLogs(limit);
    }
    
    /**
     * Thêm một log mới vào hệ thống
     * @param loaiHoatDong loại hoạt động
     * @param moTa mô tả chi tiết
     * @param maNguoiDung mã người dùng thực hiện hành động
     * @return ID của log vừa thêm
     * @throws SQLException nếu có lỗi khi thêm log
     */
    public int addLog(String loaiHoatDong, String moTa, int maNguoiDung) throws SQLException {
        LocalDateTime serverTime = getCurrentServerTime();
        LOGGER.log(Level.FINE, "Adding log with server time: {0}", serverTime);
        
        if (transactionManager != null) {
            return transactionManager.executeTransaction(connection -> {
                try {
                    return activityLogService.addLogWithTime(loaiHoatDong, moTa, maNguoiDung, serverTime);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            return activityLogService.addLogWithTime(loaiHoatDong, moTa, maNguoiDung, serverTime);
        }
    }
    
    /**
     * Lấy thông tin chi tiết của một log
     * @param logId ID của log cần xem
     * @return đối tượng ActivityLog chứa thông tin chi tiết
     * @throws SQLException nếu có lỗi truy vấn database
     */
    public ActivityLog getLogById(int logId) throws SQLException {
        return activityLogService.getLogById(logId);
    }
    
    /**
     * Xóa một log khỏi hệ thống
     * @param logId ID của log cần xóa
     * @return true nếu xóa thành công, false nếu không tìm thấy log
     * @throws SQLException nếu có lỗi khi xóa log
     */
    public boolean deleteLog(int logId) throws SQLException {
        LocalDateTime serverTime = getCurrentServerTime();
        LOGGER.log(Level.FINE, "Deleting log {0} at server time: {1}", new Object[]{logId, serverTime});
        
        if (transactionManager != null) {
            return transactionManager.executeTransaction(connection -> {
                try {
                    return activityLogService.deleteLog(logId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            return activityLogService.deleteLog(logId);
        }
    }
    
    /**
     * Xóa tất cả log cũ hơn số ngày chỉ định
     * @param days số ngày
     * @return số lượng log đã xóa
     * @throws SQLException nếu có lỗi khi xóa log
     */
    public int deleteOldLogs(int days) throws SQLException {
        LocalDateTime serverTime = getCurrentServerTime();
        LOGGER.log(Level.FINE, "Cleaning old logs (older than {0} days) at server time: {1}", 
                  new Object[]{days, serverTime});
        
        if (transactionManager != null) {
            return transactionManager.executeTransaction(connection -> {
                try {
                    return activityLogService.deleteOldLogs(days);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            return activityLogService.deleteOldLogs(days);
        }
    }
    
    /**
     * Kiểm tra xem hệ thống có đang sử dụng thời gian server không
     * @return true nếu đang sử dụng thời gian server
     */
    public boolean isUsingServerTime() {
        return ServerTimeService.isSynchronized();
    }
    
    /**
     * Force đồng bộ hóa thời gian với server
     * @return true nếu đồng bộ thành công
     */
    public boolean forceSyncTime() {
        try {
            ServerTimeService.forceSync();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to force sync time with server", e);
            return false;
        }
    }
    
    @Override
    public void close() throws Exception {
        if (dbConnection != null) {
            dbConnection.close();
        }
    }
}