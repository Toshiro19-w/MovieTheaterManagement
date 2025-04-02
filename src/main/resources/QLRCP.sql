DECLARE @sql NVARCHAR(MAX) = N'';

-- Xóa tất cả các ràng buộc FOREIGN KEY
SELECT @sql += 'ALTER TABLE ' + QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME) + 
               ' DROP CONSTRAINT ' + QUOTENAME(CONSTRAINT_NAME) + ';'
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE CONSTRAINT_TYPE = 'FOREIGN KEY';

-- Thực thi lệnh xóa khóa ngoại
EXEC sp_executesql @sql;

-- Xóa tất cả các bảng
SET @sql = N'';
SELECT @sql += 'DROP TABLE ' + QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME) + ';'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE';

EXEC sp_executesql @sql;

-- Tạo bảng NguoiDung (bảng cha của KhachHang và NhanVien)
CREATE TABLE NguoiDung (
    maNguoiDung INT IDENTITY(1,1) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(15) UNIQUE NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    loaiNguoiDung NVARCHAR(20) CHECK (loaiNguoiDung IN ('KhachHang', 'NhanVien')) NOT NULL
);

-- Tạo bảng KhachHang (thừa kế từ NguoiDung)
CREATE TABLE KhachHang (
    maKhachHang INT IDENTITY(1,1) PRIMARY KEY,
    maNguoiDung INT UNIQUE,
    diemTichLuy INT DEFAULT 0 CHECK (diemTichLuy >= 0),
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo bảng NhanVien (thừa kế từ NguoiDung)
CREATE TABLE NhanVien (
    maNhanVien INT IDENTITY(1,1) PRIMARY KEY,
    maNguoiDung INT UNIQUE,
    chucVu NVARCHAR(50) NOT NULL,
    luong DECIMAL(10,2) CHECK (luong >= 0) NOT NULL,
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo bảng TaiKhoan
CREATE TABLE TaiKhoan (
    tenDangNhap NVARCHAR(50) PRIMARY KEY,
    matKhau NVARCHAR(255) NOT NULL,
    loaiTaiKhoan NVARCHAR(10) CHECK (loaiTaiKhoan IN ('admin', 'user')) NOT NULL,
    maNguoiDung INT UNIQUE,
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo bảng TheLoaiPhim
CREATE TABLE TheLoaiPhim (
    maTheLoai INT IDENTITY(1,1) PRIMARY KEY,
    tenTheLoai NVARCHAR(50) UNIQUE NOT NULL
);

-- Tạo bảng Phim
CREATE TABLE Phim (
    maPhim INT IDENTITY(1,1) PRIMARY KEY,
    tenPhim NVARCHAR(100) NOT NULL,
    maTheLoai INT NOT NULL,
    thoiLuong INT CHECK (thoiLuong > 0) NOT NULL,
    ngayKhoiChieu DATE NOT NULL,
    nuocSanXuat NVARCHAR(50) NOT NULL,
    dinhDang NVARCHAR(20) NOT NULL,
    moTa NVARCHAR(255),
    daoDien NVARCHAR(100) NOT NULL,
    FOREIGN KEY (maTheLoai) REFERENCES TheLoaiPhim(maTheLoai) ON DELETE CASCADE
);

-- Tạo bảng PhongChieu
CREATE TABLE PhongChieu (
    maPhong INT IDENTITY(1,1) PRIMARY KEY,
    tenPhong NVARCHAR(255) UNIQUE NOT NULL,
    soLuongGhe INT CHECK (soLuongGhe > 0) NOT NULL,
    loaiPhong NVARCHAR(50) NOT NULL
);

-- Tạo bảng SuatChieu
CREATE TABLE SuatChieu (
    maSuatChieu INT IDENTITY(1,1) PRIMARY KEY,
    maPhim INT NOT NULL,
    maPhong INT NOT NULL,
    ngayGioChieu DATETIME NOT NULL,
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim) ON DELETE CASCADE,
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Tạo bảng HoaDon
CREATE TABLE HoaDon (
    maHoaDon INT IDENTITY(1,1) PRIMARY KEY,
    maNhanVien INT,
    maKhachHang INT,
    ngayLap DATETIME DEFAULT GETDATE(),
    tongTien DECIMAL(10,2) CHECK (tongTien >= 0) NOT NULL,
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien) ON DELETE NO ACTION,
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang) ON DELETE NO ACTION
);

-- Tạo bảng Ve
CREATE TABLE Ve (
    maVe INT IDENTITY(1,1) PRIMARY KEY,
    maSuatChieu INT NOT NULL,
    maKhachHang INT NULL,  -- NULL nếu vé chưa được đặt/chưa liên kết KH
    maHoaDon INT NULL,     -- NULL nếu vé chưa thanh toán
    soGhe NVARCHAR(5) NOT NULL,
    giaVe DECIMAL(10,2) CHECK (giaVe >= 0) NOT NULL,
    trangThai NVARCHAR(20) DEFAULT 'available' CHECK (trangThai IN ('available', 'booked', 'paid', 'cancelled')) NOT NULL,
    ngayDat DATETIME NULL, -- Thêm nếu cần lưu thời điểm đặt vé
    FOREIGN KEY (maSuatChieu) REFERENCES SuatChieu(maSuatChieu) ON DELETE CASCADE,
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang) ON DELETE SET NULL,
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE SET NULL
);

