-- Active: 1746521847255@@127.0.0.1@3306@quanlyrcp
SET FOREIGN_KEY_CHECKS = 0;
SET @tables = NULL;
SELECT GROUP_CONCAT(table_schema, '.', table_name) INTO @tables
	FROM information_schema.tables 
	WHERE table_schema = 'quanlyrcp';
SET @tables = CONCAT('DROP TABLE ', @tables);
PREPARE stmt FROM @tables;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET FOREIGN_KEY_CHECKS = 1;

USE quanlyrcp;

-- Tạo bảng NguoiDung (bảng cha của KhachHang và NhanVien)
CREATE TABLE IF NOT EXISTS NguoiDung (
    maNguoiDung INT AUTO_INCREMENT PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(15) UNIQUE NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    loaiNguoiDung ENUM('KhachHang', 'NhanVien') NOT NULL
);

-- Tạo bảng KhachHang (thừa kế từ NguoiDung)
CREATE TABLE IF NOT EXISTS KhachHang (
    maNguoiDung INT PRIMARY KEY,
    diemTichLuy INT DEFAULT 0 CHECK (diemTichLuy >= 0),
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- trigger tính điểm tích luỹ cho khách hàng
DELIMITER //
CREATE TRIGGER after_hoadon_insert
AFTER INSERT ON HoaDon
FOR EACH ROW
BEGIN
    IF NEW.maKhachHang IS NOT NULL THEN
        UPDATE KhachHang
        SET diemTichLuy = diemTichLuy + FLOOR(NEW.tongTien / 100000)
        WHERE maNguoiDung = NEW.maKhachHang;
    END IF;
END //
DELIMITER ;

-- Tạo bảng NhanVien (thừa kế từ NguoiDung)
CREATE TABLE IF NOT EXISTS NhanVien (
    maNguoiDung INT PRIMARY KEY,
    luong DECIMAL(10,2) CHECK (luong >= 0) NOT NULL,
    vaiTro ENUM('Admin', 'QuanLyPhim', 'ThuNgan', 'BanVe') NOT NULL,
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo bảng TaiKhoan
CREATE TABLE IF NOT EXISTS TaiKhoan (
    tenDangNhap NVARCHAR(50) PRIMARY KEY,
    matKhau NVARCHAR(255) NOT NULL,
    loaiTaiKhoan ENUM('Admin', 'QuanLyPhim', 'ThuNgan', 'BanVe', 'User') NOT NULL,
    maNguoiDung INT UNIQUE,
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo bảng TheLoaiPhim
CREATE TABLE IF NOT EXISTS TheLoaiPhim (
    maTheLoai INT AUTO_INCREMENT PRIMARY KEY,
    tenTheLoai NVARCHAR(50) UNIQUE NOT NULL
);

-- Tạo bảng Phim
CREATE TABLE IF NOT EXISTS Phim (
    maPhim INT AUTO_INCREMENT PRIMARY KEY,
    tenPhim NVARCHAR(100) NOT NULL,
    maTheLoai INT NOT NULL,
    thoiLuong INT CHECK (thoiLuong > 0) NOT NULL,
    ngayKhoiChieu DATE NOT NULL,
    nuocSanXuat NVARCHAR(50) NOT NULL,
    kieuPhim NVARCHAR(20) NOT NULL,
    moTa TEXT,
    daoDien NVARCHAR(100) NOT NULL,
    duongDanPoster TEXT,
    trangThai ENUM('active', 'deleted') DEFAULT 'active',
    FOREIGN KEY (maTheLoai) REFERENCES TheLoaiPhim(maTheLoai) ON DELETE CASCADE
);

-- Procedure xóa mềm phim
DELIMITER //
CREATE PROCEDURE SoftDeletePhim(IN p_maPhim INT)
BEGIN
    -- Cập nhật trạng thái phim thành deleted
    UPDATE Phim 
    SET trangThai = 'deleted'
    WHERE maPhim = p_maPhim;
    
    -- Hủy các vé chưa thanh toán
    UPDATE Ve
    SET trangThai = 'cancelled'
    WHERE maSuatChieu IN (
        SELECT maSuatChieu 
        FROM SuatChieu 
        WHERE maPhim = p_maPhim
    )
    AND trangThai IN ('pending', 'booked');
END //
DELIMITER ;

-- trigger kiểm tra trước khi xoá phim
DELIMITER //
CREATE TRIGGER before_delete_phim
BEFORE DELETE ON Phim
FOR EACH ROW
BEGIN
    -- Kiểm tra xem phim có vé đã bán không
    DECLARE has_paid_tickets BOOLEAN;
    
    SELECT EXISTS(
        SELECT 1 FROM Ve v
        JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
        WHERE sc.maPhim = OLD.maPhim
        AND v.trangThai = 'paid'
    ) INTO has_paid_tickets;
    
    IF has_paid_tickets THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa phim đã có vé bán. Hãy sử dụng soft delete.';
    END IF;
END //
DELIMITER ;

-- Procedure lấy danh sách phim đang chiếu
DELIMITER //
CREATE PROCEDURE GetActiveMovies()
BEGIN
    SELECT p.*, tl.tenTheLoai 
    FROM Phim p
    JOIN TheLoaiPhim tl ON p.maTheLoai = tl.maTheLoai
    WHERE p.trangThai = 'active'
    AND p.ngayKhoiChieu <= CURDATE();
END //
DELIMITER;

-- Function tính tổng doanh thu theo phim
DELIMITER //
CREATE FUNCTION GetMovieRevenue(p_maPhim INT) 
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE revenue DECIMAL(10,2);
    
    SELECT COALESCE(SUM(gv.giaVe), 0)
    INTO revenue
    FROM Phim p
    LEFT JOIN SuatChieu sc ON p.maPhim = sc.maPhim
    LEFT JOIN Ve v ON sc.maSuatChieu = v.maSuatChieu
    LEFT JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
    WHERE p.maPhim = p_maPhim
    AND v.trangThai = 'paid';
    
    RETURN revenue;
END //
DELIMITER ;

DROP FUNCTION GetMovieRevenue;

-- Xem doanh thu của một phim cụ thể
SELECT GetMovieRevenue(1) as DoanhThu;

-- Xem doanh thu của tất cả các phim
SELECT p.maPhim, p.tenPhim, GetMovieRevenue(p.maPhim) as DoanhThu
FROM Phim p;

-- View thống kê doanh thu theo phim
CREATE OR REPLACE VIEW ThongKeDoanhThuPhim AS
SELECT 
    p.maPhim,
    p.tenPhim,
    COUNT(DISTINCT CASE WHEN v.trangThai = 'paid' THEN v.maVe END) as SoVeDaBan,
    COALESCE(SUM(CASE WHEN v.trangThai = 'paid' THEN gv.giaVe END), 0) as DoanhThu,
    COALESCE((
        SELECT AVG(dg.diemDanhGia)
        FROM DanhGia dg
        WHERE dg.maPhim = p.maPhim
    ), 0) as DiemDanhGiaTrungBinh
FROM Phim p
LEFT JOIN SuatChieu sc ON p.maPhim = sc.maPhim
LEFT JOIN Ve v ON sc.maSuatChieu = v.maSuatChieu
LEFT JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
GROUP BY p.maPhim, p.tenPhim;

SELECT * FROM thongkedoanhthuphim;

-- View lịch chiếu phim
CREATE VIEW LichChieuPhim AS
SELECT 
    p.maPhim,
    p.tenPhim,
    sc.maSuatChieu,
    sc.ngayGioChieu,
    pc.tenPhong,
    pc.loaiPhong,
    COUNT(v.maVe) as SoVeDaBan,
    pc.soLuongGhe - COUNT(v.maVe) as SoGheConLai
FROM Phim p
JOIN SuatChieu sc ON p.maPhim = sc.maPhim
JOIN PhongChieu pc ON sc.maPhong = pc.maPhong
LEFT JOIN Ve v ON sc.maSuatChieu = v.maSuatChieu
WHERE p.trangThai = 'active'
GROUP BY p.maPhim, p.tenPhim, sc.maSuatChieu, sc.ngayGioChieu, pc.tenPhong;

SELECT * FROM lichchieuphim;

-- Tạo bảng PhongChieu
CREATE TABLE IF NOT EXISTS PhongChieu (
    maPhong INT AUTO_INCREMENT PRIMARY KEY,
    tenPhong NVARCHAR(255) UNIQUE NOT NULL,
    soLuongGhe INT CHECK (soLuongGhe > 0) NOT NULL,
    loaiPhong NVARCHAR(50) NOT NULL
);

-- Tạo bảng Ghe (bảng mới để quản lý ghế trong phòng chiếu)
CREATE TABLE IF NOT EXISTS Ghe (
    maPhong INT NOT NULL,
    soGhe NVARCHAR(5) NOT NULL,
    loaiGhe ENUM('Thuong', 'VIP') DEFAULT 'Thuong',
    PRIMARY KEY (maPhong, soGhe),
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Procedure kiểm tra ghế trống
DELIMITER //
CREATE PROCEDURE CheckAvailableSeats(IN p_maSuatChieu INT)
BEGIN
    SELECT g.maPhong, g.soGhe, g.loaiGhe
    FROM Ghe g
    LEFT JOIN Ve v ON g.maPhong = v.maPhong 
        AND g.soGhe = v.soGhe 
        AND v.maSuatChieu = p_maSuatChieu
    WHERE v.maVe IS NULL;
END //
DELIMITER ;

-- Tạo bảng SuatChieu (thêm ràng buộc thời gian)
CREATE TABLE IF NOT EXISTS SuatChieu (
    maSuatChieu INT AUTO_INCREMENT PRIMARY KEY,
    maPhim INT NOT NULL,
    maPhong INT NOT NULL,
    ngayGioChieu DATETIME NOT NULL,
    soSuatChieu INT DEFAULT 50,
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim) ON DELETE CASCADE,
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Tránh xung đột thời gian giữa các suất chiếu
DELIMITER //
CREATE TRIGGER before_insert_suatchieu
BEFORE INSERT ON SuatChieu
FOR EACH ROW
BEGIN
    DECLARE phim_end_time DATETIME;
    SELECT DATE_ADD(ngayGioChieu, INTERVAL p.thoiLuong MINUTE)
    INTO phim_end_time
    FROM Phim p
    WHERE p.maPhim = NEW.maPhim;
    
    IF EXISTS (
        SELECT 1
        FROM SuatChieu sc
        JOIN Phim p ON sc.maPhim = p.maPhim
        WHERE sc.maPhong = NEW.maPhong
        AND (
            (NEW.ngayGioChieu BETWEEN sc.ngayGioChieu AND DATE_ADD(sc.ngayGioChieu, INTERVAL p.thoiLuong MINUTE))
            OR (phim_end_time BETWEEN sc.ngayGioChieu AND DATE_ADD(sc.ngayGioChieu, INTERVAL p.thoiLuong MINUTE))
            OR (NEW.ngayGioChieu < sc.ngayGioChieu AND phim_end_time > DATE_ADD(sc.ngayGioChieu, INTERVAL p.thoiLuong MINUTE))
        )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Phòng chiếu đã được sử dụng trong khoảng thời gian này.';
    END IF;
END //
DELIMITER ;

-- Tạo bảng HoaDon
CREATE TABLE IF NOT EXISTS HoaDon (
    maHoaDon INT AUTO_INCREMENT PRIMARY KEY,
    maNhanVien INT,
    maKhachHang INT,
    ngayLap DATETIME DEFAULT NOW(),
    tongTien DECIMAL(10,2) CHECK (tongTien >= 0) NOT NULL,
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNguoiDung) ON DELETE SET NULL,
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maNguoiDung) ON DELETE SET NULL
);

-- Tạo trigger để tự động cập nhật tongTien trong HoaDon
DELIMITER //
CREATE TRIGGER after_ve_insert
AFTER INSERT ON ChiTietHoaDon
FOR EACH ROW
BEGIN
    UPDATE HoaDon h
    SET tongTien = (
        SELECT COALESCE(SUM(gv.giaVe), 0)
        FROM ChiTietHoaDon cthd
        JOIN Ve v ON cthd.maVe = v.maVe
        JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
        WHERE cthd.maHoaDon = NEW.maHoaDon
        AND v.trangThai = 'paid'
    )
    WHERE h.maHoaDon = NEW.maHoaDon;
END //

CREATE TRIGGER after_ve_delete
AFTER DELETE ON ChiTietHoaDon
FOR EACH ROW
BEGIN
    UPDATE HoaDon h
    SET tongTien = (
        SELECT COALESCE(SUM(gv.giaVe), 0)
        FROM ChiTietHoaDon cthd
        JOIN Ve v ON cthd.maVe = v.maVe
        JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
        WHERE cthd.maHoaDon = OLD.maHoaDon
    )
    WHERE h.maHoaDon = OLD.maHoaDon;
END//
DELIMITER ;

-- Tạo bảng Ve
CREATE TABLE IF NOT EXISTS Ve (
    maVe INT AUTO_INCREMENT PRIMARY KEY,
    maSuatChieu INT NOT NULL,
    maPhong INT NOT NULL,
    soGhe NVARCHAR(5) NOT NULL,
    maHoaDon INT NULL,
    maGiaVe INT NOT NULL,
    trangThai ENUM('booked', 'paid', 'cancelled', 'pending') NOT NULL,
    ngayDat DATETIME NULL,
    FOREIGN KEY (maSuatChieu) REFERENCES SuatChieu(maSuatChieu) ON DELETE CASCADE,
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE SET NULL,
    FOREIGN KEY (maPhong, soGhe) REFERENCES Ghe(maPhong, soGhe) ON DELETE NO ACTION,
    FOREIGN KEY (maGiaVe) REFERENCES GiaVe(maGiaVe),
    CONSTRAINT UQ_SuatChieu_SoGhe UNIQUE (maSuatChieu, soGhe)
);

-- Tạo bảng ChiTietHoaDon
CREATE TABLE IF NOT EXISTS ChiTietHoaDon (
    maHoaDon INT NOT NULL,
    maVe INT NOT NULL,
    PRIMARY KEY (maHoaDon, maVe),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    FOREIGN KEY (maVe) REFERENCES Ve(maVe) ON DELETE CASCADE
);

-- Tạo bảng GiaVe
CREATE TABLE IF NOT EXISTS GiaVe (
    maGiaVe INT AUTO_INCREMENT PRIMARY KEY,
    loaiGhe ENUM('Thuong', 'VIP') NOT NULL,
    ngayApDung DATE NOT NULL,
    giaVe DECIMAL(10,2) NOT NULL,
    ghiChu TEXT
);

-- Thêm bảng KhuyenMai
CREATE TABLE IF NOT EXISTS KhuyenMai (
    maKhuyenMai INT AUTO_INCREMENT PRIMARY KEY,
    tenKhuyenMai NVARCHAR(100) NOT NULL,
    moTa TEXT,
    phanTramGiam INT CHECK (phanTramGiam BETWEEN 0 AND 100),
    ngayBatDau DATE NOT NULL,
    ngayKetThuc DATE NOT NULL,
    CHECK (ngayKetThuc >= ngayBatDau);
    dieuKienApDung TEXT
);

-- Thêm bảng DanhGia để lưu feedback của khách hàng
CREATE TABLE IF NOT EXISTS DanhGia (
    maDanhGia INT AUTO_INCREMENT PRIMARY KEY,
    maPhim INT NOT NULL,
    maNguoiDung INT NOT NULL,
    diemDanhGia INT CHECK (diemDanhGia BETWEEN 1 AND 5),
    nhanXet TEXT,
    ngayDanhGia DATETIME DEFAULT NOW(),
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim),
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung)
);

INSERT INTO NguoiDung (hoTen, soDienThoai, email, loaiNguoiDung) VALUES
('Lê Trần Minh Khôi', '0565321247', 'letranminhkhoi2506@gmail.com', 'KhachHang'),
('Nguyễn Văn A', '0901234567', 'nguyenvana@gmail.com', 'KhachHang'),
('Trần Thị B', '0912345678', 'tranthib@gmail.com', 'KhachHang'),
('Lê Văn C', '0923456789', 'levanc@gmail.com', 'NhanVien'),
('Phạm Thị D', '0934567890', 'phamthid@gmail.com', 'NhanVien'),
('Hoàng Văn E', '0945678901', 'hoangvane@gmail.com', 'KhachHang'),
('Đỗ Thị F', '0956789012', 'dothif@gmail.com', 'NhanVien'),
('Bùi Văn G', '0967890123', 'buivang@gmail.com', 'KhachHang'),
('Vũ Thị H', '0978901234', 'vuthih@gmail.com', 'NhanVien'),
('Ngô Văn I', '0989012345', 'ngovani@gmail.com', 'KhachHang'),
('Mai Thị K', '0990123456', 'maithik@gmail.com', 'NhanVien');

-- Dữ liệu cho bảng KhachHang
INSERT INTO KhachHang (maNguoiDung, diemTichLuy) VALUES
(1, 50),
(2, 20),
(5, 100),
(7, 0),
(9, 30);

-- Dữ liệu cho bảng NhanVien
INSERT INTO NhanVien (maNguoiDung, luong, vaiTro) VALUES
(3, 15000000.00, 'QuanLyPhim'),
(4, 8000000.00, 'ThuNgan'),
(6, 7000000.00, 'BanVe'),
(8, 20000000.00, 'Admin'),
(10, 7000000.00, 'BanVe');

-- Dữ liệu cho bảng TaiKhoan
INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES
('nguyenvana', 'pass123', 'User', 1),
('tranthib', 'pass456', 'User', 2),
('levanc', 'pass789', 'QuanLyPhim', 3),
('phamthid', 'pass101', 'ThuNgan', 4),
('hoangvane', 'pass112', 'User', 5),
('dothif', 'pass131', 'BanVe', 6),
('buivang', 'pass415', 'User', 7),
('vuthih', 'pass161', 'Admin', 8),
('ngovani', 'pass718', 'user', 9),
('maithik', 'pass192', 'BanVe', 10);

-- Dữ liệu cho bảng TheLoaiPhim
INSERT INTO TheLoaiPhim (tenTheLoai) VALUES
('Hành động'),
('Tình cảm'),
('Kinh dị'),
('Hài hước'),
('Khoa học viễn tưởng'),
('Hoạt hình'),
('Tâm lý'),
('Phiêu lưu'),
('Tài liệu'),
('Cổ trang');

-- Dữ liệu cho bảng Phim
INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, kieuPhim, moTa, daoDien, duongDanPoster) VALUES
('Avatar 3', 8, 180, '2025-01-20', 'Mỹ', '3D IMAX', 'Phần tiếp theo của Avatar', 'James Cameron', 'Avatar3.jpg'),
('Mission: Impossible 8', 1, 150, '2025-05-23', 'Mỹ', 'IMAX', 'Nhiệm vụ bất khả thi mới', 'Christopher McQuarrie', 'MI8.jpg'),
('The Batman 2', 1, 165, '2025-10-03', 'Mỹ', 'IMAX', 'Phần tiếp theo của Batman', 'Matt Reeves', 'Batman2.jpg'),
('Fantastic Beasts 4', 8, 140, '2025-07-15', 'Mỹ', '3D', 'Phần mới của Sinh vật huyền bí', 'David Yates', 'FB4.jpg'),
('Captain America 4', 1, 155, '2025-03-14', 'Mỹ', 'IMAX', 'Cuộc phiêu lưu mới của Captain America', 'Julius Onah', 'Cap4.jpg'),
('Deadpool 3', 1, 130, '2025-07-26', 'Mỹ', 'IMAX', 'Deadpool trở lại với Wolverine', 'Shawn Levy', 'Deadpool3.jpg'),
('Joker 2', 7, 138, '2025-10-04', 'Mỹ', 'IMAX', 'Phần tiếp theo của Joker', 'Todd Phillips', 'Joker2.jpg'),
('Spider-Man 4', 1, 145, '2025-06-27', 'Mỹ', 'IMAX', 'Cuộc phiêu lưu mới của Spider-Man', 'Jon Watts', 'SpiderMan4.jpg'),
('The Matrix 5', 5, 160, '2025-08-22', 'Mỹ', 'IMAX', 'Phần tiếp theo của The Matrix', 'Lana Wachowski', 'Matrix5.jpg'),
('Black Panther 3', 1, 150, '2025-11-07', 'Mỹ', 'IMAX', 'Phần tiếp theo của Black Panther', 'Ryan Coogler', 'BP3.jpg');

