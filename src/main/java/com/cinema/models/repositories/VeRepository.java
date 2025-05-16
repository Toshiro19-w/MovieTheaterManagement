package com.cinema.models.repositories;

import java.math.BigDecimal;
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

    public VeRepository(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Ve save(Ve ve) throws SQLException {
        // Kiểm tra các trường bắt buộc
        if (ve.getMaSuatChieu() <= 0) {
            throw new SQLException("Mã suất chiếu không hợp lệ");
        }
        if (ve.getSoGhe() == null || ve.getSoGhe().trim().isEmpty()) {
            throw new SQLException("Số ghế không được để trống");
        }
        if (ve.getTrangThai() == null) {
            throw new SQLException("Trạng thái vé không được để trống");
        }

        // Kiểm tra suất chiếu tồn tại
        if (!isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }

        // Kiểm tra xem suất chiếu đã bắt đầu chưa
        String checkTimeSQL = """
            SELECT sc.ngayGioChieu 
            FROM SuatChieu sc
            JOIN Phim p ON sc.maPhim = p.maPhim
            WHERE sc.maSuatChieu = ? 
            AND sc.ngayGioChieu > NOW()
            AND DATE_ADD(sc.ngayGioChieu, INTERVAL p.thoiLuong MINUTE) > NOW()
        """;
        try (PreparedStatement checkStmt = conn.prepareStatement(checkTimeSQL)) {
            checkStmt.setInt(1, ve.getMaSuatChieu());
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Suất chiếu đã bắt đầu hoặc không tồn tại");
            }
        }

        // Kiểm tra ghế có tồn tại và thuộc phòng chiếu của suất chiếu
        String checkSeatSQL = """
            SELECT g.maGhe, g.loaiGhe 
            FROM Ghe g
            JOIN SuatChieu sc ON g.maPhong = sc.maPhong
            WHERE g.soGhe = ? 
            AND sc.maSuatChieu = ?
        """;
        Integer maGhe = null;
        String loaiGhe = null;
        try (PreparedStatement checkSeatStmt = conn.prepareStatement(checkSeatSQL)) {
            checkSeatStmt.setString(1, ve.getSoGhe());
            checkSeatStmt.setInt(2, ve.getMaSuatChieu());
            ResultSet rs = checkSeatStmt.executeQuery();
            if (rs.next()) {
                maGhe = rs.getInt("maGhe");
                loaiGhe = rs.getString("loaiGhe");
            } else {
                throw new SQLException("Ghế " + ve.getSoGhe() + " không tồn tại hoặc không thuộc phòng chiếu của suất chiếu");
            }
        }

        // Kiểm tra ghế đã được đặt chưa
        if (isSeatTaken(ve.getMaSuatChieu(), ve.getSoGhe())) {
            throw new SQLException("Ghế " + ve.getSoGhe() + " đã được đặt cho suất chiếu " + ve.getMaSuatChieu());
        }

        // Kiểm tra và lấy giá vé
        String checkGiaVeSQL = """
            SELECT maGiaVe, giaVe 
            FROM GiaVe 
            WHERE loaiGhe = ? 
            AND ngayApDung <= NOW() 
            ORDER BY ngayApDung DESC 
            LIMIT 1
        """;
        Integer maGiaVe = null;
        BigDecimal giaVeGoc = null;
        try (PreparedStatement checkGiaVeStmt = conn.prepareStatement(checkGiaVeSQL)) {
            checkGiaVeStmt.setString(1, loaiGhe);
            ResultSet rs = checkGiaVeStmt.executeQuery();
            if (rs.next()) {
                maGiaVe = rs.getInt("maGiaVe");
                giaVeGoc = rs.getBigDecimal("giaVe");
            } else {
                throw new SQLException("Không tìm thấy giá vé phù hợp cho loại ghế " + loaiGhe);
            }
        }

        // Kiểm tra khuyến mãi và tính giá sau giảm (nếu có)
        Integer maKhuyenMai = null;
        String tenKhuyenMai = null;
        BigDecimal tienGiam = BigDecimal.ZERO;
        BigDecimal giaVeSauGiam = giaVeGoc;
        if (ve.getMaKhuyenMai() != 0) {
            String checkKhuyenMaiSQL = """
                SELECT km.maKhuyenMai, km.tenKhuyenMai, km.loaiGiamGia, km.giaTriGiam
                FROM KhuyenMai km
                JOIN DieuKienKhuyenMai dkm ON km.maKhuyenMai = dkm.maKhuyenMai
                JOIN SuatChieu sc ON dkm.maPhim = sc.maPhim
                WHERE km.maKhuyenMai = ?
                AND sc.maSuatChieu = ?
                AND km.trangThai = 'HoatDong'
                AND NOW() BETWEEN km.ngayBatDau AND km.ngayKetThuc
            """;
            try (PreparedStatement checkKhuyenMaiStmt = conn.prepareStatement(checkKhuyenMaiSQL)) {
                checkKhuyenMaiStmt.setInt(1, ve.getMaKhuyenMai());
                checkKhuyenMaiStmt.setInt(2, ve.getMaSuatChieu());
                ResultSet rs = checkKhuyenMaiStmt.executeQuery();
                if (rs.next()) {
                    maKhuyenMai = rs.getInt("maKhuyenMai");
                    tenKhuyenMai = rs.getString("tenKhuyenMai");
                    String loaiGiamGia = rs.getString("loaiGiamGia");
                    BigDecimal giaTriGiam = rs.getBigDecimal("giaTriGiam");

                    if ("PhanTram".equals(loaiGiamGia)) {
                        tienGiam = giaVeGoc.multiply(giaTriGiam).divide(BigDecimal.valueOf(100));
                    } else if ("CoDinh".equals(loaiGiamGia)) {
                        tienGiam = giaTriGiam;
                    }
                    giaVeSauGiam = giaVeGoc.subtract(tienGiam);
                    if (giaVeSauGiam.compareTo(BigDecimal.ZERO) < 0) {
                        giaVeSauGiam = BigDecimal.ZERO;
                    }
                } else {
                    throw new SQLException("Khuyến mãi không hợp lệ hoặc không áp dụng cho suất chiếu này");
                }
            }
        }

        // Thêm vé vào cơ sở dữ liệu
        String sql = """
            INSERT INTO Ve (maSuatChieu, maGhe, maHoaDon, maGiaVe, maKhuyenMai, trangThai, ngayDat) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setInt(2, maGhe);
            
            if (ve.getMaHoaDon() != 0) {
                stmt.setInt(3, ve.getMaHoaDon());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setInt(4, maGiaVe);
            
            if (maKhuyenMai != null) {
                stmt.setInt(5, maKhuyenMai);
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
                    ve.setMaKhuyenMai(maKhuyenMai != null ? maKhuyenMai : 0);
                    ve.setGiaVeGoc(giaVeGoc);
                    ve.setGiaVeSauGiam(giaVeSauGiam);
                    ve.setTienGiam(tienGiam);
                    ve.setTenKhuyenMai(tenKhuyenMai);
                    ve.setLoaiGhe(loaiGhe);
                    return ve;
                } else {
                    throw new SQLException("Tạo vé thất bại, không lấy được mã vé.");
                }
            }
        }
    }

    // Các phương thức khác giữ nguyên
    @Override
    public List<Ve> findAll() throws SQLException {
        String sql = """
                SELECT 
                    v.maVe,
                    v.maSuatChieu,
                    v.maGhe,
                    v.maHoaDon,
                    v.maGiaVe,
                    v.maKhuyenMai,
                    v.trangThai,
                    v.ngayDat,
                    g.soGhe,
                    gv.giaVe,
                    pc.tenPhong,
                    sc.ngayGioChieu,
                    p.tenPhim
                FROM Ve v
                JOIN Ghe g ON v.maGhe = g.maGhe
                JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
                JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
                JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
                JOIN Phim p ON sc.maPhim = p.maPhim
                WHERE v.trangThai != 'DELETED'
                ORDER BY v.ngayDat DESC, v.maVe""";
                
        List<Ve> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ve ve = new Ve();
                ve.setMaVe(rs.getInt("maVe"));
                ve.setMaSuatChieu(rs.getInt("maSuatChieu"));
                ve.setSoGhe(rs.getString("soGhe"));
                ve.setGiaVeGoc(rs.getBigDecimal("giaVe"));
                ve.setTrangThai(TrangThaiVe.valueOf(rs.getString("trangThai").toUpperCase()));
                
                Timestamp ngayDatTs = rs.getTimestamp("ngayDat");
                if (ngayDatTs != null) {
                    ve.setNgayDat(ngayDatTs.toLocalDateTime());
                }
                
                ve.setMaHoaDon(rs.getInt("maHoaDon"));
                ve.setTenPhong(rs.getString("tenPhong"));
                
                Timestamp ngayGioChieuTs = rs.getTimestamp("ngayGioChieu");
                if (ngayGioChieuTs != null) {
                    ve.setNgayGioChieu(ngayGioChieuTs.toLocalDateTime());
                }
                
                ve.setTenPhim(rs.getString("tenPhim"));
                result.add(ve);
            }
        }
        return result;
    }

    @Override
    public List<Ve> findAllDetail() throws SQLException {
        String sql = """
                SELECT
                v.maVe,
                v.maSuatChieu,
                v.maGhe,
                v.trangThai,
                v.ngayDat,
                v.maHoaDon,
                g.soGhe,
                g.loaiGhe,
                gv.giaVe,
                pc.tenPhong,
                pc.loaiPhong,
                sc.ngayGioChieu,
                p.tenPhim,
                p.thoiLuong,
                COALESCE(nd.hoTen, 'Chưa xác định') as tenKhachHang,
                COALESCE(nd.soDienThoai, '') as soDienThoai,
                COALESCE(nd.email, '') as email,
                km.tenKhuyenMai,
                CASE 
                    WHEN v.trangThai = 'CANCELLED' THEN 'Đã hủy'
                    WHEN v.trangThai = 'PAID' THEN 'Đã thanh toán'
                    WHEN v.trangThai = 'BOOKED' THEN 'Đã đặt'
                    WHEN v.trangThai = 'PENDING' THEN 'Đang chờ'
                    ELSE 'Không xác định'
                END as trangThaiHienThi
            FROM Ve v
            JOIN Ghe g ON v.maGhe = g.maGhe
            JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
            JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
            JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
            JOIN Phim p ON sc.maPhim = p.maPhim
            LEFT JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon
            LEFT JOIN NguoiDung nd ON hd.maKhachHang = nd.maNguoiDung
            LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
            WHERE v.trangThai != 'DELETED'
            ORDER BY v.ngayDat DESC, v.maVe""";
                
        List<Ve> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ve ve = new Ve();
                ve.setMaVe(rs.getInt("maVe"));
                ve.setTrangThai(TrangThaiVe.valueOf(rs.getString("trangThai").toUpperCase()));
                ve.setGiaVeGoc(rs.getBigDecimal("giaVe"));
                ve.setSoGhe(rs.getString("soGhe"));
                ve.setLoaiGhe(rs.getString("loaiGhe"));
                
                Timestamp ngayDatTs = rs.getTimestamp("ngayDat");
                if (ngayDatTs != null) {
                    ve.setNgayDat(ngayDatTs.toLocalDateTime());
                }
                
                ve.setTenPhong(rs.getString("tenPhong"));
                
                Timestamp ngayGioChieuTs = rs.getTimestamp("ngayGioChieu");
                if (ngayGioChieuTs != null) {
                    ve.setNgayGioChieu(ngayGioChieuTs.toLocalDateTime());
                }
                
                ve.setTenPhim(rs.getString("tenPhim"));
                ve.setTenKhachHang(rs.getString("tenKhachHang"));
                ve.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                result.add(ve);
            }
        }
        return result;
    }

    @Override
    public List<Ve> findBySoGhe(String soGhe) throws SQLException {
        List<Ve> veList = new ArrayList<>();
        String sql = """
                SELECT
                    v.maVe,
                    v.trangThai,
                    v.ngayDat,
                    g.soGhe,
                    g.loaiGhe,
                    gv.giaVe,
                    pc.tenPhong,
                    sc.ngayGioChieu,
                    p.tenPhim,
                    km.tenKhuyenMai
                FROM Ve v
                JOIN Ghe g ON v.maGhe = g.maGhe
                JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
                JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
                JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
                JOIN Phim p ON sc.maPhim = p.maPhim
                LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
                WHERE g.soGhe = ? AND v.trangThai != 'DELETED'
                ORDER BY v.ngayDat DESC, v.maVe""";
                
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

                Ve ve = new Ve();
                ve.setMaVe(rs.getInt("maVe"));
                ve.setTrangThai(TrangThaiVe.fromString(rs.getString("trangThai")));
                ve.setGiaVeGoc(rs.getBigDecimal("giaVe"));
                ve.setSoGhe(rs.getString("soGhe"));
                ve.setLoaiGhe(rs.getString("loaiGhe"));
                ve.setNgayDat(ngayDat);
                ve.setTenPhong(rs.getString("tenPhong"));
                ve.setNgayGioChieu(ngayGioChieu);
                ve.setTenPhim(rs.getString("tenPhim"));
                ve.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                veList.add(ve);
            }
        }
        return veList;
    }

    @Override
    public Ve findVeByMaVe(int maVe) throws SQLException {
        String sql = """
                SELECT
                    v.maVe,
                    v.trangThai,
                    v.ngayDat,
                    g.soGhe,
                    g.loaiGhe,
                    gv.giaVe,
                    pc.tenPhong,
                    sc.ngayGioChieu,
                    p.tenPhim,
                    km.tenKhuyenMai
                FROM Ve v
                JOIN Ghe g ON v.maGhe = g.maGhe
                JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
                JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
                JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
                JOIN Phim p ON sc.maPhim = p.maPhim
                LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
                WHERE v.maVe = ? AND v.trangThai != 'DELETED'""";
                
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

                Ve ve = new Ve();
                ve.setMaVe(rs.getInt("maVe"));
                ve.setTrangThai(TrangThaiVe.fromString(rs.getString("trangThai")));
                ve.setGiaVeGoc(rs.getBigDecimal("giaVe"));
                ve.setSoGhe(rs.getString("soGhe"));
                ve.setLoaiGhe(rs.getString("loaiGhe"));
                ve.setNgayDat(ngayDat);
                ve.setTenPhong(rs.getString("tenPhong"));
                ve.setNgayGioChieu(ngayGioChieu);
                ve.setTenPhim(rs.getString("tenPhim"));
                ve.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                return ve;
            }
        }
        return null;
    }

    @Override
    public Ve update(Ve ve) throws SQLException {
        String checkSql = """
                SELECT v.maVe 
                FROM Ve v
                JOIN Ghe g ON v.maGhe = g.maGhe
                WHERE v.maSuatChieu = ? 
                AND g.soGhe = ? 
                AND v.trangThai != 'CANCELLED' 
                AND v.maVe != ?""";
                
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, ve.getMaSuatChieu());
            checkStmt.setString(2, ve.getSoGhe());
            checkStmt.setInt(3, ve.getMaVe());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new SQLException("Ghế " + ve.getSoGhe() + " đã được đặt cho suất chiếu " + ve.getMaSuatChieu());
            }
        }

        if (!isSuatChieuExists(ve.getMaSuatChieu())) {
            throw new SQLException("Suất chiếu với mã " + ve.getMaSuatChieu() + " không tồn tại");
        }

        String sql = """
                UPDATE Ve 
                SET maSuatChieu = ?, 
                    maGhe = (SELECT maGhe FROM Ghe WHERE soGhe = ? AND maPhong = (SELECT maPhong FROM SuatChieu WHERE maSuatChieu = ?)),
                    maGiaVe = (SELECT maGiaVe FROM GiaVe WHERE loaiGhe = (SELECT loaiGhe FROM Ghe WHERE soGhe = ? AND maPhong = (SELECT maPhong FROM SuatChieu WHERE maSuatChieu = ?)) AND ngayApDung <= NOW() ORDER BY ngayApDung DESC LIMIT 1),
                    trangThai = ?, 
                    ngayDat = ? 
                WHERE maVe = ?""";
                
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ve.getMaSuatChieu());
            stmt.setString(2, ve.getSoGhe());
            stmt.setInt(3, ve.getMaSuatChieu());
            stmt.setString(4, ve.getSoGhe());
            stmt.setInt(5, ve.getMaSuatChieu());
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

    @Override
    public void delete(int id) throws SQLException {
        String checkSQL = """
                SELECT v.trangThai, sc.ngayGioChieu 
                FROM Ve v
                JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
                WHERE v.maVe = ?""";
                
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                throw new SQLException("Không tìm thấy vé với mã: " + id);
            }
            
            String trangThai = rs.getString("trangThai");
            Timestamp ngayGioChieu = rs.getTimestamp("ngayGioChieu");
            
            if ("PAID".equals(trangThai)) {
                throw new SQLException("Không thể xóa vé đã thanh toán");
            }
            
            if (ngayGioChieu != null && ngayGioChieu.before(new Timestamp(System.currentTimeMillis()))) {
                throw new SQLException("Không thể xóa vé của suất chiếu đã diễn ra");
            }
        }

        String sql = "UPDATE Ve SET trangThai = 'DELETED' WHERE maVe = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Không thể xóa vé với mã: " + id);
            }
        }
    }

    @Override
    public BigDecimal findTicketPriceBySuatChieu(int maSuatChieu) throws SQLException {
        String sql = """
                SELECT gv.giaVe 
                FROM Ve v
                JOIN Ghe g ON v.maGhe = g.maGhe
                JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
                WHERE v.maSuatChieu = ? 
                LIMIT 1""";
                
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

    @Override
    public boolean isSuatChieuExists(int maSuatChieu) throws SQLException {
        String sql = "SELECT 1 FROM SuatChieu WHERE maSuatChieu = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
                AND v.trangThai != 'CANCELLED'""";
                
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            WHERE sc.maPhim = ? AND v.trangThai = 'PAID'
            LIMIT 1
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhim);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}