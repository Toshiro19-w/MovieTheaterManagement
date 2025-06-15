package com.cinema.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.cinema.models.ActivityLog;
import com.cinema.services.ActivityLogService;
import com.cinema.utils.DatabaseConnection;

public class ActivityLogController {
    private final ActivityLogService activityLogService;
    
    public ActivityLogController() throws IOException {
        this.activityLogService = new ActivityLogService(new DatabaseConnection());
    }
    
    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }
    
    public List<ActivityLog> getRecentLogs(int limit) throws SQLException {
        return activityLogService.getRecentLogs(limit);
    }
    
    public int addLog(String loaiHoatDong, String moTa, int maNguoiDung) throws SQLException {
        return activityLogService.addLog(loaiHoatDong, moTa, maNguoiDung);
    }
}