-- Dữ liệu cho bảng PhongChieu
INSERT INTO PhongChieu (tenPhong, soLuongGhe, loaiPhong) VALUES
('Phòng 1', 100, 'Thường'),
('Phòng 2', 80, 'VIP'),
('Phòng 3', 120, 'Thường'),
('Phòng 4', 60, 'VIP'),
('Phòng 5', 150, 'Thường');

-- Dữ liệu cho bảng Ghe
INSERT INTO Ghe (maPhong, soGhe) VALUES
-- Phòng 1 (100 ghế, thêm 20 ghế mẫu)
(1, 'A1'), (1, 'A2'), (1, 'A3'), (1, 'A4'), (1, 'A5'),
(1, 'B1'), (1, 'B2'), (1, 'B3'), (1, 'B4'), (1, 'B5'),
(1, 'C1'), (1, 'C2'), (1, 'C3'), (1, 'C4'), (1, 'C5'),
(1, 'D1'), (1, 'D2'), (1, 'D3'), (1, 'D4'), (1, 'D5'),

-- Phòng 2 (80 ghế, thêm 15 ghế mẫu)
(2, 'A1'), (2, 'A2'), (2, 'A3'), (2, 'A4'), (2, 'A5'),
(2, 'B1'), (2, 'B2'), (2, 'B3'), (2, 'B4'), (2, 'B5'),
(2, 'C1'), (2, 'C2'), (2, 'C3'), (2, 'C4'), (2, 'C5'),

