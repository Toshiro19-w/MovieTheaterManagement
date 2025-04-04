package com.cinema.repositories;

import com.cinema.models.Phim;
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

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Ve WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}