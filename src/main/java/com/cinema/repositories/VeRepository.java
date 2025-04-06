package com.cinema.repositories;

import com.cinema.models.Phim;
import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
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
        String sql = "SELECT \n" +
                "    Ve.maVe,\n" +
                "    Ve.trangThai,\n" +
                "    Ve.giaVe,\n" +
                "    Ve.soGhe,\n" +
                "    Ve.ngayDat,\n" +
                "    PhongChieu.tenPhong,\n" +
                "    SuatChieu.ngayGioChieu,\n" +
                "    Phim.tenPhim\n" +
                "FROM Ve\n" +
                "LEFT JOIN SuatChieu ON Ve.maSuatChieu = SuatChieu.maSuatChieu\n" +
                "LEFT JOIN PhongChieu ON Ve.maPhong = PhongChieu.maPhong\n" +
                "LEFT JOIN Phim ON SuatChieu.maPhim = Phim.maPhim\n" +
                "ORDER BY Ve.maVe;";

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


    @Override
    public Ve findById(int id) throws SQLException {
        String sql = "SELECT * FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    LocalDateTime ngayDat = null;
                    if (resultSet.getDate("ngayDat") != null) {
                        ngayDat = resultSet.getDate("ngayDat").toLocalDate().atStartOfDay();
                    }

                    return new Ve(
                            resultSet.getInt("maVe"),
                            resultSet.getInt("maSuatChieu"),
                            resultSet.getInt("maPhong"),
                            resultSet.getString("soGhe"),
                            resultSet.getObject("maHoaDon", Integer.class),
                            resultSet.getBigDecimal("giaVe"),
                            TrangThaiVe.fromString(resultSet.getString("trangThai")),
                            ngayDat
                    );
                }
            }
        }
        return null;
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

    @Override
    public Ve save(Ve ve) throws SQLException {
        String sql = "INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, ve.getMaPhong());
            stmt.setString(3, ve.getSoGhe());
            stmt.setObject(4, ve.getMaHoaDon(), Types.INTEGER); // Xử lý maHoaDon có thể null
            stmt.setBigDecimal(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai().toString());
            stmt.setObject(7, ve.getNgayDat() != null ? Timestamp.valueOf(ve.getNgayDat()) : null, Types.TIMESTAMP);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating Ve failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ve.setMaVe(generatedKeys.getInt(1));
                    return ve;
                } else {
                    throw new SQLException("Creating Ve failed, no generated key obtained.");
                }
            }
        }
    }

    @Override
    public Ve update(Ve ve) throws SQLException {
        String sql = "UPDATE Ve SET maSuatChieu = ?, maPhong = ?, soGhe = ?, maHoaDon = ?, giaVe = ?, trangThai = ?, ngayDat = ? WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, ve.getMaPhong());
            stmt.setString(3, ve.getSoGhe());
            stmt.setObject(4, ve.getMaHoaDon(), Types.INTEGER);
            stmt.setBigDecimal(5, ve.getGiaVe());
            stmt.setString(6, ve.getTrangThai().toString());
            stmt.setObject(7, ve.getNgayDat() != null ? Timestamp.valueOf(ve.getNgayDat()) : null, Types.TIMESTAMP);
            stmt.setInt(8, ve.getMaVe());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return ve;
            }
        }
        return null;
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
}