-- Phòng 3 (120 ghế, thêm 15 ghế mẫu)
(3, 'A1'), (3, 'A2'), (3, 'A3'), (3, 'A4'), (3, 'A5'),
(3, 'B1'), (3, 'B2'), (3, 'B3'), (3, 'B4'), (3, 'B5'),
(3, 'C1'), (3, 'C2'), (3, 'C3'), (3, 'C4'), (3, 'C5'),

-- Phòng 4 (60 ghế, thêm 10 ghế mẫu)
(4, 'A1'), (4, 'A2'), (4, 'A3'), (4, 'A4'), (4, 'A5'),
(4, 'B1'), (4, 'B2'), (4, 'B3'), (4, 'B4'), (4, 'B5'),

-- Phòng 5 (150 ghế, thêm 15 ghế mẫu)
(5, 'A1'), (5, 'A2'), (5, 'A3'), (5, 'A4'), (5, 'A5'),
(5, 'B1'), (5, 'B2'), (5, 'B3'), (5, 'B4'), (5, 'B5'),
(5, 'C1'), (5, 'C2'), (5, 'C3'), (5, 'C4'), (5, 'C5');

-- Dữ liệu cho bảng SuatChieu
INSERT INTO SuatChieu (maPhim, maPhong, ngayGioChieu) VALUES
(1, 1, '2025-01-20 10:00:00'), -- Avatar 3
(1, 2, '2025-01-20 14:00:00'), -- Avatar 3
(2, 3, '2025-05-23 10:00:00'), -- Mission: Impossible 8
(2, 4, '2025-05-23 14:00:00'), -- Mission: Impossible 8
(3, 5, '2025-10-03 10:00:00'), -- The Batman 2
(3, 1, '2025-10-03 14:00:00'), -- The Batman 2
(4, 2, '2025-07-15 10:00:00'), -- Fantastic Beasts 4
(4, 3, '2025-07-15 14:00:00'), -- Fantastic Beasts 4
(5, 4, '2025-03-14 10:00:00'), -- Captain America 4
(5, 5, '2025-03-14 14:00:00'), -- Captain America 4
(6, 1, '2025-07-26 10:00:00'), -- Deadpool 3
(6, 2, '2025-07-26 14:00:00'), -- Deadpool 3
(7, 3, '2025-10-04 10:00:00'), -- Joker 2
(7, 4, '2025-10-04 14:00:00'), -- Joker 2
(8, 5, '2025-06-27 10:00:00'), -- Spider-Man 4
(8, 1, '2025-06-27 14:00:00'), -- Spider-Man 4
(9, 2, '2025-08-22 10:00:00'), -- The Matrix 5
(9, 3, '2025-08-22 14:00:00'), -- The Matrix 5
(10, 4, '2025-11-07 10:00:00'), -- Black Panther 3
(10, 5, '2025-11-07 14:00:00'); -- Black Panther 3

