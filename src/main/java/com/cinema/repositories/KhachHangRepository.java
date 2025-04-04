//package com.cinema.repositories;
//
//import com.cinema.models.KhachHang;
//import com.cinema.utils.DatabaseConnection;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class KhachHangRepository implements IKhachHangRepository{
//    public KhachHangRepository(DatabaseConnection databaseConnection){
//        super();
//    }
//
//    public List<KhachHang> getAllKhachHang() {
//        List<KhachHang> list = new ArrayList<>();
//        String query = "SELECT \n" + "nd.maNguoiDung,\n" + "kh.maKhachHang,\n" + "nd.hoTen,\n" +
//                "nd.soDienThoai,\n" + "nd.email,\n" + "kh.diemTichLuy\n" +
//                "FROM \n" + "NguoiDung nd\n" +
//                "JOIN \n" + "KhachHang kh ON nd.maNguoiDung = kh.maNguoiDung\n" +
//                "WHERE \n" + "nd.loaiNguoiDung = 'KhachHang';";
//        try (Connection conn = DatabaseConnection.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//            while (rs.next()) {
//                KhachHang khachHang = new KhachHang();
//                khachHang.setMaNguoiDung(rs.getInt("maNguoiDung"));
//                khachHang.setMaKhachHang(rs.getInt("maKhachHang"));
//                khachHang.setHoTen(rs.getString("hoTen"));
//                khachHang.setSoDienThoai(rs.getString("soDienThoai"));
//                khachHang.setEmail(rs.getString("email"));
//                khachHang.setDiemTichLuy(rs.getInt("diemTichLuy"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//    public int getIdByEmail(String email) {
//        String query = "SELECT maKhachHang FROM KhachHang WHERE email = ?";
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//            stmt.setString(1, email);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt("maKhachHang");
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }
//
//    public static boolean login(String email, String matKhau) {
//        String query = "SELECT matKhau FROM KhachHang WHERE email = ?";
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//            stmt.setString(1, email);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    String hashedPassword = rs.getString("matKhau");
//                    return matKhau.equals(hashedPassword);
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//}