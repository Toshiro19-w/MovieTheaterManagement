package com.cinema.models;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ActivityLog {
    private int maLog; // Đổi tên từ id sang maLog để khớp với tên cột trong DB
    private String loaiHoatDong;
    private String moTa;
    private int maNguoiDung;
    private String tenNguoiDung; // Thêm tên người dùng từ bảng NguoiDung
    private LocalDateTime thoiGian;
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy (Z)", new Locale("vi", "VN"));

    public ActivityLog() {
    }

    public ActivityLog(int maLog, String loaiHoatDong, String moTa, int maNguoiDung, LocalDateTime thoiGian) {
        this.maLog = maLog;
        this.loaiHoatDong = loaiHoatDong;
        this.moTa = moTa;
        this.maNguoiDung = maNguoiDung;
        this.thoiGian = thoiGian;
    }

    // Getter và Setter cho maLog
    public int getMaLog() {
        return maLog;
    }

    public void setMaLog(int maLog) {
        this.maLog = maLog;
    }

    // Getter và Setter cho tenNguoiDung
    public String getTenNguoiDung() {
        return tenNguoiDung;
    }

    public void setTenNguoiDung(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
    }

    // Các getter và setter hiện tại giữ nguyên, chỉ đổi getId/setId sang dùng maLog
    public int getId() {
        return getMaLog();
    }

    public void setId(int id) {
        setMaLog(id);
    }

    public String getLoaiHoatDong() {
        return loaiHoatDong;
    }

    public void setLoaiHoatDong(String loaiHoatDong) {
        this.loaiHoatDong = loaiHoatDong;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    /**
     * Lấy thời gian đã được format với timezone
     * @return String thời gian định dạng "HH:mm:ss dd/MM/yyyy (+0700)"
     */
    public String getFormattedTime() {
        if (thoiGian == null) {
            return "N/A";
        }
        // Chuyển LocalDateTime sang ZonedDateTime với timezone Việt Nam
        ZonedDateTime vietnamTime = thoiGian.atZone(VIETNAM_ZONE);
        return vietnamTime.format(FORMATTER);
    }

    @Override
    public String toString() {
        String userInfo = tenNguoiDung != null ? tenNguoiDung : "User " + maNguoiDung;
        return String.format("[%s] %s - Mã log: %d (Người dùng: %s, Thời gian: %s)",
            loaiHoatDong, 
            moTa, 
            maLog, 
            userInfo,
            getFormattedTime());
    }
}