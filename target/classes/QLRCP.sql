-- Xóa bảng và ràng buộc
SET FOREIGN_KEY_CHECKS = 0;
SET @tables = NULL;
SELECT GROUP_CONCAT(table_schema, '.', table_name) INTO @tables
    FROM information_schema.tables
    WHERE table_schema = 'quanlyrcp';
SET @tables = IF(@tables IS NULL, 'SELECT 1', CONCAT('DROP TABLE ', @tables));
PREPARE stmt FROM @tables;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET FOREIGN_KEY_CHECKS = 1;

-- Tạo cơ sở dữ liệu
CREATE DATABASE IF NOT EXISTS quanlyrcp;
USE quanlyrcp;

-- Tạo bảng RapChieu
CREATE TABLE IF NOT EXISTS RapChieu (
    maRap INT AUTO_INCREMENT PRIMARY KEY,
    tenRap NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255),
    soDienThoai VARCHAR(15)
);

-- Tạo bảng PhongChieu
CREATE TABLE IF NOT EXISTS PhongChieu (
    maPhong INT AUTO_INCREMENT PRIMARY KEY,
    tenPhong NVARCHAR(255) UNIQUE NOT NULL,
    soLuongGhe INT CHECK (soLuongGhe > 0) NOT NULL,
    loaiPhong NVARCHAR(50) NOT NULL,
    maRap INT NOT NULL,
    FOREIGN KEY (maRap) REFERENCES RapChieu(maRap) ON DELETE CASCADE
);

-- Tạo bảng NguoiDung
CREATE TABLE IF NOT EXISTS NguoiDung (
    maNguoiDung INT AUTO_INCREMENT PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(15) UNIQUE NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    loaiNguoiDung ENUM('KhachHang', 'NhanVien') NOT NULL
);

-- Tạo bảng KhachHang
CREATE TABLE IF NOT EXISTS KhachHang (
    maNguoiDung INT PRIMARY KEY,
    diemTichLuy INT DEFAULT 0 CHECK (diemTichLuy >= 0),
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo bảng NhanVien
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
    doTuoiGioiHan INT,
    moTa TEXT,
    daoDien NVARCHAR(100) NOT NULL,
    duongDanPoster TEXT,
    duongDanTrailer TEXT,
    FOREIGN KEY (maTheLoai) REFERENCES TheLoaiPhim(maTheLoai) ON DELETE CASCADE
);

-- Tạo bảng SuatChieu (cập nhật ON DELETE SET NULL)
CREATE TABLE IF NOT EXISTS SuatChieu (
    maSuatChieu INT AUTO_INCREMENT PRIMARY KEY,
    maPhim INT,
    maPhong INT NOT NULL,
    ngayGioChieu DATETIME NOT NULL,
    soSuatChieu INT DEFAULT 50,
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim) ON DELETE SET NULL,
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Tạo bảng Ghe
CREATE TABLE IF NOT EXISTS Ghe (
    maPhong INT NOT NULL,
    soGhe NVARCHAR(5) NOT NULL,
    PRIMARY KEY (maPhong, soGhe),
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Tạo bảng BapNuoc
CREATE TABLE IF NOT EXISTS BapNuoc (
    maSP INT AUTO_INCREMENT PRIMARY KEY,
    tenSP NVARCHAR(100) NOT NULL,
    loai ENUM('bap', 'nuoc', 'combo') NOT NULL,
    gia DECIMAL(10,2) NOT NULL CHECK (gia >= 0),
    moTa NVARCHAR(255)
);

-- Tạo bảng HoaDon
CREATE TABLE IF NOT EXISTS HoaDon (
    maHoaDon INT AUTO_INCREMENT PRIMARY KEY,
    maNhanVien INT,
    maKhachHang INT,
    ngayLap DATETIME DEFAULT CURRENT_TIMESTAMP,
    tongTien DECIMAL(10,2) CHECK (tongTien >= 0) NOT NULL,
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNguoiDung) ON DELETE SET NULL,
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maNguoiDung) ON DELETE SET NULL
);