-- Dữ liệu cho bảng HoaDon
INSERT INTO HoaDon (maNhanVien, maKhachHang, ngayLap, tongTien) VALUES
(3, 1, '2025-01-15 09:00:00', 330000.00),  -- 3 vé thường
(4, 2, '2025-01-15 10:00:00', 440000.00),  -- 2 vé VIP
(6, 3, '2025-01-15 13:00:00', 440000.00),  -- 2 vé VIP
(8, 1, '2025-01-15 14:00:00', 220000.00),  -- 1 vé VIP
(10, 2, '2025-01-15 16:00:00', 220000.00); -- 2 vé thường

-- Dữ liệu cho bảng Vechitiethoadon
INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, maGiaVe, trangThai, ngayDat) VALUES
-- Suất chiếu Avatar 3
(1, 1, 'A1', 1, 1, 'paid', '2025-01-15 09:00:00'),
(1, 1, 'A2', 1, 1, 'paid', '2025-01-15 09:00:00'),
(1, 1, 'A3', 1, 1, 'paid', '2025-01-15 09:00:00'),
(1, 1, 'B1', 2, 2, 'paid', '2025-01-15 10:00:00'),
(1, 1, 'B2', 2, 2, 'paid', '2025-01-15 10:00:00'),
(1, 1, 'C1', NULL, 1, 'booked', '2025-01-15 11:00:00'),
(1, 1, 'C2', NULL, 1, 'pending', '2025-01-15 11:00:00'),

