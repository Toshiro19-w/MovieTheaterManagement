package com.cinema.repositories;

import com.cinema.models.Phim;
import com.cinema.utils.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PhimRepository implements IPhimRepository {
    private final Connection conn;

    public PhimRepository() {
        this.conn = DatabaseConnection.getConnection();
    }

    // Lấy tất cả phim (có phân trang)
    public List<Phim> findAll(int page, int pageSize) {
        List<Phim> list = new ArrayList<>();
        String sql = "SELECT * FROM Phim ORDER BY maPhim OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, (page - 1) * pageSize);
            stmt.setInt(2, pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToPhim(rs));
            }
        } catch (SQLException e) {
            handleException("Lỗi khi lấy danh sách phim", e);
        }
        return list;
    }

    // Tìm phim theo ID (trả về Optional)
    public Optional<Phim> findById(int maPhim) {
        String sql = "SELECT * FROM Phim WHERE maPhim = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToPhim(rs));
            }
        } catch (SQLException e) {
            handleException("Lỗi khi tìm phim theo ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Phim> findAll() {
        return List.of();
    }

    // Lưu phim (thêm mới hoặc cập nhật)
    public Phim save(Phim phim) {
        if (phim.getMaPhim() == 0) {
            return insert(phim);
        } else {
            return update(phim);
        }
    }

    // Xóa phim theo ID
    public boolean deleteById(int maPhim) {
        String sql = "DELETE FROM Phim WHERE maPhim = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("Lỗi khi xóa phim", e);
            return false;
        }
    }

    // Tìm phim theo tên (không phân biệt hoa thường)
    public List<Phim> findByTenPhimContaining(String keyword) {
        List<Phim> list = new ArrayList<>();
        String sql = "SELECT * FROM Phim WHERE LOWER(tenPhim) LIKE LOWER(?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToPhim(rs));
            }
        } catch (SQLException e) {
            handleException("Lỗi khi tìm phim theo tên", e);
        }
        return list;
    }

    @Override
    public List<Phim> findByNgayKhoiChieuBetween(LocalDate start, LocalDate end) {
        return List.of();
    }

    // ====================================================================
    // Các phương thức private hỗ trợ
    // ====================================================================

    private Phim insert(Phim phim) {
        String sql = "INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, "
                + "nuocSanXuat, dinhDang, moTa, daoDien) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setPhimParameters(stmt, phim);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    phim.setMaPhim(rs.getInt(1));
                }
            }
            return phim;
        } catch (SQLException e) {
            handleException("Lỗi khi thêm phim mới", e);
            throw new RuntimeException("Không thể thêm phim", e);
        }
    }

    private Phim update(Phim phim) {
        String sql = "UPDATE Phim SET tenPhim=?, maTheLoai=?, thoiLuong=?, "
                + "ngayKhoiChieu=?, nuocSanXuat=?, dinhDang=?, moTa=?, daoDien=? "
                + "WHERE maPhim=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setPhimParameters(stmt, phim);
            stmt.setInt(9, phim.getMaPhim());

            stmt.executeUpdate();
            return phim;
        } catch (SQLException e) {
            handleException("Lỗi khi cập nhật phim", e);
            throw new RuntimeException("Không thể cập nhật phim", e);
        }
    }

    private void setPhimParameters(PreparedStatement stmt, Phim phim) throws SQLException {
        stmt.setString(1, phim.getTenPhim());
        stmt.setInt(2, phim.getMaTheLoai());
        stmt.setInt(3, phim.getThoiLuong());
        stmt.setDate(4, Date.valueOf(phim.getNgayKhoiChieu()));
        stmt.setString(5, phim.getNuocSanXuat());
        stmt.setString(6, phim.getDinhDang());
        stmt.setString(7, phim.getMoTa());
        stmt.setString(8, phim.getDaoDien());
    }

    private Phim mapResultSetToPhim(ResultSet rs) throws SQLException {
        return new Phim(
                rs.getInt("maPhim"),
                rs.getString("tenPhim"),
                rs.getInt("maTheLoai"),
                rs.getInt("thoiLuong"),
                rs.getDate("ngayKhoiChieu").toLocalDate(),
                rs.getString("nuocSanXuat"),
                rs.getString("dinhDang"),
                rs.getString("moTa"),
                rs.getString("daoDien")
        );
    }

    private void handleException(String message, SQLException e) {
        System.err.println(message + ": " + e.getMessage());
        // Có thể thêm ghi log vào file ở đây
        e.printStackTrace();
    }
}