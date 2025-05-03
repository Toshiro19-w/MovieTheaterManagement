package com.cinema.models.repositories;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.models.repositories.Interface.IVeRepository;
import com.cinema.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VeRepository extends BaseRepository<Ve> implements IVeRepository {

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
                if (resultSet.getTimestamp("ngayDat") != null) {
                    ngayDat = resultSet.getTimestamp("ngayDat").toLocalDateTime();
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
        if (isSeatTaken(ve.getMaSuatChieu(), ve.getSoGhe())) {
            throw new SQLException("Ghế " + ve.getSoGhe() + " đã được đặt cho suất chiếu " + ve.getMaSuatChieu());
        }

        if (isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }
        if (isPhongExists(ve.getMaPhong())) {
            throw new SQLException("Phòng chiếu với mã " + ve.getMaPhong() + " không tồn tại");
        }

        String sql = "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, ve.getMaPhong());
            stmt.setString(3, ve.getSoGhe());
            stmt.setBigDecimal(4, ve.getGiaVe());
            stmt.setString(5, ve.getTrangThai().toString());
            stmt.setObject(6, ve.getNgayDat() != null ? Timestamp.valueOf(ve.getNgayDat()) : null, Types.TIMESTAMP);

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
        String checkSql = "SELECT maVe FROM Ve WHERE maSuatChieu = ? AND soGhe = ? AND trangThai != 'CANCELLED' AND maVe != ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, ve.getMaSuatChieu());
            checkStmt.setString(2, ve.getSoGhe());
            checkStmt.setInt(3, ve.getMaVe());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new SQLException("Ghế " + ve.getSoGhe() + " đã được đặt cho suất chiếu " + ve.getMaSuatChieu());
            }
        }

        if (isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }
        if (isPhongExists(ve.getMaPhong())) {
            throw new SQLException("Phòng chiếu với mã " + ve.getMaPhong() + " không tồn tại");
        }

        String sql = "UPDATE Ve SET maSuatChieu = ?, maPhong = ?, soGhe = ?, giaVe = ?, trangThai = ?, ngayDat = ? WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, ve.getMaPhong());
            stmt.setString(3, ve.getSoGhe());
            stmt.setBigDecimal(4, ve.getGiaVe());
            stmt.setString(5, ve.getTrangThai().toString());
            stmt.setObject(6, ve.getNgayDat() != null ? Timestamp.valueOf(ve.getNgayDat()) : null, Types.TIMESTAMP);
            stmt.setInt(7, ve.getMaVe());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return ve;
            }
            return null;
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Không tìm thấy vé với mã: " + id);
            }
        }
    }

    public void cancelVe(int maVe) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Lấy maSuatChieu trước khi xóa vé
                int maSuatChieu = getMaSuatChieuFromVe(maVe, conn);

                // Xóa vé khỏi CSDL
                String deleteVeSql = "DELETE FROM Ve WHERE maVe = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteVeSql)) {
                    deleteStmt.setInt(1, maVe);
                    int affectedRows = deleteStmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Không tìm thấy vé với mã: " + maVe);
                    }
                }

                // Tăng soSuatChieu trong SuatChieu
                String updateSuatChieuSql = "UPDATE SuatChieu SET soSuatChieu = soSuatChieu + 1 WHERE maSuatChieu = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSuatChieuSql)) {
                    updateStmt.setInt(1, maSuatChieu);
                    updateStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Lỗi khi hủy vé: " + e.getMessage(), e);
            }
        }
    }

    public Ve findVeByMaVe(int maVe) throws SQLException {
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
                WHERE Ve.maVe = ?""";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LocalDateTime ngayDat = null, ngayGioChieu = null;
                if (rs.getTimestamp("ngayDat") != null) {
                    ngayDat = rs.getTimestamp("ngayDat").toLocalDateTime();
                }
                if (rs.getTimestamp("ngayGioChieu") != null) {
                    ngayGioChieu = rs.getTimestamp("ngayGioChieu").toLocalDateTime();
                }

                return new Ve(
                        rs.getInt("maVe"),
                        TrangThaiVe.fromString(rs.getString("trangThai")),
                        rs.getBigDecimal("giaVe"),
                        rs.getString("soGhe"),
                        ngayDat,
                        rs.getString("tenPhong"),
                        ngayGioChieu,
                        rs.getString("tenPhim")
                );
            }
        }
        return null; // Nếu không tìm thấy vé
    }

    public void datVe(int maSuatChieu, Integer maPhong, String soGhe, BigDecimal giaVe, int maKhachHang) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String checkSuatChieuSql = "SELECT soSuatChieu FROM SuatChieu WHERE maSuatChieu = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSuatChieuSql)) {
                    checkStmt.setInt(1, maSuatChieu);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            int soSuatChieu = rs.getInt("soSuatChieu");
                            if (soSuatChieu <= 0) {
                                throw new SQLException("Không còn suất chiếu khả dụng cho mã suất chiếu: " + maSuatChieu);
                            }
                        } else {
                            throw new SQLException("Suất chiếu không tồn tại: " + maSuatChieu);
                        }
                    }
                }

                if (isSeatTaken(maSuatChieu, soGhe)) {
                    throw new SQLException("Ghế " + soGhe + " đã được đặt cho suất chiếu " + maSuatChieu);
                }

                String insertVeSql = "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement veStmt = conn.prepareStatement(insertVeSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    veStmt.setInt(1, maSuatChieu);
                    veStmt.setInt(2, maPhong);
                    veStmt.setString(3, soGhe);
                    veStmt.setBigDecimal(4, giaVe);
                    veStmt.setString(5, TrangThaiVe.BOOKED.toString());
                    veStmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                    veStmt.executeUpdate();
                }

                String updateSuatChieuSql = "UPDATE SuatChieu SET soSuatChieu = soSuatChieu - 1 WHERE maSuatChieu = ?";
                try (PreparedStatement updateSuatChieuStmt = conn.prepareStatement(updateSuatChieuSql)) {
                    updateSuatChieuStmt.setInt(1, maSuatChieu);
                    updateSuatChieuStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Lỗi khi đặt vé: " + e.getMessage(), e);
            }
        }
    }

    public int confirmPayment(int maVe, int maKhachHang) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Lấy giá vé từ Ve
                BigDecimal giaVe = getGiaVeFromVe(maVe, conn);

                // Tạo hóa đơn
                String hoaDonSql = "INSERT INTO HoaDon (maKhachHang, ngayLap, tongTien) VALUES (?, NOW(), ?)";
                int maHoaDonGenerated;
                try (PreparedStatement hoaDonStmt = conn.prepareStatement(hoaDonSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    hoaDonStmt.setInt(1, maKhachHang);
                    hoaDonStmt.setBigDecimal(2, giaVe);
                    hoaDonStmt.executeUpdate();

                    try (ResultSet generatedKeys = hoaDonStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            maHoaDonGenerated = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Không thể lấy mã hóa đơn!");
                        }
                    }
                }

                // Cập nhật vé với maHoaDon và trạng thái PAID
                String updateVeSql = "UPDATE Ve SET trangThai = ?, maHoaDon = ? WHERE maVe = ?";
                try (PreparedStatement updateVeStmt = conn.prepareStatement(updateVeSql)) {
                    updateVeStmt.setString(1, TrangThaiVe.PAID.toString());
                    updateVeStmt.setInt(2, maHoaDonGenerated);
                    updateVeStmt.setInt(3, maVe);
                    int affectedRows = updateVeStmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Không tìm thấy vé với mã: " + maVe);
                    }
                }

                // Tạo ChiTietHoaDon
                String chiTietHoaDonSql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES (?, ?)";
                try (PreparedStatement chiTietHoaDonStmt = conn.prepareStatement(chiTietHoaDonSql)) {
                    chiTietHoaDonStmt.setInt(1, maHoaDonGenerated);
                    chiTietHoaDonStmt.setInt(2, maVe);
                    chiTietHoaDonStmt.executeUpdate();
                }

                conn.commit();
                return maHoaDonGenerated;
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Lỗi khi xác nhận thanh toán: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public BigDecimal getGiaVeFromVe(int maVe, Connection conn) throws SQLException {
        String sql = "SELECT giaVe FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("giaVe");
                }
                throw new SQLException("Không tìm thấy giá vé cho mã vé: " + maVe);
            }
        }
    }

    private int getMaSuatChieuFromVe(int maVe, Connection conn) throws SQLException {
        String sql = "SELECT maSuatChieu FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maSuatChieu");
                }
                throw new SQLException("Không tìm thấy suất chiếu cho vé với mã: " + maVe);
            }
        }
    }

    public BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        String sql = "SELECT giaVe FROM Ve WHERE maSuatChieu = ? LIMIT 1";
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

    public int getMaVeFromBooking(int maSuatChieu, String soGhe, int maKhachHang) throws SQLException {
        String sql = "SELECT maVe FROM Ve WHERE maSuatChieu = ? AND soGhe = ? AND maHoaDon IS NULL ORDER BY ngayDat DESC LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maVe");
                }
                throw new SQLException("Không tìm thấy mã vé vừa đặt!");
            }
        }
    }

    public int getMaHoaDonFromVe(int maVe) throws SQLException {
        String sql = "SELECT maHoaDon FROM Ve WHERE maVe = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maHoaDon");
                }
                throw new SQLException("Không tìm thấy hóa đơn cho vé: " + maVe);
            }
        }
    }

    public boolean isSuatChieuExists(int maSuatChieu) throws SQLException {
        String sql = "SELECT 1 FROM SuatChieu WHERE maSuatChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    public boolean isPhongExists(int maPhong) throws SQLException {
        String sql = "SELECT 1 FROM PhongChieu WHERE maPhong = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    public boolean isSeatTaken(int maSuatChieu, String soGhe) throws SQLException {
        String sql = "SELECT maVe FROM Ve WHERE maSuatChieu = ? AND soGhe = ? AND trangThai != 'CANCELLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}