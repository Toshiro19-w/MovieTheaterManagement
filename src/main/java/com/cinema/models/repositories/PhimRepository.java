package com.cinema.models.repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.Phim;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.PaginationResult;

public class PhimRepository extends BaseRepository<Phim> {
    public PhimRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<Phim> findAll() throws SQLException {
        return findAllPaginated(1, Integer.MAX_VALUE).getData();
    }
    
    public PaginationResult<Phim> findAllPaginated(int page, int pageSize) throws SQLException {
        List<Phim> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        
        // Đếm tổng số phim
        String countSql = "SELECT COUNT(*) FROM Phim p JOIN TheLoaiPhim tl ON p.maTheLoai = tl.maTheLoai";
        int totalItems = 0;
        try (Statement countStmt = conn.createStatement(); 
             ResultSet countRs = countStmt.executeQuery(countSql)) {
            if (countRs.next()) {
                totalItems = countRs.getInt(1);
            }
        }
        
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        // Lấy dữ liệu phim theo trang
        String sql = """
                     SELECT p.maPhim, p.tenPhim, p.maTheLoai, tl.tenTheLoai, p.thoiLuong, p.ngayKhoiChieu, 
                     p.nuocSanXuat, p.kieuPhim, p.moTa, p.daoDien, p.duongDanPoster, p.trangThai
                     FROM Phim p
                     JOIN TheLoaiPhim tl ON p.maTheLoai = tl.maTheLoai
                     LEFT JOIN SuatChieu sc ON p.maPhim = sc.maPhim
                     GROUP BY p.maPhim, p.tenPhim, p.maTheLoai, tl.tenTheLoai, p.thoiLuong, p.ngayKhoiChieu,
                     p.nuocSanXuat, p.kieuPhim, p.moTa, p.daoDien, p.duongDanPoster, p.trangThai
                     ORDER BY p.ngayKhoiChieu DESC, p.tenPhim
                     LIMIT ? OFFSET ?""";
                     
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Phim phim = new Phim();
                phim.setMaPhim(rs.getInt("maPhim"));
                phim.setTenPhim(rs.getString("tenPhim"));
                phim.setMaTheLoai(rs.getInt("maTheLoai"));
                phim.setTenTheLoai(rs.getString("tenTheLoai"));
                phim.setThoiLuong(rs.getInt("thoiLuong"));
                phim.setNgayKhoiChieu(rs.getDate("ngayKhoiChieu") != null
                        ? rs.getDate("ngayKhoiChieu").toLocalDate()
                        : null);
                phim.setNuocSanXuat(rs.getString("nuocSanXuat"));
                phim.setKieuPhim(rs.getString("kieuPhim"));
                phim.setMoTa(rs.getString("moTa"));
                phim.setDaoDien(rs.getString("daoDien"));
                phim.setDuongDanPoster(rs.getString("duongDanPoster"));
                phim.setTrangThai(rs.getString("trangThai"));

                list.add(phim);
            }
        }
        
        return new PaginationResult<>(list, page, totalPages, pageSize, totalItems);
    }

    
    public Phim findById(int id) throws SQLException {
        String sql = "SELECT * FROM Phim WHERE maPhim = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Phim phim = new Phim();
                phim.setMaPhim(rs.getInt("maPhim"));
                phim.setTenPhim(rs.getString("tenPhim"));
                phim.setMaTheLoai(rs.getInt("maTheLoai"));
                phim.setThoiLuong(rs.getInt("thoiLuong"));
                phim.setNgayKhoiChieu(rs.getDate("ngayKhoiChieu") != null
                        ? rs.getDate("ngayKhoiChieu").toLocalDate()
                        : null);
                phim.setNuocSanXuat(rs.getString("nuocSanXuat"));
                phim.setKieuPhim(rs.getString("kieuPhim"));
                phim.setMoTa(rs.getString("moTa"));
                phim.setDaoDien(rs.getString("daoDien"));
                phim.setDuongDanPoster(rs.getString("duongDanPoster"));
                phim.setTrangThai(rs.getString("trangThai"));
                return phim;
            }
        }
        return null;
    }

    @Override
    public Phim save(Phim entity) throws SQLException {
        String sql = "INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, kieuPhim, moTa, daoDien, duongDanPoster, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getTenPhim());
            stmt.setInt(2, entity.getMaTheLoai());
            stmt.setInt(3, entity.getThoiLuong());
            stmt.setDate(4, entity.getNgayKhoiChieu() != null ? Date.valueOf(entity.getNgayKhoiChieu()) : null);
            stmt.setString(5, entity.getNuocSanXuat());
            stmt.setString(6, entity.getKieuPhim());
            stmt.setString(7, entity.getMoTa());
            stmt.setString(8, entity.getDaoDien());
            stmt.setString(9, entity.getDuongDanPoster());
            stmt.setString(10, entity.getTrangThai());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                entity.setMaPhim(rs.getInt(1));
            }
        }
        return entity;
    }

    @Override
    public Phim update(Phim entity) throws SQLException {
        String sql = """
            UPDATE Phim 
            SET tenPhim=?, maTheLoai=?, thoiLuong=?, ngayKhoiChieu=?, 
                nuocSanXuat=?, kieuPhim=?, moTa=?, daoDien=?, 
                duongDanPoster=?, trangThai=? 
            WHERE maPhim=?
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entity.getTenPhim());
            stmt.setInt(2, entity.getMaTheLoai());
            stmt.setInt(3, entity.getThoiLuong());
            stmt.setDate(4, entity.getNgayKhoiChieu() != null ? Date.valueOf(entity.getNgayKhoiChieu()) : null);
            stmt.setString(5, entity.getNuocSanXuat());
            stmt.setString(6, entity.getKieuPhim());
            stmt.setString(7, entity.getMoTa());
            stmt.setString(8, entity.getDaoDien());
            stmt.setString(9, entity.getDuongDanPoster());
            stmt.setString(10, entity.getTrangThai());
            stmt.setInt(11, entity.getMaPhim());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không thể cập nhật phim. Không tìm thấy phim với mã: " + entity.getMaPhim());
            }
        }
        return entity;
    }
    
    @Override
    public void delete(int id) throws SQLException {
        String sql = "UPDATE Phim SET trangThai='deleted' WHERE maPhim=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public int getMaTheLoaiByTen(String tenTheLoai) throws SQLException {
        String sql = "SELECT maTheLoai FROM TheLoaiPhim WHERE tenTheLoai = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenTheLoai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maTheLoai");
            }
            throw new SQLException("Không tìm thấy thể loại phim: " + tenTheLoai);
        }
    }

    //Kiểm tra xem tên phim đã tồn tại chưa
    public boolean isMovieTitleExists(String tenPhim, int excludeMaPhim) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Phim WHERE tenPhim = ? AND maPhim != ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhim);
            stmt.setInt(2, excludeMaPhim);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    // Lấy danh sách thể loại duy nhất
    public List<String> getAllTheLoai() throws SQLException {
        List<String> theLoaiList = new ArrayList<>();
        String sql = "SELECT DISTINCT tenTheLoai FROM TheLoaiPhim";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                theLoaiList.add(rs.getString("tenTheLoai"));
            }
        }
        return theLoaiList;
    }

    // Lấy danh sách định dạng duy nhất
    public List<String> getAllDinhDang() throws SQLException {
        List<String> dinhDangList = new ArrayList<>();
        String sql = "SELECT DISTINCT kieuPhim FROM Phim";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String kieuPhim = rs.getString("kieuPhim");
                if (kieuPhim != null && !kieuPhim.isEmpty()) {
                    dinhDangList.add(kieuPhim);
                }
            }
        }
        return dinhDangList;
    }

    // Lấy danh sách phim có thể chiếu ở một phòng (theo tên phòng)
    public List<Phim> getPhimByTenPhong(String tenPhong) throws SQLException {
        List<Phim> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT p.* FROM Phim p
            JOIN SuatChieu sc ON p.maPhim = sc.maPhim
            JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
            WHERE pc.tenPhong = ? AND p.trangThai = 'active'
            ORDER BY p.tenPhim
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Phim phim = new Phim();
                    phim.setMaPhim(rs.getInt("maPhim"));
                    phim.setTenPhim(rs.getString("tenPhim"));
                    phim.setMaTheLoai(rs.getInt("maTheLoai"));
                    phim.setThoiLuong(rs.getInt("thoiLuong"));
                    phim.setNgayKhoiChieu(rs.getDate("ngayKhoiChieu") != null ? rs.getDate("ngayKhoiChieu").toLocalDate() : null);
                    phim.setNuocSanXuat(rs.getString("nuocSanXuat"));
                    phim.setKieuPhim(rs.getString("kieuPhim"));
                    phim.setMoTa(rs.getString("moTa"));
                    phim.setDaoDien(rs.getString("daoDien"));
                    phim.setDuongDanPoster(rs.getString("duongDanPoster"));
                    phim.setTrangThai(rs.getString("trangThai"));
                    list.add(phim);
                }
            }
        }
        return list;
    }
}