package com.cinema.models.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.cinema.models.repositories.Interface.IDashboardRepository;
import com.cinema.utils.DatabaseConnection;

public class DashboardRepository implements IDashboardRepository {
    private final DatabaseConnection databaseConnection;
    
    public DashboardRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }
    
    @Override
    public Map<LocalDate, DailySalesData> getSalesDataByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<LocalDate, DailySalesData> result = new HashMap<>();
        
        // Khởi tạo map với giá trị mặc định cho tất cả các ngày trong khoảng
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            result.put(date, new DailySalesData(0, 0));
        }
        
        // Lấy dữ liệu doanh thu và số vé bán theo ngày
        String sql = "SELECT DATE(v.ngayDat) as ngay, COUNT(*) as soVe, " +
                     "SUM(CASE " +
                     "    WHEN v.maKhuyenMai IS NOT NULL THEN " +
                     "        CASE " +
                     "            WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (1 - km.giaTriGiam/100) " +
                     "            WHEN km.loaiGiamGia = 'CoDinh' THEN GREATEST(gv.giaVe - km.giaTriGiam, 0) " +
                     "            ELSE gv.giaVe " +
                     "        END " +
                     "    ELSE gv.giaVe " +
                     "END) as doanhThu " +
                     "FROM Ve v " +
                     "JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe " +
                     "LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai " +
                     "WHERE v.trangThai = 'paid' AND DATE(v.ngayDat) BETWEEN ? AND ? " +
                     "GROUP BY DATE(v.ngayDat) " +
                     "ORDER BY ngay";
                     
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("ngay").toLocalDate();
                    int tickets = rs.getInt("soVe");
                    double revenue = rs.getDouble("doanhThu");
                    
                    result.put(date, new DailySalesData(tickets, revenue));
                }
            }
        }
        
        return result;
    }
    
    @Override
    public int countTotalCustomers() throws SQLException {
        // Đếm số lượng khách hàng đã mua vé
        String sql = "SELECT COUNT(DISTINCT maKhachHang) FROM HoaDon WHERE maKhachHang IS NOT NULL";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    @Override
    public SummaryData getSummaryData(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        int ticketsSold = 0;
        double revenue = 0;
        double averageRating = 0;
        
        // Lấy tổng số vé bán và doanh thu
        String sql = "SELECT COUNT(*) as soVe, " +
                     "SUM(CASE " +
                     "    WHEN v.maKhuyenMai IS NOT NULL THEN " +
                     "        CASE " +
                     "            WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (1 - km.giaTriGiam/100) " +
                     "            WHEN km.loaiGiamGia = 'CoDinh' THEN GREATEST(gv.giaVe - km.giaTriGiam, 0) " +
                     "            ELSE gv.giaVe " +
                     "        END " +
                     "    ELSE gv.giaVe " +
                     "END) as doanhThu, " +
                     "(SELECT AVG(dg.diemDanhGia) FROM DanhGia dg " +
                     " JOIN SuatChieu sc2 ON dg.maPhim = sc2.maPhim " +
                     " JOIN Ve v2 ON sc2.maSuatChieu = v2.maSuatChieu " +
                     " WHERE v2.ngayDat BETWEEN ? AND ?) as diemDanhGia " +
                     "FROM Ve v " +
                     "JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe " +
                     "LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai " +
                     "WHERE v.trangThai = 'paid' AND v.ngayDat BETWEEN ? AND ?";
                     
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(endDate));
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(startDate));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ticketsSold = rs.getInt("soVe");
                    revenue = rs.getDouble("doanhThu");
                    averageRating = rs.getDouble("diemDanhGia");
                }
            }
        }
        
        return new SummaryData(ticketsSold, revenue, averageRating);
    }
}