-- Tạo bảng Ve (thêm tenPhim, cập nhật ON DELETE NO ACTION)
CREATE TABLE IF NOT EXISTS Ve (
    maVe INT AUTO_INCREMENT PRIMARY KEY,
    maSuatChieu INT NOT NULL,
    maPhong INT NOT NULL,
    soGhe NVARCHAR(5) NOT NULL,
    maHoaDon INT NULL,
    tenPhim NVARCHAR(100) NOT NULL,
    giaVe DECIMAL(10,2) CHECK (giaVe >= 0) NOT NULL,
    trangThai ENUM('booked', 'paid', 'cancelled', 'pending') NOT NULL,
    ngayDat DATETIME NULL,
    FOREIGN KEY (maSuatChieu) REFERENCES SuatChieu(maSuatChieu) ON DELETE NO ACTION,
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

-- Tạo bảng LichChieu
CREATE TABLE IF NOT EXISTS LichChieu (
    maLichChieu INT AUTO_INCREMENT PRIMARY KEY,
    maPhim INT,
    maPhong INT NOT NULL,
    ngayChieu DATE NOT NULL,
    gioBatDau TIME NOT NULL,
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim) ON DELETE SET NULL,
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Chèn dữ liệu mẫu
INSERT INTO RapChieu (tenRap, diaChi, soDienThoai) VALUES
('Rạp Galaxy Nguyễn Trãi', '123 Nguyễn Trãi, Quận 5, TP.HCM', '02838312345'),
('Rạp CGV Vincom', '456 Lê Lợi, Quận 1, TP.HCM', '02838245678'),
('Rạp Lotte Cinema', '789 Phạm Văn Đồng, Thủ Đức, TP.HCM', '02838378901');

INSERT INTO PhongChieu (tenPhong, soLuongGhe, loaiPhong, maRap) VALUES
('Phòng 1', 100, 'Thường', 1),
('Phòng 2', 80, 'VIP', 1),
('Phòng 3', 120, 'Thường', 2),
('Phòng 4', 60, 'VIP', 2),
('Phòng 5', 150, 'Thường', 3);

INSERT INTO TheLoaiPhim (tenTheLoai) VALUES
('Hành động'),
('Tình cảm'),
('Kinh dị');

INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, kieuPhim, moTa, daoDien, duongDanPoster) VALUES
('Fast & Furious 9', 1, 143, '2021-06-25', 'Mỹ', '2D', 'Phim hành động tốc độ cao', 'Justin Lin', 'FastAndFurious.jpg'),
('Titanic', 2, 195, '1997-12-19', 'Mỹ', '3D', 'Tình yêu trên tàu định mệnh', 'James Cameron', 'Titanic.jpg'),
('The Conjuring', 3, 112, '2013-07-19', 'Mỹ', '2D', 'Kinh dị dựa trên sự kiện có thật', 'James Wan', 'TheConjuring.jpg');

INSERT INTO Ghe (maPhong, soGhe) VALUES
(1, 'A1'), (1, 'A2'), (1, 'B1'),
(2, 'A1'), (2, 'A2'),
(3, 'A1');

INSERT INTO SuatChieu (maPhim, maPhong, ngayGioChieu) VALUES
(1, 1, '2025-04-06 14:00:00'),
(2, 2, '2025-04-06 16:00:00'),
(3, 3, '2025-04-06 20:00:00');

INSERT INTO NguoiDung (hoTen, soDienThoai, email, loaiNguoiDung) VALUES
('Lê Trần Minh Khôi', '0565321247', 'letranminhkhoi2506@gmail.com', 'KhachHang'),
('Nguyễn Văn A', '0901234567', 'nguyenvana@gmail.com', 'KhachHang'),
('Lê Văn C', '0923456789', 'levanc@gmail.com', 'NhanVien');

INSERT INTO KhachHang (maNguoiDung, diemTichLuy) VALUES
(1, 50),
(2, 20);

INSERT INTO NhanVien (maNguoiDung, luong, vaiTro) VALUES
(3, 15000000.00, 'QuanLyPhim');

INSERT INTO HoaDon (maNhanVien, maKhachHang, ngayLap, tongTien) VALUES
(3, 1, '2025-04-05 10:00:00', 150000.00),
(NULL, 2, '2025-04-05 12:00:00', 200000.00);

INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, tenPhim, giaVe, trangThai, ngayDat) VALUES
(1, 1, 'A1', 1, 'Fast & Furious 9', 75000.00, 'paid', '2025-04-05 09:00:00'),
(1, 1, 'A2', 1, 'Fast & Furious 9', 75000.00, 'paid', '2025-04-05 09:00:00'),
(2, 2, 'A1', 2, 'Titanic', 100000.00, 'paid', '2025-04-05 11:00:00'),
(3, 3, 'A1', NULL, 'The Conjuring', 100000.00, 'booked', '2025-04-05 13:00:00');

INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES
(1, 1),
(1, 2),
(2, 3);