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

-- Tạo bảng NhanVien (thừa kế từ NguoiDung)
CREATE TABLE IF NOT EXISTS NhanVien (
    maNguoiDung INT PRIMARY KEY,
    chucVu NVARCHAR(50) NOT NULL,
    luong DECIMAL(10,2) CHECK (luong >= 0) NOT NULL,
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

-- Tạo bảng Ghe
CREATE TABLE IF NOT EXISTS Ghe (
    maPhong INT NOT NULL,
    soGhe NVARCHAR(5) NOT NULL,
    PRIMARY KEY (maPhong, soGhe),
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Tạo bảng SuatChieu
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

-- Dữ liệu cho bảng NguoiDung
INSERT INTO NguoiDung (hoTen, soDienThoai, email, loaiNguoiDung) VALUES
(N'Nguyễn Văn An', '0905123456', 'an.nguyen@email.com', 'KhachHang'),
(N'Trần Thị Bình', '0915123456', 'binh.tran@email.com', 'KhachHang'),
(N'Lê Văn Cường', '0925123456', 'cuong.le@email.com', 'NhanVien'),
(N'Phạm Thị Duyên', '0935123456', 'duyen.pham@email.com', 'NhanVien');

-- Dữ liệu cho bảng KhachHang
INSERT INTO KhachHang (maNguoiDung, diemTichLuy) VALUES
(1, 100),
(2, 50);

-- Dữ liệu cho bảng NhanVien
INSERT INTO NhanVien (maNguoiDung, chucVu, luong) VALUES
(3, 'Quản lý', 10000000),
(4, 'Thu ngân', 6000000);

-- Dữ liệu cho bảng TaiKhoan
INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES
('nva', '123456', 'user', 1),
('ttb', '654321', 'user', 2),
('lvc', 'password', 'admin', 3),
('ptd', 'pass123', 'admin', 4);

-- Dữ liệu cho bảng TheLoaiPhim
INSERT INTO TheLoaiPhim (tenTheLoai) VALUES
('Hành động'),
('Hài hước'),
('Kinh dị'),
('Tình cảm'),
('Khoa học viễn tưởng');

-- Dữ liệu cho bảng Phim
INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien) VALUES
('John Wick 4', 1, 169, '2023-03-24', 'Mỹ', '2D', 'Phim hành động', 'Chad Stahelski'),
('Fast X', 1, 141, '2023-05-19', 'Mỹ', '3D', 'Phim hành động đua xe', 'Louis Leterrier'),
('Siêu Lừa Gặp Siêu Lầy', 2, 107, '2023-03-10', 'Việt Nam', '2D', 'Phim hài hước', 'Lý Hải'),
('Lật Mặt 6: Tấm Vé Định Mệnh', 2, 120, '2023-04-28', 'Việt Nam', '2D', 'Phim hài hành động', 'Lý Hải'),
('The Flash', 5, 144, '2023-06-16', 'Mỹ', 'IMAX', 'Phim khoa học viễn tưởng', 'Andy Muschietti');

-- Dữ liệu cho bảng PhongChieu
INSERT INTO PhongChieu (tenPhong, soLuongGhe, loaiPhong) VALUES
('Phòng 1', 50, '2D'),
('Phòng 2', 100, '3D'),
('Phòng 3', 75, 'IMAX');

-- Dữ liệu cho bảng Ghe
INSERT INTO Ghe (maPhong, soGhe) VALUES
(1, 'A1'), (1, 'A2'), (1, 'A3'), (1, 'B1'), (1, 'B2'),
(2, 'A1'), (2, 'A2'), (2, 'A3'), (2, 'B1'), (2, 'B2'), (2, 'C1'), (2, 'C2'),
(3, 'A1'), (3, 'A2'), (3, 'A3'), (3, 'B1'), (3, 'B2'), (3,'C1'), (3,'C2'), (3, 'D1');

-- Dữ liệu cho bảng SuatChieu
INSERT INTO SuatChieu (maPhim, maPhong, ngayGioChieu) VALUES
(1, 1, '2023-11-20 10:00:00'),
(2, 2, '2023-11-20 14:00:00'),
(3, 1, '2023-11-20 16:00:00'),
(4, 3, '2023-11-20 19:00:00'),
(5, 2, '2023-11-20 21:00:00');

-- Dữ liệu cho bảng HoaDon
INSERT INTO HoaDon (maNhanVien, maKhachHang, tongTien) VALUES
(3, 1, 200000),
(4, 2, 150000);

-- Dữ liệu cho bảng Vechitiethoadon
INSERT INTO Ve (maSuatChieu, maPhong, soGhe, maHoaDon, giaVe, trangThai, ngayDat) VALUES
(1, 1, 'A1', 1, 100000, 'paid', '2023-11-19 10:00:00'),
(2, 2, 'B2', 1, 100000, 'paid', '2023-11-19 10:00:00'),
(3, 1, 'B2', 2, 75000, 'paid', '2023-11-19 11:00:00'),
(4, 3, 'C1', 2, 75000, 'paid', '2023-11-19 11:00:00');

-- Dữ liệu cho bảng ChiTietHoaDon
INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES
(1, 1),
(1, 2),
(2, 3),
(2, 4);