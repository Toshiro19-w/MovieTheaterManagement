﻿  SET FOREIGN_KEY_CHECKS = 0;
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

CREATE TABLE IF NOT EXISTS ResetToken (
    token VARCHAR(255) PRIMARY KEY,
    email NVARCHAR(100) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    FOREIGN KEY (email) REFERENCES NguoiDung(email) ON DELETE CASCADE
);

-- Tạo bảng NhanVien (thừa kế từ NguoiDung)
CREATE TABLE IF NOT EXISTS NhanVien (
    maNguoiDung INT PRIMARY KEY,
    chucVu NVARCHAR(50) NOT NULL,
    luong DECIMAL(10,2) CHECK (luong >= 0) NOT NULL,
    vaiTro ENUM('Admin', 'QuanLy', 'ThuNgan', 'BanVe') NOT NULL,
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo bảng TaiKhoan
CREATE TABLE IF NOT EXISTS TaiKhoan (
    tenDangNhap NVARCHAR(50) PRIMARY KEY,
    matKhau NVARCHAR(255) NOT NULL,
    loaiTaiKhoan ENUM('admin', 'user') NOT NULL,
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
    dinhDang NVARCHAR(20) NOT NULL,
    moTa TEXT,
    daoDien NVARCHAR(100) NOT NULL,
    FOREIGN KEY (maTheLoai) REFERENCES TheLoaiPhim(maTheLoai) ON DELETE CASCADE
);

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
    PRIMARY KEY (maPhong, soGhe),
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Tạo bảng SuatChieu (thêm ràng buộc thời gian)
CREATE TABLE IF NOT EXISTS SuatChieu (
    maSuatChieu INT AUTO_INCREMENT PRIMARY KEY,
    maPhim INT NOT NULL,
    maPhong INT NOT NULL,
    ngayGioChieu DATETIME NOT NULL,
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim) ON DELETE CASCADE,
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

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

-- Tạo bảng Ve
CREATE TABLE IF NOT EXISTS Ve (
    maVe INT AUTO_INCREMENT PRIMARY KEY,
    maSuatChieu INT NOT NULL,
    maPhong INT NOT NULL,
    soGhe NVARCHAR(5) NOT NULL,
    maHoaDon INT NULL,
    giaVe DECIMAL(10,2) CHECK (giaVe >= 0) NOT NULL,
    trangThai ENUM('available', 'booked', 'paid', 'cancelled', 'pending') DEFAULT 'available' NOT NULL,
    ngayDat DATETIME NULL,
    FOREIGN KEY (maSuatChieu) REFERENCES SuatChieu(maSuatChieu) ON DELETE CASCADE,
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE SET NULL,
    FOREIGN KEY (maPhong, soGhe) REFERENCES Ghe(maPhong, soGhe) ON DELETE NO ACTION,
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
INSERT INTO NhanVien (maNguoiDung, chucVu, luong, vaiTro) VALUES
(3, 'Quản lý', 15000000.00, 'QuanLy'),
(4, 'Thu ngân', 8000000.00, 'ThuNgan'),
(6, 'Bán vé', 7000000.00, 'BanVe'),
(8, 'Admin', 20000000.00, 'Admin'),
(10, 'Bán vé', 7000000.00, 'BanVe');

-- Dữ liệu cho bảng TaiKhoan
INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES
('nguyenvana', 'pass123', 'user', 1),
('tranthib', 'pass456', 'user', 2),
('levanc', 'pass789', 'admin', 3),
('phamthid', 'pass101', 'user', 4),
('hoangvane', 'pass112', 'user', 5),
('dothif', 'pass131', 'user', 6),
('buivang', 'pass415', 'user', 7),
('vuthih', 'pass161', 'admin', 8),
('ngovani', 'pass718', 'user', 9),
('maithik', 'pass192', 'user', 10);

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
INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien) VALUES
('Fast & Furious 9', 1, 143, '2021-06-25', 'Mỹ', '2D', 'Phim hành động tốc độ cao', 'Justin Lin'),
('Titanic', 2, 195, '1997-12-19', 'Mỹ', '3D', 'Tình yêu trên tàu định mệnh', 'James Cameron'),
('The Conjuring', 3, 112, '2013-07-19', 'Mỹ', '2D', 'Kinh dị dựa trên sự kiện có thật', 'James Wan'),
('Home Alone', 4, 103, '1990-11-16', 'Mỹ', '2D', 'Hài hước mùa Giáng sinh', 'Chris Columbus'),
('Interstellar', 5, 169, '2014-11-07', 'Mỹ', 'IMAX', 'Hành trình khám phá vũ trụ', 'Christopher Nolan'),
('Coco', 6, 105, '2017-11-22', 'Mỹ', '3D', 'Hành trình âm nhạc gia đình', 'Lee Unkrich'),
('Joker', 7, 122, '2019-10-04', 'Mỹ', '2D', 'Tâm lý tội phạm', 'Todd Phillips'),
('Avatar', 8, 162, '2009-12-18', 'Mỹ', '3D', 'Phiêu lưu trên Pandora', 'James Cameron'),
('Planet Earth', 9, 60, '2006-03-05', 'Anh', '2D', 'Tài liệu về thiên nhiên', 'Alastair Fothergill'),
('Tam Quốc Diễn Nghĩa', 10, 120, '1994-01-01', 'Trung Quốc', '2D', 'Lịch sử cổ trang', 'Wang Fulin');

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
(1, 1, '2025-04-06 14:00:00'),
(2, 2, '2025-04-06 16:00:00'),
(3, 3, '2025-04-06 20:00:00'),
(4, 4, '2025-04-07 10:00:00'),
(5, 5, '2025-04-07 15:00:00');
(6, 1, '2025-04-07 17:00:00'), -- Coco
(7, 2, '2025-04-07 19:00:00'), -- Joker
(8, 3, '2025-04-07 21:00:00'), -- Avatar
(9, 4, '2025-04-08 14:00:00'), -- Planet Earth
(10, 5, '2025-04-08 16:00:00'); -- Tam Quốc Diễn Nghĩa

-- Dữ liệu cho bảng HoaDon
INSERT INTO HoaDon (maNhanVien, maKhachHang, ngayLap, tongTien) VALUES
(3, 1, '2025-04-05 10:00:00', 150000.00),
(4, 2, '2025-04-05 12:00:00', 200000.00),
(NULL, 5, '2025-04-05 14:00:00', 100000.00),
(6, 7, '2025-04-05 16:00:00', 300000.00),
(8, 9, '2025-04-05 18:00:00', 250000.00);

-- Dữ liệu cho bảng Vechitiethoadon
INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES
(1, 1, 'A1', 1, 75000.00, 'paid', '2025-04-05 09:00:00'),
(1, 1, 'A2', 1, 75000.00, 'paid', '2025-04-05 09:00:00'),
(2, 2, 'A1', 2, 100000.00, 'paid', '2025-04-05 11:00:00'),
(3, 3, 'A1', 3, 100000.00, 'paid', '2025-04-05 13:00:00'),
(4, 4, 'A1', 4, 150000.00, 'paid', '2025-04-05 15:00:00'),
(5, 5, 'A1', 5, 125000.00, 'paid', '2025-04-05 17:00:00'),
(1, 1, 'B1', NULL, 75000.00, 'available', NULL),
(2, 2, 'A2', NULL, 100000.00, 'booked', '2025-04-05 10:00:00'),
(3, 3, 'B1', NULL, 100000.00, 'pending', '2025-04-05 12:00:00'),
(5, 5, 'A2', NULL, 125000.00, 'cancelled', '2025-04-05 14:00:00');

-- Dữ liệu cho bảng ChiTietHoaDon
INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES
(1, 1),
(1, 2),
(2, 3),
(3, 4),
(4, 5),
(5, 6);