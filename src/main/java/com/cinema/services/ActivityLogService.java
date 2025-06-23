package com.cinema.services;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cinema.models.ActivityLog;
import com.cinema.repositories.ActivityLogRepository;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.ServerTimeService;

/**
 * Service xử lý nghiệp vụ cho hoạt động log
 */
public class ActivityLogService {
    private static final Logger LOGGER = Logger.getLogger(ActivityLogService.class.getName());
    private final ActivityLogRepository repository;

    public ActivityLogService(DatabaseConnection dbConnection) {
        this.repository = new ActivityLogRepository(dbConnection);
    }

    /**
     * Thêm log với thời gian được chỉ định
     */
    public int addLogWithTime(String loaiHoatDong, String moTa, int maNguoiDung, LocalDateTime thoiGian)
            throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLoaiHoatDong(loaiHoatDong);
        log.setMoTa(moTa);
        log.setMaNguoiDung(maNguoiDung);
        log.setThoiGian(thoiGian);

        int logId = repository.addLog(log);
        LOGGER.log(Level.FINE, "Created log ID {0} at {1}", new Object[]{logId, thoiGian});
        return logId;
    }

    /**
     * Thêm log với thời gian hiện tại của server
     */
    public int addLog(String loaiHoatDong, String moTa, int maNguoiDung) throws SQLException {
        return addLogWithTime(loaiHoatDong, moTa, maNguoiDung, ServerTimeService.getServerTime());
    }

    /**
     * Lấy danh sách log gần đây nhất
     */
    public List<ActivityLog> getRecentLogs(int limit) throws SQLException {
        return repository.getRecentLogs(limit);
    }

    /**
     * Lấy thông tin chi tiết của một log
     */
    public ActivityLog getLogById(int logId) throws SQLException {
        return repository.getLogById(logId);
    }

    /**
     * Xóa log cũ hơn số ngày chỉ định
     */
    public int deleteOldLogs(int days) throws SQLException {
        int deletedCount = repository.deleteOldLogs(days);
        LOGGER.log(Level.INFO, "Deleted {0} old logs older than {1} days",
                new Object[]{deletedCount, days});
        return deletedCount;
    }

    /**
     * Xóa một log cụ thể
     */
    public boolean deleteLog(int logId) throws SQLException {
        boolean success = repository.deleteLog(logId);
        if (success) {
            LOGGER.log(Level.FINE, "Deleted log ID {0} at {1}",
                    new Object[]{logId, ServerTimeService.getServerTime()});
        }
        return success;
    }
}