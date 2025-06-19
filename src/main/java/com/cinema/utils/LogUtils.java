package com.cinema.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.cinema.controllers.ActivityLogController;

/**
 * Lớp tiện ích để quản lý việc ghi log hoạt động trong hệ thống
 * TODO: Hỗ trợ cho nhiều loại log khác nhau, không chỉ giới hạn ở phim
 */
public class LogUtils {
    private static final Logger LOGGER = Logger.getLogger(LogUtils.class.getName());
    private static volatile ActivityLogController activityLogController;
    private static final Object LOCK = new Object();
    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY = 1000; // 1 second
    private static final String BACKUP_LOG_DIR = "logs";
    private static final BlockingQueue<LogEntry> logQueue;
    private static final SimpleDateFormat dateFormat;
    private static final Thread logProcessor;
    
    static {
        logQueue = new ArrayBlockingQueue<>(1000);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        setupFileLogger();
        logProcessor = createLogProcessor();
        logProcessor.start();
    }
    
    // Các đối tượng trong hệ thống
    public static final String OBJ_PHIM = "phim";
    public static final String OBJ_SUAT_CHIEU = "suất chiếu";
    public static final String OBJ_VE = "vé";
    public static final String OBJ_KHACH_HANG = "khách hàng";
    public static final String OBJ_NHAN_VIEN = "nhân viên";
    public static final String OBJ_HOA_DON = "hoá đơn";

    // Actions cho các đối tượng
    private static final String ACTION_THEM_PHIM = "Thêm phim mới";
    private static final String ACTION_CAP_NHAT_PHIM = "Cập nhật thông tin phim";
    private static final String ACTION_XOA_PHIM = "Xóa phim";

    /**
     * Class để đóng gói thông tin log
     */
    private static class LogEntry {
        final String loaiHoatDong;
        final String moTa;
        final int maNguoiDung;
        final long timestamp;

        LogEntry(String loaiHoatDong, String moTa, int maNguoiDung) {
            this.loaiHoatDong = loaiHoatDong;
            this.moTa = moTa;
            this.maNguoiDung = maNguoiDung;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static void setupFileLogger() {
        try {
            File logDir = new File(BACKUP_LOG_DIR);
            if (!logDir.exists() && !logDir.mkdirs()) {
                System.err.println("Không thể tạo thư mục logs");
                return;
            }
            
            FileHandler fileHandler = new FileHandler(
                BACKUP_LOG_DIR + "/activity_%g.log",
                5242880, // 5MB mỗi file
                5,       // Tối đa 5 file
                true    // Append mode
            );
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Không thể thiết lập file logger: " + e.getMessage());
        }
    }

    private static Thread createLogProcessor() {
        Thread processor = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    LogEntry entry = logQueue.poll(1, TimeUnit.SECONDS);
                    if (entry != null) {
                        processLogEntry(entry);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "LogProcessor");
        processor.setDaemon(true);
        return processor;
    }

    private static void processLogEntry(LogEntry entry) {
        boolean success = false;
        Exception lastException = null;

        for (int i = 0; i < MAX_RETRY && !success; i++) {
            try {
                synchronized (LOCK) {
                    if (activityLogController == null) {
                        activityLogController = new ActivityLogController();
                    }
                    activityLogController.addLog(entry.loaiHoatDong, entry.moTa, entry.maNguoiDung);
                }
                success = true;
                LOGGER.info(String.format("Log ghi thành công: [%s] %s (User: %d)",
                    entry.loaiHoatDong, entry.moTa, entry.maNguoiDung));
            } catch (Exception e) {
                lastException = e;
                LOGGER.warning("Lần thử " + (i + 1) + " thất bại: " + e.getMessage());
                if (i < MAX_RETRY - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY * (i + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!success) {
            writeToBackupFile(entry, lastException);
        }
    }

    private static void writeToBackupFile(LogEntry entry, Exception dbException) {
        String logMessage = String.format("%s - [%s] %s (User: %d) - DB Error: %s%n",
            dateFormat.format(new Date(entry.timestamp)),
            entry.loaiHoatDong,
            entry.moTa,
            entry.maNguoiDung,
            dbException.getMessage());

        File backupFile = new File(BACKUP_LOG_DIR + "/backup_" + 
            new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

        try (FileWriter writer = new FileWriter(backupFile, true)) {
            writer.write(logMessage);
            LOGGER.info("Log đã được ghi vào file backup: " + backupFile.getPath());
        } catch (IOException e) {
            LOGGER.severe("Không thể ghi log vào file backup: " + e.getMessage());
        }
    }

    /**
     * Phương thức chung để ghi log
     */
    private static void ghiLog(String loaiHoatDong, String moTa, int maNguoiDung) {
        try {
            LogEntry entry = new LogEntry(loaiHoatDong, moTa, maNguoiDung);
            if (!logQueue.offer(entry)) {
                LOGGER.warning("Queue log đầy, ghi trực tiếp vào file backup");
                writeToBackupFile(entry, new Exception("Queue full"));
            }
        } catch (Exception e) {
            LOGGER.severe("Lỗi nghiêm trọng khi ghi log: " + e.getMessage());
        }
    }

    // Các phương thức log cho Phim
    public static void logThemPhim(int  maPhim, String tenPhim, int maNguoiDung) {
        String chiTiet = String.format("Thêm phim mới - Mã: %s, Tên: %s", maPhim, tenPhim);
        ghiLog(ACTION_THEM_PHIM, chiTiet, maNguoiDung);
    }

    public static void logCapNhatPhim(int maPhim, String tenPhim, String thongTinCapNhat, int maNguoiDung) {
        String chiTiet = String.format("Cập nhật phim - Mã: %s, Tên: %s, Chi tiết: %s", 
            maPhim, tenPhim, thongTinCapNhat);
        ghiLog(ACTION_CAP_NHAT_PHIM, chiTiet, maNguoiDung);
    }

    public static void logXoaPhim(int maPhim, String tenPhim, int maNguoiDung) {
        String chiTiet = String.format("Xóa phim - Mã: %s, Tên: %s", maPhim, tenPhim);
        ghiLog(ACTION_XOA_PHIM, chiTiet, maNguoiDung);
    }

    /**
     * Đóng connection và dọn dẹp resources
     */
    public static void shutdown() {
        if (logProcessor != null) {
            logProcessor.interrupt();
        }
        
        synchronized (LOCK) {
            if (activityLogController != null) {
                try {
                    activityLogController.close();
                    activityLogController = null;
                } catch (Exception e) {
                    LOGGER.warning("Lỗi khi đóng ActivityLogController: " + e.getMessage());
                }
            }
        }
    }
}
