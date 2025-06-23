package com.cinema.models.repositories;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.Phim;
import com.cinema.models.dto.PaginationResult;
import com.cinema.services.PhimTheLoaiService;
import com.cinema.utils.DatabaseConnection;

public class PhimRepository extends BaseRepository<Phim> {
    private final PhimTheLoaiService phimTheLoaiService;
    
    public PhimRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
        this.phimTheLoaiService = new PhimTheLoaiService(databaseConnection);
    }

    @Override
    public List<Phim> findAll() throws SQLException {
        return findAllPaginated(1, Integer.MAX_VALUE).getData();
    }
    
    public PaginationResult<Phim> findAllPaginated(int page, int pageSize) throws SQLException {
        List<Phim> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        
        // Đếm tổng số phim
        String countSql = "SELECT COUNT(DISTINCT p.maPhim) FROM Phim p WHERE p.trangThai != 'deleted'";
        int totalItems = 0;
        try (Connection conn = getConnection();
            Statement countStmt = conn.createStatement(); 
            ResultSet countRs = countStmt.executeQuery(countSql)) {
            if (countRs.next()) {
                totalItems = countRs.getInt(1);
            }
        }
        
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        // Lấy dữ liệu phim theo trang
        String sql = """
                    SELECT DISTINCT p.maPhim, p.tenPhim, p.thoiLuong, p.ngayKhoiChieu, 
                    p.nuocSanXuat, p.kieuPhim, p.moTa, p.daoDien, p.duongDanPoster, p.trangThai
                    FROM Phim p
                    LEFT JOIN SuatChieu sc ON p.maPhim = sc.maPhim
                    WHERE p.trangThai != 'deleted'
                    GROUP BY p.maPhim, p.tenPhim, p.thoiLuong, p.ngayKhoiChieu,
                    p.nuocSanXuat, p.kieuPhim, p.moTa, p.daoDien, p.duongDanPoster, p.trangThai
                    ORDER BY p.ngayKhoiChieu DESC, p.tenPhim
                    LIMIT ? OFFSET ?""";
                    
        List<Phim> phimList = new ArrayList<>();
        
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Phim phim = new Phim();
                    phim.setMaPhim(rs.getInt("maPhim"));
                    phim.setTenPhim(rs.getString("tenPhim"));
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
                    phimList.add(phim);
                }
            }
        }
        
        // Bây giờ thêm thông tin thể loại cho từng phim
        for (Phim phim : phimList) {
            // Lấy danh sách thể loại cho phim
            String tenTheLoai = phimTheLoaiService.getTheLoaiNamesStringByPhimId(phim.getMaPhim());
            phim.setTenTheLoai(tenTheLoai);
            
            // Lấy danh sách mã thể loại
            List<Integer> maTheLoaiList = phimTheLoaiService.getTheLoaiIdsByPhimId(phim.getMaPhim());
            phim.setMaTheLoaiList(maTheLoaiList);
            
            list.add(phim);
        }
        
        return new PaginationResult<>(list, page, totalPages, pageSize, totalItems);
    }

    
    @Override
    public Phim findById(int id) throws SQLException {
        String sql = "SELECT * FROM Phim WHERE maPhim = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Phim phim = new Phim();
                    phim.setMaPhim(rs.getInt("maPhim"));
                    phim.setTenPhim(rs.getString("tenPhim"));
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
                    
                    // Lấy danh sách thể loại cho phim
                    String tenTheLoai = phimTheLoaiService.getTheLoaiNamesStringByPhimId(phim.getMaPhim());
                    phim.setTenTheLoai(tenTheLoai);
                    
                    // Lấy danh sách mã thể loại
                    List<Integer> maTheLoaiList = phimTheLoaiService.getTheLoaiIdsByPhimId(phim.getMaPhim());
                    phim.setMaTheLoaiList(maTheLoaiList);
                    
                    return phim;
                }
            }
        }
        return null;
    }

    @Override
    public Phim save(Phim entity) throws SQLException {
        String sql = "INSERT INTO Phim (tenPhim, thoiLuong, ngayKhoiChieu, nuocSanXuat, kieuPhim, moTa, daoDien, duongDanPoster, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getTenPhim());
            stmt.setInt(2, entity.getThoiLuong());
            stmt.setDate(3, entity.getNgayKhoiChieu() != null ? Date.valueOf(entity.getNgayKhoiChieu()) : null);
            stmt.setString(4, entity.getNuocSanXuat());
            stmt.setString(5, entity.getKieuPhim());
            stmt.setString(6, entity.getMoTa());
            stmt.setString(7, entity.getDaoDien());
            stmt.setString(8, entity.getDuongDanPoster());
            stmt.setString(9, entity.getTrangThai() != null ? entity.getTrangThai() : "upcoming");
            
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int maPhim = rs.getInt(1);
                    entity.setMaPhim(maPhim);
                    
                    // Thêm thể loại cho phim
                    if (entity.getMaTheLoaiList() != null && !entity.getMaTheLoaiList().isEmpty()) {
                        phimTheLoaiService.addTheLoaisForPhim(maPhim, entity.getMaTheLoaiList());
                    }
                }
            }
        }
        return entity;
    }

    public List<Phim> findPhimDangChieu() throws SQLException {
        List<Phim> list = new ArrayList<>();
        
        String sql = """
                SELECT DISTINCT p.maPhim, p.tenPhim, p.thoiLuong, p.ngayKhoiChieu, 
                p.nuocSanXuat, p.kieuPhim, p.moTa, p.daoDien, p.duongDanPoster, p.trangThai
                FROM Phim p
                WHERE p.trangThai = 'active'
                ORDER BY p.ngayKhoiChieu DESC, p.tenPhim
                """;
                
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Phim phim = new Phim();
                phim.setMaPhim(rs.getInt("maPhim"));
                phim.setTenPhim(rs.getString("tenPhim"));
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
                
                // Lấy danh sách thể loại cho phim
                String tenTheLoai = phimTheLoaiService.getTheLoaiNamesStringByPhimId(phim.getMaPhim());
                phim.setTenTheLoai(tenTheLoai);
                
                // Lấy danh sách mã thể loại
                List<Integer> maTheLoaiList = phimTheLoaiService.getTheLoaiIdsByPhimId(phim.getMaPhim());
                phim.setMaTheLoaiList(maTheLoaiList);
                
                list.add(phim);
            }
        }
        
        return list;
    }

    @Override
    public Phim update(Phim entity) throws SQLException {
        String sql = """
            UPDATE Phim 
            SET tenPhim=?, thoiLuong=?, ngayKhoiChieu=?, 
                nuocSanXuat=?, kieuPhim=?, moTa=?, daoDien=?, 
                duongDanPoster=?, trangThai=? 
            WHERE maPhim=?
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entity.getTenPhim());
            stmt.setInt(2, entity.getThoiLuong());
            stmt.setDate(3, entity.getNgayKhoiChieu() != null ? Date.valueOf(entity.getNgayKhoiChieu()) : null);
            stmt.setString(4, entity.getNuocSanXuat());
            stmt.setString(5, entity.getKieuPhim());
            stmt.setString(6, entity.getMoTa());
            stmt.setString(7, entity.getDaoDien());
            stmt.setString(8, entity.getDuongDanPoster());
            stmt.setString(9, entity.getTrangThai());
            stmt.setInt(10, entity.getMaPhim());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không thể cập nhật phim. Không tìm thấy phim với mã: " + entity.getMaPhim());
            }
            
            // Cập nhật thể loại cho phim
            if (entity.getMaTheLoaiList() != null) {
                phimTheLoaiService.updateTheLoaisForPhim(entity.getMaPhim(), entity.getMaTheLoaiList());
            }
        }
        return entity;
    }
    
    @Override
    public void delete(int id) throws SQLException {
        String sql = "UPDATE Phim SET trangThai='deleted' WHERE maPhim=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public int getMaTheLoaiByTen(String tenTheLoai) throws SQLException {
        String sql = "SELECT maTheLoai FROM TheLoaiPhim WHERE tenTheLoai = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenTheLoai);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maTheLoai");
                }
                throw new SQLException("Không tìm thấy thể loại phim: " + tenTheLoai);
            }
        }
    }

    //Kiểm tra xem tên phim đã tồn tại chưa
    public boolean isMovieTitleExists(String tenPhim, int excludeMaPhim) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Phim WHERE tenPhim = ? AND maPhim != ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhim);
            stmt.setInt(2, excludeMaPhim);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    // Lấy danh sách thể loại duy nhất
    public List<String> getAllTheLoai() throws SQLException {
        List<String> theLoaiList = new ArrayList<>();
        String sql = "SELECT DISTINCT tenTheLoai FROM TheLoaiPhim ORDER BY tenTheLoai";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
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
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
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
        List<Phim> phimList = new ArrayList<>();
        
        String sql = """
            SELECT DISTINCT p.* FROM Phim p
            JOIN SuatChieu sc ON p.maPhim = sc.maPhim
            JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
            WHERE pc.tenPhong = ? AND p.trangThai = 'active'
            ORDER BY p.tenPhim
        """;
        
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Phim phim = new Phim();
                    phim.setMaPhim(rs.getInt("maPhim"));
                    phim.setTenPhim(rs.getString("tenPhim"));
                    phim.setThoiLuong(rs.getInt("thoiLuong"));
                    phim.setNgayKhoiChieu(rs.getDate("ngayKhoiChieu") != null ? rs.getDate("ngayKhoiChieu").toLocalDate() : null);
                    phim.setNuocSanXuat(rs.getString("nuocSanXuat"));
                    phim.setKieuPhim(rs.getString("kieuPhim"));
                    phim.setMoTa(rs.getString("moTa"));
                    phim.setDaoDien(rs.getString("daoDien"));
                    phim.setDuongDanPoster(rs.getString("duongDanPoster"));
                    phim.setTrangThai(rs.getString("trangThai"));
                    phimList.add(phim);
                }
            }
        }
        
        // Thêm thông tin thể loại cho từng phim
        for (Phim phim : phimList) {
            // Lấy danh sách thể loại cho phim
            String tenTheLoai = phimTheLoaiService.getTheLoaiNamesStringByPhimId(phim.getMaPhim());
            phim.setTenTheLoai(tenTheLoai);
            
            // Lấy danh sách mã thể loại
            List<Integer> maTheLoaiList = phimTheLoaiService.getTheLoaiIdsByPhimId(phim.getMaPhim());
            phim.setMaTheLoaiList(maTheLoaiList);
            
            list.add(phim);
        }
        
        return list;
    }
    public void addTheLoai(String tenTheLoai) throws SQLException {
        String sql = "INSERT INTO theloai (ten_the_loai) VALUES (?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTheLoai);
            ps.executeUpdate();
        }
    }

    public void updateTheLoai(int maTheLoai, String tenTheLoai) throws SQLException {
        String sql = "UPDATE theloai SET ten_the_loai = ? WHERE ma_the_loai = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTheLoai);
            ps.setInt(2, maTheLoai);
            ps.executeUpdate();
        }
    }

    public void deleteTheLoai(int maTheLoai) throws SQLException {
        String sql = "DELETE FROM theloai WHERE ma_the_loai = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTheLoai);
            ps.executeUpdate();
        }
    }
}