-- Tạo bảng ChiTietHoaDon
CREATE TABLE ChiTietHoaDon (
    maHoaDon INT NOT NULL,
    maVe INT NOT NULL,
    giaVe DECIMAL(10,2) CHECK (giaVe >= 0) NOT NULL, -- Lưu giá vé tại thời điểm mua (phòng khi giá thay đổi)
    PRIMARY KEY (maHoaDon, maVe),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    FOREIGN KEY (maVe) REFERENCES Ve(maVe) ON DELETE CASCADE
);

INSERT INTO NguoiDung (hoTen, soDienThoai, email, loaiNguoiDung) VALUES
('Nguyễn Văn A', '0987654321', 'nguyenvana@gmail.com', 'KhachHang'),
('Trần Thị B', '0912345678', 'tranthib@gmail.com', 'KhachHang'),
('Lê Hoàng C', '0909123456', 'lehoangc@gmail.com', 'NhanVien'),
('Phạm Minh D', '0934567890', 'phamminhd@gmail.com', 'NhanVien');

INSERT INTO KhachHang (maNguoiDung, diemTichLuy) VALUES
(1, 100),
(2, 50);

INSERT INTO NhanVien (maNguoiDung, chucVu, luong) VALUES
(3, 'Thu Ngân', 8000000),
(4, 'Quản Lý', 12000000);

INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES
('khach1', 'password123', 'user', 1),
('khach2', 'password456', 'user', 2),
('nv1', 'admin123', 'admin', 3),
('nv2', 'admin456', 'admin', 4);

INSERT INTO TheLoaiPhim (tenTheLoai) VALUES
('Hành Động'),
('Tình Cảm'),
('Kinh Dị'),
('Hài Hước');

INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien) VALUES
('Fast & Furious 9', 1, 130, '2023-05-10', 'Mỹ', 'IMAX', 'Phim hành động hấp dẫn về đua xe.', 'Justin Lin'),
('Titanic', 2, 195, '1997-12-19', 'Mỹ', '2D', 'Câu chuyện tình lãng mạn trên tàu Titanic.', 'James Cameron'),
('Annabelle', 3, 100, '2014-10-03', 'Mỹ', '2D', 'Búp bê ma quái.', 'John R. Leonetti'),
('Deadpool', 4, 108, '2016-02-12', 'Mỹ', 'IMAX', 'Siêu anh hùng lầy lội.', 'Tim Miller');

INSERT INTO PhongChieu (tenPhong, soLuongGhe, loaiPhong) VALUES
('Phòng 1', 50, 'Thường'),
('Phòng 2', 80, 'VIP'),
('Phòng 3', 60, 'Thường');

INSERT INTO SuatChieu (maPhim, maPhong, ngayGioChieu) VALUES
(1, 1, '2025-04-05 14:00:00'),
(2, 2, '2025-04-05 18:00:00'),
(3, 3, '2025-04-06 20:00:00'),
(4, 1, '2025-04-06 22:00:00');

INSERT INTO HoaDon (maNhanVien, maKhachHang, ngayLap, tongTien) VALUES
(1, 1, '2025-04-02', 150000),
(2, 2, '2025-04-02', 120000);

INSERT INTO Ve (maSuatChieu, maKhachHang, maHoaDon, soGhe, giaVe, trangThai, ngayDat) VALUES
(1, 1, 1, 'A1', 75000, 'paid', '2025-04-02'),
(2, 2, 2, 'B5', 60000, 'paid', '2025-04-02'),
(3, NULL, NULL, 'C3', 50000, 'available', NULL);

INSERT INTO ChiTietHoaDon (maHoaDon, maVe, giaVe) VALUES
(1, 1, 75000),
(2, 2, 60000);