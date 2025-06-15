package com.cinema.services;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.cinema.models.ActivityLog;
import com.cinema.repositories.ActivityLogRepository;
import com.cinema.utils.DatabaseConnection;

public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(DatabaseConnection dbConnection) {
        this.activityLogRepository = new ActivityLogRepository(dbConnection);
    }
    
    public List<ActivityLog> getRecentLogs(int limit) throws SQLException {
        return activityLogRepository.getRecentLogs(limit);
    }
    
    public int addLog(String loaiHoatDong, String moTa, int maNguoiDung) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLoaiHoatDong(loaiHoatDong);
        log.setMoTa(moTa);
        log.setThoiGian(new Date());
        log.setMaNguoiDung(maNguoiDung);
        
        return activityLogRepository.addLog(log);
    }
}