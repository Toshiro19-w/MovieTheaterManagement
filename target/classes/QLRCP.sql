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

--Bảng thể loại phim
CREATE TABLE TheLoaiPhim (
    maTheLoai INT IDENTITY(1,1) PRIMARY KEY,
    tenTheLoai NVARCHAR(50) UNIQUE
);

--Bảng Phim
CREATE TABLE Phim (
    maPhim INT IDENTITY(1,1) PRIMARY KEY,
    tenPhim NVARCHAR(100),
    maTheLoai INT,
    thoiLuong INT,
    ngayKhoiChieu DATE,
    nuocSanXuat NVARCHAR(50),
    dinhDang NVARCHAR(20),
    moTa NVARCHAR(255),
    daoDien NVARCHAR(100),
    CONSTRAINT FK_Phim_TheLoai FOREIGN KEY (maTheLoai) REFERENCES TheLoaiPhim(maTheLoai)
);

--Bảng phòng chiếu
CREATE TABLE PhongChieu (
    maPhong INT IDENTITY(1,1) PRIMARY KEY,
	tenPhong NVARCHAR(255),
    soLuongGhe INT,
    loaiPhong NVARCHAR(50)
);

--Bảng suất chiếu
CREATE TABLE SuatChieu (
    maSuatChieu INT IDENTITY(1,1) PRIMARY KEY,
    maPhim INT,
    maPhong INT,
    ngayGioChieu DATETIME,
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim),
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong)
);

--Bảng khách hàng
CREATE TABLE KhachHang (
    maKhachHang INT IDENTITY(1,1) PRIMARY KEY,
    hoTen NVARCHAR(100),
    soDienThoai NVARCHAR(15),
    email NVARCHAR(100),
    diemTichLuy INT DEFAULT 0
);

--Bảng nhân viên
CREATE TABLE NhanVien (
    maNhanVien INT IDENTITY(1,1) PRIMARY KEY,
    hoTen NVARCHAR(100),
    soDienThoai NVARCHAR(15),
    email NVARCHAR(100),
    chucVu NVARCHAR(50),
    luong DECIMAL(10,2)
);

--Bảng hoá đơn
CREATE TABLE HoaDon (
    maHoaDon INT IDENTITY(1,1) PRIMARY KEY,
    maNhanVien INT,
    maKhachHang INT,
    ngayLap DATETIME DEFAULT GETDATE(),
    tongTien DECIMAL(10,2), -- Tính tổng từ ChiTietHoaDon
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang)
);

--Bảng vé
CREATE TABLE Ve (
    maVe INT IDENTITY(1,1) PRIMARY KEY,
    maSuatChieu INT,
    maKhachHang INT NULL,  -- NULL nếu vé chưa được đặt/chưa liên kết KH
    maHoaDon INT NULL,     -- NULL nếu vé chưa thanh toán
    soGhe NVARCHAR(5),
    giaVe DECIMAL(10,2),
    trangThai NVARCHAR(20) DEFAULT 'available', -- available/booked/paid/cancelled
    ngayDat DATETIME NULL, -- Thêm nếu cần lưu thời điểm đặt vé
    FOREIGN KEY (maSuatChieu) REFERENCES SuatChieu(maSuatChieu),
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon)
);

--Bảng chi tiết hoá đơn
CREATE TABLE ChiTietHoaDon (
    maHoaDon INT,
    maVe INT,
    giaVe DECIMAL(10,2), -- Lưu giá vé tại thời điểm mua (phòng khi giá thay đổi)
    PRIMARY KEY (maHoaDon, maVe),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maVe) REFERENCES Ve(maVe)
);

--Bảng tài khoản
CREATE TABLE TaiKhoan (
    tenDangNhap NVARCHAR(50) PRIMARY KEY,
    matKhau NVARCHAR(255),
    loaiTaiKhoan NVARCHAR(10) CHECK (loaiTaiKhoan IN ('admin', 'user'))
);

-- Thêm dữ liệu vào bảng TheLoaiPhim
INSERT INTO TheLoaiPhim (tenTheLoai) VALUES 
(N'Hành động'),
(N'Kinh dị'),
(N'Tình cảm'),
(N'Hài'),
(N'Viễn tưởng');

-- Thêm dữ liệu vào bảng Phim
INSERT INTO Phim (tenPhim, maTheLoai, thoiLuong, ngayKhoiChieu, nuocSanXuat, dinhDang, moTa, daoDien) VALUES 
('Fast & Furious', 1, 130, '2025-06-01', N'Mỹ', '2D', N'Phim đua xe hấp dẫn', 'Justin Lin'),
('IT', 2, 140, '2025-07-15', N'Mỹ', '3D', N'Chú hề ma quái', 'Andy Muschietti'),
('La La Land', 3, 128, '2025-05-20', N'Mỹ', '2D', N'Câu chuyện tình yêu âm nhạc', 'Damien Chazelle'),
('Deadpool', 4, 119, '2025-08-10', N'Mỹ', 'IMAX', N'Siêu anh hùng lầy lội', 'Tim Miller'),
('Interstellar', 5, 169, '2025-09-12', N'Mỹ', 'IMAX', N'Du hành vũ trụ', 'Christopher Nolan');

-- Thêm dữ liệu vào bảng PhongChieu
INSERT INTO PhongChieu (tenPhong, soLuongGhe, loaiPhong)  
VALUES  
(N'Phòng 1', 100, N'2D'),  
(N'Phòng 2', 120, N'3D'),  
(N'Phòng 3', 80, N'IMAX'),  
(N'Phòng 4', 150, N'VIP'),  
(N'Phòng 5', 90, N'4DX');  


-- Thêm dữ liệu vào bảng SuatChieu
INSERT INTO SuatChieu (maPhim, maPhong, ngayGioChieu) VALUES 
(1, 1, '2025-06-02 18:00'),
(2, 2, '2025-07-16 20:00'),
(3, 3, '2025-05-21 19:30');

-- Thêm dữ liệu vào bảng KhachHang
INSERT INTO KhachHang (hoTen, soDienThoai, email, diemTichLuy) VALUES 
(N'Nguyễn Văn A', '0123456789', 'a@gmail.com', 10),
(N'Trần Thị B', '0987654321', 'b@yahoo.com', 20);

-- Thêm dữ liệu vào bảng NhanVien
INSERT INTO NhanVien (hoTen, soDienThoai, email, chucVu, luong) VALUES 
(N'Lê Văn C', '0912345678', 'c@cinema.com', 'Quản lý', 15000000),
(N'Phạm Văn D', '0934567890', 'd@cinema.com', 'Nhân viên bán vé', 7000000);

-- Thêm dữ liệu vào bảng HoaDon
INSERT INTO HoaDon (maNhanVien, maKhachHang, tongTien) VALUES 
(1, 1, 200000),
(2, 2, 300000);

-- Thêm dữ liệu vào bảng Ve
INSERT INTO Ve (maSuatChieu, maKhachHang, maHoaDon, soGhe, giaVe, trangThai, ngayDat) VALUES 
(1, 1, 1, 'A1', 100000, 'paid', '2025-06-01 10:00'),
(2, 2, 2, 'B5', 150000, 'paid', '2025-07-15 15:00');

-- Thêm dữ liệu vào bảng ChiTietHoaDon
INSERT INTO ChiTietHoaDon (maHoaDon, maVe, giaVe) VALUES 
(1, 1, 100000),
(2, 2, 150000);

-- Thêm dữ liệu vào bảng TaiKhoan
INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan) VALUES 
('admin', 'admin123', 'admin'),
('user1', 'user123', 'user');

