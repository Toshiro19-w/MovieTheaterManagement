//package com.cinema.repositories;
//
//import com.cinema.models.SuatChieu;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SuatChieuRepository extends BaseRepository {
//    public SuatChieuRepository(Connection conn){
//        super(conn);
//    }
//
//    @Override
//    public List<SuatChieu> getAllSuatChieu() {
//        List<SuatChieu> list = new ArrayList<>();
//        String sql = "SELECT sc.*, p.tenPhim, pc.tenPhong, p.thoiLuong, p.dinhDang " +
//                "FROM SuatChieu sc " +
//                "JOIN Phim p ON sc.maPhim = p.maPhim " +
//                "JOIN PhongChieu pc ON sc.maPhong = pc.maPhong";
//
//        try (PreparedStatement stmt = conn.prepareStatement(sql);
//             ResultSet rs = stmt.executeQuery()) {
//
//            while (rs.next()) {
//                SuatChieu sc = new SuatChieu();
//                sc.setMaSuatChieu(rs.getInt("maSuatChieu"));
//                sc.setMaPhim(rs.getInt("maPhim"));
//                sc.setMaPhong(rs.getInt("maPhong"));
//                sc.setNgayGioChieu(rs.getTimestamp("ngayGioChieu").toLocalDateTime());
//                sc.setTenPhim(rs.getString("tenPhim"));
//                sc.setTenPhong(rs.getString("tenPhong"));
//                sc.setThoiLuongPhim(rs.getInt("thoiLuong"));
//                sc.setDinhDangPhim(rs.getString("dinhDang"));
//                list.add(sc);
//            }
//        } catch (SQLException e) {
//            handleSQLException(e, "Lỗi khi lấy danh sách suất chiếu");
//        }
//        return list;
//    }
//
//    @Override
//    public boolean themSuatChieu(SuatChieu suatChieu) {
//        String sql = "INSERT INTO SuatChieu (maPhim, maPhong, ngayGioChieu) VALUES (?, ?, ?)";
//
//        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            stmt.setInt(1, suatChieu.getMaPhim());
//            stmt.setInt(2, suatChieu.getMaPhong());
//            stmt.setTimestamp(3, Timestamp.valueOf(suatChieu.getNgayGioChieu()));
//
//            int affectedRows = stmt.executeUpdate();
//            if (affectedRows > 0) {
//                try (ResultSet rs = stmt.getGeneratedKeys()) {
//                    if (rs.next()) {
//                        suatChieu.setMaSuatChieu(rs.getInt(1));
//                        return true;
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            handleSQLException(e, "Lỗi khi thêm suất chiếu");
//        }
//        return false;
//    }
//
//    private void handleSQLException(SQLException e, String message) {
//        System.err.println(message + ": " + e.getMessage());
//        e.printStackTrace();
//    }
//
//    @Override
//    public List findAll() throws SQLException {
//        return List.of();
//    }
//
//    @Override
//    public Object findById(int id) throws SQLException {
//        return null;
//    }
//
//    @Override
//    public Object save(Object entity) throws SQLException {
//        return null;
//    }
//
//    @Override
//    public Object update(Object entity) throws SQLException {
//        return null;
//    }
//
//    @Override
//    public void delete(int id) throws SQLException {
//
//    }
//}