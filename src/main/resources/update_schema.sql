-- Chuyển dữ liệu từ bảng Phim sang bảng PhimTheLoai
INSERT INTO PhimTheLoai (maPhim, maTheLoai)
SELECT maPhim, maTheLoai FROM Phim;

-- Sửa đổi bảng Phim để loại bỏ cột maTheLoai
ALTER TABLE Phim DROP FOREIGN KEY phim_ibfk_1;
ALTER TABLE Phim DROP COLUMN maTheLoai;

-- Cập nhật các procedure liên quan
DROP PROCEDURE IF EXISTS GetActiveMovies;
DELIMITER //
CREATE PROCEDURE GetActiveMovies()
BEGIN
    SELECT p.*, GROUP_CONCAT(tl.tenTheLoai SEPARATOR ', ') as tenTheLoai
    FROM Phim p
    LEFT JOIN PhimTheLoai pt ON p.maPhim = pt.maPhim
    LEFT JOIN TheLoaiPhim tl ON pt.maTheLoai = tl.maTheLoai
    WHERE p.trangThai IN ('active', 'upcoming')
    GROUP BY p.maPhim
    ORDER BY p.ngayKhoiChieu;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS GetUpcomingMovies;
DELIMITER //
CREATE PROCEDURE GetUpcomingMovies()
BEGIN
    SELECT p.*, GROUP_CONCAT(tl.tenTheLoai SEPARATOR ', ') as tenTheLoai
    FROM Phim p
    LEFT JOIN PhimTheLoai pt ON p.maPhim = pt.maPhim
    LEFT JOIN TheLoaiPhim tl ON pt.maTheLoai = tl.maTheLoai
    WHERE p.trangThai = 'upcoming'
    GROUP BY p.maPhim
    ORDER BY p.ngayKhoiChieu;
END //
DELIMITER ;

-- Cập nhật view LichChieuPhim
DROP VIEW IF EXISTS LichChieuPhim;
CREATE VIEW LichChieuPhim AS
SELECT 
    p.maPhim,
    p.tenPhim,
    sc.maSuatChieu,
    sc.ngayGioChieu,
    pc.tenPhong,
    pc.loaiPhong,
    COUNT(v.maVe) as SoVeDaBan,
    pc.soLuongGhe - COUNT(v.maVe) as SoGheConLai,
    GROUP_CONCAT(tl.tenTheLoai SEPARATOR ', ') as tenTheLoai
FROM Phim p
JOIN SuatChieu sc ON p.maPhim = sc.maPhim
JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
LEFT JOIN Ve v ON sc.maSuatChieu = v.maSuatChieu
LEFT JOIN PhimTheLoai pt ON p.maPhim = pt.maPhim
LEFT JOIN TheLoaiPhim tl ON pt.maTheLoai = tl.maTheLoai
WHERE p.trangThai = 'active'
GROUP BY p.maPhim, p.tenPhim, sc.maSuatChieu, sc.ngayGioChieu, pc.tenPhong;

-- Cập nhật view ThongKeDoanhThuPhim
DROP VIEW IF EXISTS ThongKeDoanhThuPhim;
CREATE VIEW ThongKeDoanhThuPhim AS
SELECT 
    p.maPhim,
    p.tenPhim,
    GROUP_CONCAT(DISTINCT tl.tenTheLoai SEPARATOR ', ') as tenTheLoai,
    COUNT(DISTINCT CASE WHEN v.trangThai = 'paid' THEN v.maVe END) as SoVeDaBan,
    COALESCE(SUM(CASE WHEN v.trangThai = 'paid' THEN gv.giaVe END), 0) as DoanhThuGoc,
    COALESCE(SUM(CASE 
        WHEN v.trangThai = 'paid' AND v.maKhuyenMai IS NOT NULL THEN
            CASE 
                WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (1 - km.giaTriGiam/100)
                WHEN km.loaiGiamGia = 'CoDinh' THEN GREATEST(gv.giaVe - km.giaTriGiam, 0)
                ELSE gv.giaVe
            END
        WHEN v.trangThai = 'paid' THEN gv.giaVe
        ELSE 0
    END), 0) as DoanhThuThucTe,
    COALESCE((
        SELECT AVG(dg.diemDanhGia)
        FROM DanhGia dg
        WHERE dg.maPhim = p.maPhim
    ), 0) as DiemDanhGiaTrungBinh
FROM Phim p
LEFT JOIN PhimTheLoai pt ON p.maPhim = pt.maPhim
LEFT JOIN TheLoaiPhim tl ON pt.maTheLoai = tl.maTheLoai
LEFT JOIN SuatChieu sc ON p.maPhim = sc.maPhim
LEFT JOIN Ve v ON sc.maSuatChieu = v.maSuatChieu
LEFT JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
GROUP BY p.maPhim, p.tenPhim;

-- Xóa chỉ mục cũ
DROP INDEX IF EXISTS idx_phim_maTheLoai ON Phim;

-- Tạo chỉ mục mới
CREATE INDEX idx_phimtheloai_maphim ON PhimTheLoai(maPhim);
CREATE INDEX idx_phimtheloai_matheloai ON PhimTheLoai(maTheLoai);