-- Suất chiếu Mission: Impossible 8
(2, 3, 'A1', 3, 2, 'paid', '2025-01-15 13:00:00'),
(2, 3, 'A2', 3, 2, 'paid', '2025-01-15 13:00:00'),
(2, 3, 'B1', 4, 2, 'paid', '2025-01-15 14:00:00'),
(2, 3, 'B2', NULL, 2, 'booked', '2025-01-15 15:00:00'),
(2, 3, 'C1', NULL, 2, 'pending', '2025-01-15 15:00:00'),

-- Suất chiếu The Batman 2
(3, 5, 'A1', 5, 1, 'paid', '2025-01-15 16:00:00'),
(3, 5, 'A2', 5, 1, 'paid', '2025-01-15 16:00:00'),
(3, 5, 'B1', NULL, 2, 'booked', '2025-01-15 17:00:00'),
(3, 5, 'B2', NULL, 2, 'cancelled', '2025-01-15 17:00:00'),
(3, 5, 'C1', NULL, 1, 'pending', '2025-01-15 18:00:00');

-- Dữ liệu cho bảng ChiTietHoaDon
INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES
(1, 1), (1, 2), (1, 3),           -- Hóa đơn 1: 3 vé
(2, 4), (2, 5),                   -- Hóa đơn 2: 2 vé
(3, 8), (3, 9),                   -- Hóa đơn 3: 2 vé
(4, 10),                          -- Hóa đơn 4: 1 vé
(5, 12), (5, 13);                 -- Hóa đơn 5: 2 vé

