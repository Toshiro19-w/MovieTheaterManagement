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
import java.util.Optional;

public class VeRepository implements IVeRepository {
    private final Connection conn;
    private LocalDate ngayDat;

    public VeRepository() {
        this.conn = DatabaseConnection.getConnection();
    }

    @Override
    public List<Ve> findAll(int page, int pageSize) {
        List<Ve> list = new ArrayList<>();
        String sql = "SELECT * FROM Ve ORDER BY maVe OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, (page - 1) * pageSize);
            stmt.setInt(2, pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToVe(rs));
            }
        } catch (SQLException e) {
            handleException("Lỗi khi lấy danh sách vé", e);
        }
        return list;
    }

    @Override
    public List<Ve> findAll() {
        return List.of();
    }

    @Override
    public Optional<Ve> findById(int maVe) {
        String sql = "SELECT * FROM Ve WHERE maVe = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToVe(rs));
            }
        } catch (SQLException e) {
            handleException("Lỗi khi tìm vé theo ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ve> findByMaSuatChieu(int maSuatChieu) {
        return List.of();
    }

    @Override
    public List<Ve> findByNgayDatBetween(LocalDateTime start, LocalDateTime end) {
        return List.of();
    }

    @Override
    public List<Ve> findByTrangThai(String trangThai) {
        return List.of();
    }

    @Override
    public List<Ve> findByMaKhachHang(int maKhachHang) {
        return List.of();
    }

    @Override
    public List<Ve> findByMaHoaDon(int maHoaDon) {
        return List.of();
    }

    @Override
    public Ve save(Ve ve) {
        if (ve.getMaVe() == 0) {
            return insert(ve);
        } else {
            return update(ve);
        }
    }

    @Override
    public boolean deleteById(int maVe) {
        String sql = "DELETE FROM Ve WHERE maVe = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maVe);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("Lỗi khi xóa vé", e);
            return false;
        }
    }

    @Override
    public boolean updateTrangThai(int maVe, String trangThai) {
        return false;
    }

    @Override
    public long countByTrangThai(String trangThai) {
        return 0;
    }

    // ====================================================================
    // Các phương thức private hỗ trợ
    // ====================================================================

    private Ve insert(Ve ve) {
        String sql = "INSERT INTO Ve (maSuatChieu, maKhachHang, maHoaDon, soGhe, giaVe, trangThai, ngayDat) VALUES"
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setVeParameters(stmt, ve);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ve.setMaVe(rs.getInt(1));
                }
            }
            return ve;
        } catch (SQLException e) {
            handleException("Lỗi khi thêm phim mới", e);
            throw new RuntimeException("Không thể thêm phim", e);
        }
    }

    private Ve update(Ve ve) {
        String sql = "UPDATE Phim SET tenPhim=?, maTheLoai=?, thoiLuong=?, "
                + "ngayKhoiChieu=?, nuocSanXuat=?, dinhDang=?, moTa=?, daoDien=? "
                + "WHERE maPhim=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setVeParameters(stmt, ve);
            stmt.setInt(9, ve.getMaVe());

            stmt.executeUpdate();
            return ve;
        } catch (SQLException e) {
            handleException("Lỗi khi cập nhật phim", e);
            throw new RuntimeException("Không thể cập nhật phim", e);
        }
    }

    private void setVeParameters(PreparedStatement stmt, Ve ve) throws SQLException {
        stmt.setInt(1, ve.getMaVe());
        stmt.setInt(2, ve.getMaSuatChieu());
        stmt.setInt(3, ve.getMaKhachHang());
        stmt.setInt(4, ve.getMaHoaDon());
        stmt.setString(5, ve.getSoGhe());
        stmt.setDouble(6, ve.getGiaVe());
        stmt.setObject(7, ve.getTrangThai());
    }

    private Ve mapResultSetToVe(ResultSet rs) throws SQLException {
        return new Ve(
                rs.getInt("maVe"),
                rs.getInt("maSuatChieu"),
                rs.getInt("maKhachHang"),
                rs.getInt("maHoaDon"),
                rs.getString("soGhe"),
                rs.getDouble("giaVe"),
                (TrangThaiVe.fromString(rs.getString("trangThai"))),
                ngayDat = Optional.ofNullable(rs.getDate("ngayDat"))
                .map(java.sql.Date::toLocalDate)
                .orElse(null)
        );
    }

    private void handleException(String message, SQLException e) {
        System.err.println(message + ": " + e.getMessage());
        // Có thể thêm ghi log vào file ở đây
        e.printStackTrace();
    }
}