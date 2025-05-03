package com.cinema.models.repositories;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VeRepository extends BaseRepository<Ve> {

    public VeRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<Ve> findAll() throws SQLException {
        String sql = "SELECT * FROM Ve";
        List<Ve> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                LocalDateTime ngayDat = null;
                if (resultSet.getDate("ngayDat") != null) {
                    ngayDat = resultSet.getDate("ngayDat").toLocalDate().atStartOfDay();
                }

                result.add(new Ve(
                        resultSet.getInt("maVe"),
                        resultSet.getInt("maSuatChieu"),
                        resultSet.getInt("maPhong"),
                        resultSet.getString("soGhe"),
                        resultSet.getObject("maHoaDon", Integer.class),
                        resultSet.getBigDecimal("giaVe"),
                        TrangThaiVe.fromString(resultSet.getString("trangThai")),
                        ngayDat
                ));
            }
            return result;
        }
    }

    public List<Ve> findAllDetail() throws SQLException {
        String sql = """
                SELECT 
                    Ve.maVe,
                    Ve.trangThai,
                    Ve.giaVe,
                    Ve.soGhe,
                    Ve.ngayDat,
                    PhongChieu.tenPhong,
                    SuatChieu.ngayGioChieu,
                    Phim.tenPhim
                FROM Ve
                LEFT JOIN SuatChieu ON Ve.maSuatChieu = SuatChieu.maSuatChieu
                LEFT JOIN PhongChieu ON Ve.maPhong = PhongChieu.maPhong
                LEFT JOIN Phim ON SuatChieu.maPhim = Phim.maPhim
                ORDER BY Ve.maVe;""";

        List<Ve> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                LocalDateTime ngayDat = null, ngayGioChieu = null;
                if (resultSet.getTimestamp("ngayDat") != null) {
                    ngayDat = resultSet.getTimestamp("ngayDat").toLocalDateTime();
                }
                if (resultSet.getTimestamp("ngayGioChieu") != null) {
                    ngayGioChieu = resultSet.getTimestamp("ngayGioChieu").toLocalDateTime();
                }

                Ve ve = new Ve(
                        resultSet.getInt("maVe"),
                        TrangThaiVe.fromString(resultSet.getString("trangThai")),
                        resultSet.getBigDecimal("giaVe"),
                        resultSet.getString("soGhe"),
                        ngayDat,
                        resultSet.getString("tenPhong"),
                        ngayGioChieu,
                        resultSet.getString("tenPhim")
                );
                result.add(ve);
            }
        }
        return result;
    }

    public List<Ve> findBySoGhe(String soGhe) throws SQLException {
        List<Ve> veList = new ArrayList<>();
        String sql = """
                SELECT 
                    Ve.maVe,
                    Ve.trangThai,
                    Ve.giaVe,
                    Ve.soGhe,
                    Ve.ngayDat,
                    PhongChieu.tenPhong,
                    SuatChieu.ngayGioChieu,
                    Phim.tenPhim
                FROM Ve
                LEFT JOIN SuatChieu ON Ve.maSuatChieu = SuatChieu.maSuatChieu
                LEFT JOIN PhongChieu ON Ve.maPhong = PhongChieu.maPhong
                LEFT JOIN Phim ON SuatChieu.maPhim = Phim.maPhim
                WHERE soGhe = ?
                ORDER BY Ve.maVe;""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soGhe);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime ngayDat = null, ngayGioChieu = null;
                if (rs.getTimestamp("ngayDat") != null) {
                    ngayDat = rs.getTimestamp("ngayDat").toLocalDateTime();
                }
                if (rs.getTimestamp("ngayGioChieu") != null) {
                    ngayGioChieu = rs.getTimestamp("ngayGioChieu").toLocalDateTime();
                }

                Ve ve = new Ve(
                        rs.getInt("maVe"),
                        TrangThaiVe.fromString(rs.getString("trangThai")),
                        rs.getBigDecimal("giaVe"),
                        rs.getString("soGhe"),
                        ngayDat,
                        rs.getString("tenPhong"),
                        ngayGioChieu,
                        rs.getString("tenPhim")
                );
                veList.add(ve);
            }
        }
        return veList;
    }

    @Override
    public Ve save(Ve ve) throws SQLException {
        // Kiểm tra tính hợp lệ của maSuatChieu và maPhong
        if (isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }
        if (isPhongExists(ve.getMaPhong())) {
            throw new SQLException("Phòng chiếu với mã " + ve.getMaPhong() + " không tồn tại");
        }

        String sql = "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, ve.getMaPhong());
            stmt.setString(3, ve.getSoGhe());
            stmt.setObject(4, null, Types.INTEGER); // maHoaDon mặc định là null
            stmt.setBigDecimal(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai().toString());
            stmt.setObject(7, ve.getNgayDat() != null ? Timestamp.valueOf(ve.getNgayDat()) : null, Types.TIMESTAMP);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tạo vé thất bại, không có dòng nào được thêm.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ve.setMaVe(generatedKeys.getInt(1));
                    return ve;
                } else {
                    throw new SQLException("Tạo vé thất bại, không lấy được khóa chính.");
                }
            }
        }
    }

    @Override
    public Ve update(Ve ve) throws SQLException {
        // Kiểm tra tính hợp lệ của maSuatChieu và maPhong
        if (isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }
        if (isPhongExists(ve.getMaPhong())) {
            throw new SQLException("Phòng chiếu với mã " + ve.getMaPhong() + " không tồn tại");
        }

        String sql = "UPDATE Ve SET maSuatChieu = ?, maPhong = ?, soGhe = ?, maHoaDon = ?, giaVe = ?, trangThai = ?, ngayDat = ? WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, ve.getMaPhong());
            stmt.setString(3, ve.getSoGhe());
            stmt.setObject(4, null, Types.INTEGER);
            stmt.setBigDecimal(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai().toString());
            stmt.setObject(7, ve.getNgayDat() != null ? Timestamp.valueOf(ve.getNgayDat()) : null, Types.TIMESTAMP);
            stmt.setInt(8, ve.getMaVe());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return ve;
            }
            return null;
        }
    }

    public void updateVeStatus(int maVe, String trangThai, Integer maHoaDon) throws SQLException {
        String sql = "UPDATE Ve SET trangThai = ?, maHoaDon = ? WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trangThai);
            stmt.setObject(2, maHoaDon);
            stmt.setInt(3, maVe);
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        Connection conn = null;
        PreparedStatement hoaDonStmt = null;
        PreparedStatement veStmt = null;
        PreparedStatement chiTietHoaDonStmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Create HoaDon
            String hoaDonSql = "INSERT INTO HoaDon (maKhachHang, ngayLap, tongTien) VALUES (?, NOW(), ?)";
            hoaDonStmt = conn.prepareStatement(hoaDonSql, PreparedStatement.RETURN_GENERATED_KEYS);
            hoaDonStmt.setInt(1, maKhachHang);
            hoaDonStmt.setBigDecimal(2, giaVe); // tongTien is the ticket price for one seat
            hoaDonStmt.executeUpdate();

            // Get generated maHoaDon
            generatedKeys = hoaDonStmt.getGeneratedKeys();
            int maHoaDon;
            if (generatedKeys.next()) {
                maHoaDon = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Không thể lấy mã hóa đơn!");
            }

            // Create Ve
            Ve ve = new Ve(0, maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, TrangThaiVe.BOOKED, LocalDateTime.now());
            veStmt = conn.prepareStatement(
                    "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            veStmt.setInt(1, ve.getMaSuatChieu());
            veStmt.setInt(2, ve.getMaPhong());
            veStmt.setString(3, ve.getSoGhe());
            veStmt.setInt(4, ve.getMaHoaDon());
            veStmt.setBigDecimal(5, ve.getGiaVe());
            veStmt.setString(6, ve.getTrangThai().toString());
            veStmt.setTimestamp(7, java.sql.Timestamp.valueOf(ve.getNgayDat()));
            veStmt.executeUpdate();

            // Get generated maVe
            generatedKeys = veStmt.getGeneratedKeys();
            int maVe;
            if (generatedKeys.next()) {
                maVe = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Không thể lấy mã vé!");
            }

            // Create ChiTietHoaDon
            String chiTietHoaDonSql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES (?, ?)";
            chiTietHoaDonStmt = conn.prepareStatement(chiTietHoaDonSql);
            chiTietHoaDonStmt.setInt(1, maHoaDon);
            chiTietHoaDonStmt.setInt(2, maVe);
            chiTietHoaDonStmt.executeUpdate();

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (hoaDonStmt != null) try { hoaDonStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (veStmt != null) try { veStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (chiTietHoaDonStmt != null) try { chiTietHoaDonStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        String sql = "SELECT giaVe FROM Ve WHERE maSuatChieu = ? AND trangThai = 'available' LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("giaVe");
                }
            }
        }
        return null;
    }

    private boolean isSuatChieuExists(int maSuatChieu) throws SQLException {
        String sql = "SELECT 1 FROM SuatChieu WHERE maSuatChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    private boolean isPhongExists(int maPhong) throws SQLException {
        String sql = "SELECT 1 FROM PhongChieu WHERE maPhong = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }
}