-- Dữ liệu cho bảng GiaVe
INSERT INTO GiaVe (loaiGhe, ngayApDung, giaVe, ghiChu) VALUES
('Thuong', '2025-01-01', 110000.00, 'Giá vé thường 2025'),
('VIP', '2025-01-01', 220000.00, 'Giá vé VIP 2025'),
('Thuong', '2025-02-01', 120000.00, 'Tết 2025'),
('VIP', '2025-02-01', 240000.00, 'Tết 2025');

-- Dữ liệu cho bảng KhuyenMai
INSERT INTO KhuyenMai (tenKhuyenMai, moTa, phanTramGiam, ngayBatDau, ngayKetThuc, dieuKienApDung) VALUES
('Tết Nguyên Đán 2025', 'Ưu đãi đặc biệt dịp Tết', 25, '2025-02-01', '2025-02-07', 'Áp dụng cho tất cả các suất chiếu'),
('Mùa hè 2025', 'Khuyến mãi mùa hè', 20, '2025-06-01', '2025-08-31', 'Áp dụng cho học sinh, sinh viên'),
('Sinh nhật rạp 2025', 'Kỷ niệm thành lập', 30, '2025-07-01', '2025-07-07', 'Áp dụng cho thành viên');

-- Dữ liệu cho bảng DanhGia
INSERT INTO DanhGia (maPhim, maNguoiDung, diemDanhGia, nhanXet, ngayDanhGia) VALUES
(1, 1, 5, 'Phim hay, hiệu ứng đẹp', '2025-01-15 20:30:00'),
(1, 2, 4, 'Phim hấp dẫn, diễn viên đóng tốt', '2025-01-16 21:00:00'),
(2, 1, 5, 'Phim hành động xuất sắc', '2025-01-17 19:45:00'),
(2, 3, 3, 'Phim dài hơi', '2025-01-18 22:15:00'),
(3, 2, 4, 'Phim siêu anh hùng đúng chất', '2025-01-19 20:00:00'),
(3, 3, 5, 'Hiệu ứng đặc biệt tuyệt vời', '2025-01-20 21:30:00'),
(4, 1, 4, 'Phim phiêu lưu hấp dẫn', '2025-01-21 19:00:00'),
(4, 2, 5, 'Phim phù hợp với mọi lứa tuổi', '2025-01-22 20:45:00'),
(5, 3, 5, 'Phim siêu anh hùng xuất sắc', '2025-01-23 21:15:00'),
(5, 1, 4, 'Hiệu ứng đặc biệt ấn tượng', '2025-01-24 22:00:00'),
(6, 2, 5, 'Hài hước và hành động xuất sắc', '2025-01-25 20:30:00'),
(6, 3, 4, 'Deadpool trở lại ấn tượng', '2025-01-26 21:00:00'),
(7, 1, 5, 'Diễn xuất xuất sắc của Joaquin Phoenix', '2025-01-27 19:45:00'),
(7, 2, 4, 'Phim tâm lý đỉnh cao', '2025-01-28 22:15:00'),
(8, 3, 5, 'Spider-Man mới thú vị', '2025-01-29 20:00:00'),
(8, 1, 4, 'Hiệu ứng đặc biệt ấn tượng', '2025-01-30 21:30:00'),
(9, 2, 5, 'The Matrix trở lại hoành tráng', '2025-01-31 19:00:00'),
(9, 3, 4, 'Phim khoa học viễn tưởng xuất sắc', '2025-02-01 20:45:00'),
(10, 1, 5, 'Black Panther mới ấn tượng', '2025-02-02 21:15:00'),
(10, 2, 4, 'Phim siêu anh hùng đỉnh cao', '2025-02-03 22:00:00');

SELECT * FROM TaiKhoan;