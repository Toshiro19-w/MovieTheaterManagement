package com.cinema.models.repositories;

import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.utils.DatabaseConnection;

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
                SELECT\s
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

    public List<Ve> findByHoaDon(int maHoaDon) throws SQLException {
        List<Ve> list = new ArrayList<>();
        String sql = "SELECT * FROM Ve WHERE maHoaDon = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maHoaDon);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Ve(
                            rs.getInt("maVe"),
                            rs.getInt("maSuatChieu"),
                            rs.getInt("maPhong"),
                            rs.getString("soGhe"),
                            rs.getObject("maHoaDon") != null ? rs.getInt("maHoaDon") : null,
                            rs.getBigDecimal("giaVe"),
                            TrangThaiVe.fromString(rs.getString("trangThai")),
                            rs.getTimestamp("ngayDat") != null ? rs.getTimestamp("ngayDat").toLocalDateTime() : null
                    ));
                }
            }
        }
        return list;
    }

    public List<Ve> findBySoGhe(String soGhe) throws SQLException {
        List<Ve> veList = new ArrayList<>();
        String sql = """
                SELECT\s
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
        String sql = "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Lấy maSuatChieu và maPhong dựa trên tenPhong, ngayGioChieu, tenPhim
            Integer maSuatChieu = getMaSuatChieu(ve.getTenPhim(), ve.getNgayGioChieu());
            Integer maPhong = getMaPhong(ve.getTenPhong());
            if (maSuatChieu == null || maPhong == null) {
                throw new SQLException("Không tìm thấy suất chiếu hoặc phòng chiếu tương ứng.");
            }

            stmt.setInt(1, maSuatChieu);
            stmt.setInt(2, maPhong);
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
        String sql = "UPDATE Ve SET maSuatChieu = ?, maPhong = ?, soGhe = ?, maHoaDon = ?, giaVe = ?, trangThai = ?, ngayDat = ? WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Lấy maSuatChieu và maPhong dựa trên tenPhong, ngayGioChieu, tenPhim
            Integer maSuatChieu = getMaSuatChieu(ve.getTenPhim(), ve.getNgayGioChieu());
            Integer maPhong = getMaPhong(ve.getTenPhong());
            if (maSuatChieu == null || maPhong == null) {
                throw new SQLException("Không tìm thấy suất chiếu hoặc phòng chiếu tương ứng.");
            }

            stmt.setInt(1, maSuatChieu);
            stmt.setInt(2, maPhong);
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

    private Integer getMaSuatChieu(String tenPhim, LocalDateTime ngayGioChieu) throws SQLException {
        String sql = "SELECT s.maSuatChieu FROM SuatChieu s JOIN Phim p ON s.maPhim = p.maPhim " +
                "WHERE p.tenPhim = ? AND s.ngayGioChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhim);
            stmt.setTimestamp(2, Timestamp.valueOf(ngayGioChieu));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maSuatChieu");
                }
                return null;
            }
        }
    }

    private Integer getMaPhong(String tenPhong) throws SQLException {
        String sql = "SELECT maPhong FROM PhongChieu WHERE tenPhong = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maPhong");
                }
                return null;
            }
        }
    }
}