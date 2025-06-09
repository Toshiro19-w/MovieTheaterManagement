package com.cinema.models.repositories;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cinema.enums.TrangThaiVe;
import com.cinema.models.Ve;
import com.cinema.models.repositories.Interface.IVeRepository;
import com.cinema.utils.DatabaseConnection; 

public class VeRepository extends BaseRepository<Ve> implements IVeRepository {

    public VeRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    @Override
    public List<Ve> findAll() throws SQLException {
        List<Ve> veList = new ArrayList<>();
        String sql = "SELECT * FROM VeView";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Ve ve = new Ve();
                ve.setMaVe(rs.getInt("MaVe"));
                ve.setTrangThai(TrangThaiVe.fromString(rs.getString("TrangThai")));
                ve.setSoGhe(rs.getString("SoGhe"));
                ve.setGiaVeGoc(rs.getBigDecimal("GiaVeGoc"));
                ve.setTienGiam(rs.getBigDecimal("TienGiam"));
                ve.setGiaVeSauGiam(rs.getBigDecimal("GiaVeSauGiam"));
                
                Timestamp ngayDat = rs.getTimestamp("NgayDat");
                if (ngayDat != null) {
                    ve.setNgayDat(ngayDat.toLocalDateTime());
                }
                
                ve.setTenPhong(rs.getString("TenPhong"));
                
                Timestamp ngayGioChieu = rs.getTimestamp("NgayGioChieu");
                if (ngayGioChieu != null) {
                    ve.setNgayGioChieu(ngayGioChieu.toLocalDateTime());
                }
                
                ve.setTenPhim(rs.getString("TenPhim"));
                ve.setTenKhuyenMai(rs.getString("TenKhuyenMai"));
                
                veList.add(ve);
            }
        }
        
        return veList;
    }

    @Override
    public Ve findById(int id) throws SQLException {
        return findVeByMaVe(id);
    }

    @Override
    public List<Ve> findAllDetail() throws SQLException {
        List<Ve> veList = new ArrayList<>();
        String sql = "SELECT * FROM VeView";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Ve ve = new Ve();
                ve.setMaVe(rs.getInt("MaVe"));
                ve.setTrangThai(TrangThaiVe.fromString(rs.getString("TrangThai")));
                ve.setSoGhe(rs.getString("SoGhe"));
                ve.setGiaVeGoc(rs.getBigDecimal("GiaVeGoc"));
                ve.setTienGiam(rs.getBigDecimal("TienGiam"));
                ve.setGiaVeSauGiam(rs.getBigDecimal("GiaVeSauGiam"));
                
                Timestamp ngayDat = rs.getTimestamp("NgayDat");
                if (ngayDat != null) {
                    ve.setNgayDat(ngayDat.toLocalDateTime());
                }
                
                ve.setTenPhong(rs.getString("TenPhong"));
                
                Timestamp ngayGioChieu = rs.getTimestamp("NgayGioChieu");
                if (ngayGioChieu != null) {
                    ve.setNgayGioChieu(ngayGioChieu.toLocalDateTime());
                }
                
                ve.setTenPhim(rs.getString("TenPhim"));
                ve.setTenKhuyenMai(rs.getString("TenKhuyenMai"));
                
                veList.add(ve);
            }
        }
        return veList;
    }

    @Override
    public List<Ve> findBySoGhe(String soGhe) throws SQLException {
        List<Ve> veList = new ArrayList<>();
        String sql = "SELECT * FROM VeView WHERE SoGhe = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, soGhe);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ve ve = new Ve();
                    ve.setMaVe(rs.getInt("MaVe"));
                    ve.setTrangThai(TrangThaiVe.fromString(rs.getString("TrangThai")));
                    ve.setSoGhe(rs.getString("SoGhe"));
                    ve.setGiaVeGoc(rs.getBigDecimal("GiaVeGoc"));
                    ve.setTienGiam(rs.getBigDecimal("TienGiam"));
                    ve.setGiaVeSauGiam(rs.getBigDecimal("GiaVeSauGiam"));
                    
                    Timestamp ngayDat = rs.getTimestamp("NgayDat");
                    if (ngayDat != null) {
                        ve.setNgayDat(ngayDat.toLocalDateTime());
                    }
                    
                    ve.setTenPhong(rs.getString("TenPhong"));
                    
                    Timestamp ngayGioChieu = rs.getTimestamp("NgayGioChieu");
                    if (ngayGioChieu != null) {
                        ve.setNgayGioChieu(ngayGioChieu.toLocalDateTime());
                    }
                    
                    ve.setTenPhim(rs.getString("TenPhim"));
                    ve.setTenKhuyenMai(rs.getString("TenKhuyenMai"));
                    
                    veList.add(ve);
                }
            }
        }
        return veList;
    }

    @Override
    public Ve findVeByMaVe(int maVe) throws SQLException {
        String sql = "SELECT * FROM VeView WHERE maVe = ?";
                
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maVe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Ve ve = new Ve();
                    ve.setMaVe(rs.getInt("maVe"));
                    ve.setTrangThai(TrangThaiVe.fromString(rs.getString("trangThai")));
                    ve.setSoGhe(rs.getString("soGhe"));
                    ve.setNgayDat(rs.getTimestamp("ngayDat") != null ? 
                        rs.getTimestamp("ngayDat").toLocalDateTime() : null);
                    ve.setTenPhong(rs.getString("tenPhong"));
                    ve.setNgayGioChieu(rs.getTimestamp("ngayGioChieu").toLocalDateTime());
                    ve.setTenPhim(rs.getString("tenPhim"));
                    ve.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                    ve.setGiaVeGoc(rs.getBigDecimal("giaVeGoc"));
                    ve.setTienGiam(rs.getBigDecimal("tienGiam"));
                    ve.setGiaVeSauGiam(rs.getBigDecimal("giaVeSauGiam"));
                    return ve;
                }
            }
        }
        return null;
    }

    @Override
    public Ve save(Ve ve) throws SQLException {
        // Kiểm tra xem suất chiếu có tồn tại không
        if (!isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }

        // Kiểm tra xem ghế đã được đặt chưa
        if (isSeatTaken(ve.getMaSuatChieu(), ve.getSoGhe())) {
            throw new SQLException("Ghế " + ve.getSoGhe() + " đã được đặt cho suất chiếu " + ve.getMaSuatChieu());
        }

        // Lấy mã ghế từ số ghế và mã phòng
        int maGhe = getMaGheFromSoGhe(ve.getSoGhe(), ve.getMaSuatChieu());
        if (maGhe <= 0) {
            throw new SQLException("Không tìm thấy ghế " + ve.getSoGhe() + " trong phòng chiếu");
        }
        
        // Lấy mã giá vé từ loại ghế
        int maGiaVe = getMaGiaVeFromMaGhe(maGhe);
        if (maGiaVe <= 0) {
            throw new SQLException("Không tìm thấy giá vé cho ghế " + ve.getSoGhe());
        }

        String sql = "INSERT INTO Ve (maSuatChieu, maGhe, maGiaVe, maKhuyenMai, maHoaDon, trangThai, ngayDat) VALUES (?, ?, ?, ?, ?, ?, ?)";
                
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, maGhe);
            stmt.setInt(3, maGiaVe);
            
            if (ve.getMaKhuyenMai() > 0) {
                stmt.setInt(4, ve.getMaKhuyenMai());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            if (ve.getMaHoaDon() > 0) {
                stmt.setInt(5, ve.getMaHoaDon());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setString(6, ve.getTrangThai().toString());
            
            if (ve.getNgayDat() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(ve.getNgayDat()));
            } else {
                stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            }

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Tạo vé thất bại, không có dòng nào được thêm vào.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ve.setMaVe(generatedKeys.getInt(1));
                    ve.setMaGhe(maGhe);
                    ve.setMaGiaVe(maGiaVe);
                } else {
                    throw new SQLException("Tạo vé thất bại, không lấy được ID.");
                }
            }
        }

        updateTicketPricesFromView(ve);
        
        return ve;
    }
    
    /**
     * Lấy mã ghế từ số ghế và mã suất chiếu
     */
    public int getMaGheFromSoGhe(String soGhe, int maSuatChieu) throws SQLException {
        String sql = """
                SELECT g.maGhe 
                FROM Ghe g
                JOIN SuatChieu sc ON g.maPhong = sc.maPhong
                WHERE g.soGhe = ? AND sc.maSuatChieu = ?""";
                
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, soGhe);
            stmt.setInt(2, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maGhe");
                }
            }
        }
        return 0;
    }
    
    /**
     * Lấy mã giá vé từ mã ghế
     */
    public int getMaGiaVeFromMaGhe(int maGhe) throws SQLException {
        String sql = """
                SELECT gv.maGiaVe 
                FROM GiaVe gv
                JOIN Ghe g ON gv.loaiGhe = g.loaiGhe
                WHERE g.maGhe = ? 
                AND gv.ngayApDung <= NOW() 
                ORDER BY gv.ngayApDung DESC 
                LIMIT 1""";
                
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maGhe);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maGiaVe");
                }
            }
        }
        return 0;
    }

    @Override
    public Ve update(Ve ve) throws SQLException {
        // Kiểm tra xem ghế đã được đặt bởi vé khác chưa
        String checkSql = """
                SELECT v.maVe 
                FROM Ve v
                JOIN Ghe g ON v.maGhe = g.maGhe
                WHERE v.maSuatChieu = ? 
                AND g.soGhe = ? 
                AND v.trangThai NOT IN ('cancelled', 'deleted') 
                AND v.maVe != ?""";
                
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, ve.getMaSuatChieu());
            checkStmt.setString(2, ve.getSoGhe());
            checkStmt.setInt(3, ve.getMaVe());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("Ghế " + ve.getSoGhe() + " đã được đặt cho suất chiếu " + ve.getMaSuatChieu());
                }
            }
        }

        // Kiểm tra xem suất chiếu có tồn tại không
        if (!isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }

        // Lấy mã ghế từ số ghế và mã phòng
        int maGhe = getMaGheFromSoGhe(ve.getSoGhe(), ve.getMaSuatChieu());
        if (maGhe <= 0) {
            throw new SQLException("Không tìm thấy ghế " + ve.getSoGhe() + " trong phòng chiếu");
        }
        
        // Lấy mã giá vé từ loại ghế
        int maGiaVe = getMaGiaVeFromMaGhe(maGhe);
        if (maGiaVe <= 0) {
            throw new SQLException("Không tìm thấy giá vé cho ghế " + ve.getSoGhe());
        }

        String sql = "UPDATE Ve SET maSuatChieu = ?, maGhe = ?, maGiaVe = ?, trangThai = ?, ngayDat = ? WHERE maVe = ?";
                
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, maGhe);
            stmt.setInt(3, maGiaVe);
            stmt.setString(4, ve.getTrangThai().toString());
            
            if (ve.getNgayDat() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(ve.getNgayDat()));
            } else {
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            }
            
            stmt.setInt(6, ve.getMaVe());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ve.setMaGhe(maGhe);
                ve.setMaGiaVe(maGiaVe);
                updateTicketPricesFromView(ve);
                return ve;
            }
            return null;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String checkSQL = """
                SELECT v.trangThai, sc.ngayGioChieu 
                FROM Ve v
                JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
                WHERE v.maVe = ?""";
                
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy vé với mã: " + id);
                }
                
                String trangThai = rs.getString("trangThai");
                Timestamp ngayGioChieu = rs.getTimestamp("ngayGioChieu");
                
                if ("paid".equals(trangThai)) {
                    throw new SQLException("Không thể xóa vé đã thanh toán");
                }
                
                if (ngayGioChieu != null && ngayGioChieu.before(new Timestamp(System.currentTimeMillis()))) {
                    throw new SQLException("Không thể xóa vé của suất chiếu đã diễn ra");
                }
            }
        }

        String sql = "UPDATE Ve SET trangThai = 'deleted' WHERE maVe = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Không thể xóa vé với mã: " + id);
            }
        }
    }
    
    /**
     * Lấy thông tin khuyến mãi từ mã khuyến mãi
     * @param maKhuyenMai Mã khuyến mãi
     * @return Mảng chứa [phần trăm giảm, giá giảm tối đa]
     */
    public BigDecimal[] getKhuyenMaiInfo(int maKhuyenMai) throws SQLException {
        if (maKhuyenMai <= 0) {
            return new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
        }
        
        String sql = "SELECT phanTramGiam, giaGiamToiDa FROM KhuyenMai WHERE maKhuyenMai = ? AND trangThai = 'ACTIVE' AND ngayBatDau <= NOW() AND ngayKetThuc >= NOW()";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maKhuyenMai);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal phanTramGiam = rs.getBigDecimal("phanTramGiam");
                    BigDecimal giaGiamToiDa = rs.getBigDecimal("giaGiamToiDa");
                    if (giaGiamToiDa == null) {
                        giaGiamToiDa = BigDecimal.valueOf(Long.MAX_VALUE);
                    }
                    return new BigDecimal[] { phanTramGiam, giaGiamToiDa };
                }
            }
        }
        return new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
    }
    
    /**
     * Kiểm tra xem khuyến mãi có hợp lệ không
     * @param maKhuyenMai Mã khuyến mãi
     * @return true nếu khuyến mãi hợp lệ, false nếu không
     */
    public boolean isKhuyenMaiValid(int maKhuyenMai) throws SQLException {
        if (maKhuyenMai <= 0) {
            return false;
        }
        
        String sql = "SELECT 1 FROM KhuyenMai WHERE maKhuyenMai = ? AND trangThai = 'HoatDong' AND ngayBatDau <= NOW() AND ngayKetThuc >= NOW()";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maKhuyenMai);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        String sql = """
                SELECT gv.giaVe 
                FROM SuatChieu sc
                JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
                JOIN Ghe g ON g.maPhong = pc.maPhong
                JOIN GiaVe gv ON g.loaiGhe = gv.loaiGhe
                WHERE sc.maSuatChieu = ?
                AND gv.ngayApDung <= NOW()
                ORDER BY gv.ngayApDung DESC
                LIMIT 1""";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("giaVe");
                }
            }
            return null;
        }
    }

    @Override
    public boolean isSuatChieuExists(int maSuatChieu) throws SQLException {
        String sql = "SELECT 1 FROM SuatChieu WHERE maSuatChieu = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maSuatChieu);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean isSeatTaken(int maSuatChieu, String soGhe) throws SQLException {
        String sql = """
                SELECT v.maVe 
                FROM Ve v
                JOIN Ghe g ON v.maGhe = g.maGhe
                WHERE v.maSuatChieu = ? 
                AND g.soGhe = ? 
                AND v.trangThai NOT IN ('cancelled', 'deleted')""";
                
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maSuatChieu);
            stmt.setString(2, soGhe);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasPaidTicketsByPhim(int maPhim) throws SQLException {
        String sql = """
            SELECT 1 FROM Ve v
            JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
            WHERE sc.maPhim = ? AND v.trangThai = 'paid'
            LIMIT 1
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maPhim);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Cập nhật giá vé từ VeView
     */
    public void updateTicketPricesFromView(Ve ve) throws SQLException {
        String sql = """
            SELECT GiaVeGoc, TienGiam, GiaVeSauGiam 
            FROM VeView 
            WHERE MaSuatChieu = ? 
            AND SoGhe = ? 
            AND (MaKhuyenMai = ? OR (? IS NULL AND MaKhuyenMai IS NULL))""";
            
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setString(2, ve.getSoGhe());
            
            if (ve.getMaKhuyenMai() > 0) {
                stmt.setInt(3, ve.getMaKhuyenMai());
                stmt.setInt(4, ve.getMaKhuyenMai());
            } else {
                stmt.setNull(3, Types.INTEGER);
                stmt.setNull(4, Types.INTEGER);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ve.setGiaVeGoc(rs.getBigDecimal("GiaVeGoc"));
                    ve.setTienGiam(rs.getBigDecimal("TienGiam"));
                    ve.setGiaVeSauGiam(rs.getBigDecimal("GiaVeSauGiam"));
                } else {
                    throw new SQLException("Không thể tính giá vé cho mã suất chiếu " + ve.getMaSuatChieu() + " và ghế " + ve.getSoGhe());
                }
            }
        }
    }
}