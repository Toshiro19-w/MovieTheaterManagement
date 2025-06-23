-- Active: 1746521847255@@127.0.0.1@3306@quanlyrcp
DROP DATABASE IF EXISTS quanlyrcp;
CREATE DATABASE quanlyrcp;
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
    luong DECIMAL(10,2) CHECK (luong >= 0) NOT NULL,
    vaiTro ENUM('Admin', 'QuanLyPhim', 'ThuNgan', 'BanVe') NOT NULL,
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PhienLamViec (
    maPhien INT AUTO_INCREMENT PRIMARY KEY,
    maNhanVien INT NOT NULL,
    thoiGianBatDau DATETIME DEFAULT NOW(),
    thoiGianKetThuc DATETIME NULL,
    tongDoanhThu DECIMAL(10,2) DEFAULT 0,
    soVeDaBan INT DEFAULT 0,
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNguoiDung) ON DELETE CASCADE
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

-- Tạo bảng PhimTheLoai để quản lý quan hệ nhiều-nhiều giữa Phim và TheLoaiPhim
CREATE TABLE IF NOT EXISTS PhimTheLoai (
    maPhim INT NOT NULL,
    maTheLoai INT NOT NULL,
    PRIMARY KEY (maPhim, maTheLoai),
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim) ON DELETE CASCADE,
    FOREIGN KEY (maTheLoai) REFERENCES TheLoaiPhim(maTheLoai) ON DELETE CASCADE
);

-- Tạo bảng Phim
CREATE TABLE IF NOT EXISTS Phim (
    maPhim INT AUTO_INCREMENT PRIMARY KEY,
    tenPhim NVARCHAR(100) NOT NULL,
    thoiLuong INT CHECK (thoiLuong > 0) NOT NULL,
    ngayKhoiChieu DATE NOT NULL,
    nuocSanXuat NVARCHAR(50) NOT NULL,
    kieuPhim NVARCHAR(20) NOT NULL,
    moTa TEXT,
    daoDien NVARCHAR(100) NOT NULL,
    duongDanPoster TEXT,
    trangThai ENUM('active', 'deleted', 'upcoming') DEFAULT 'active',
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

-- Tạo trigger kiểm tra ngày khởi chiếu phim
DELIMITER //
CREATE TRIGGER update_movie_status
BEFORE INSERT ON Phim
FOR EACH ROW
BEGIN
    IF NEW.ngayKhoiChieu > CURDATE() THEN
        SET NEW.trangThai = 'upcoming';
    ELSE
        SET NEW.trangThai = 'active';
    END IF;
END //
DELIMITER ;

SET GLOBAL event_scheduler = ON;

-- Tạo event để tự động cập nhật trạng thái phim hàng ngày
DELIMITER //
DROP EVENT IF EXISTS update_movie_status_daily//
CREATE EVENT update_movie_status_daily 
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    UPDATE Phim SET trangThai = 'active'
    WHERE trangThai = 'upcoming'
    AND ngayKhoiChieu <= CURDATE();

    UPDATE Phim SET trangThai = 'deleted'
    WHERE trangThai = 'active'
    AND ngayKhoiChieu < DATE_SUB(CURDATE(), INTERVAL 10 DAY);
END//
DELIMITER ;

select * from phim;

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
DELIMITER;

-- Cập nhật procedure GetActiveMovies để bao gồm cả phim sắp chiếu
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

-- Tạo procedure mới để lấy danh sách phim sắp chiếu
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

-- Tạo bảng PhongChieu
CREATE TABLE IF NOT EXISTS PhongChieu (
    maPhong INT AUTO_INCREMENT PRIMARY KEY,
    tenPhong NVARCHAR(255) UNIQUE NOT NULL,
    soLuongGhe INT CHECK (soLuongGhe > 0) NOT NULL,
    loaiPhong NVARCHAR(50) NOT NULL
);

-- Tạo bảng Ghe (bảng mới để quản lý ghế trong phòng chiếu)
CREATE TABLE IF NOT EXISTS Ghe (
    maGhe INT AUTO_INCREMENT PRIMARY KEY,
    maPhong INT NOT NULL,
    soGhe NVARCHAR(5) NOT NULL,
    loaiGhe ENUM('Thuong', 'VIP') DEFAULT 'Thuong',
    UNIQUE (maPhong, soGhe),
    FOREIGN KEY (maPhong) REFERENCES PhongChieu(maPhong) ON DELETE CASCADE
);

-- Procedure kiểm tra ghế trống
DELIMITER //
CREATE PROCEDURE CheckAvailableSeats(IN p_maSuatChieu INT)
BEGIN
    SELECT g.maGhe, g.maPhong, g.soGhe, g.loaiGhe
    FROM Ghe g
    LEFT JOIN Ve v ON g.maGhe = v.maGhe 
        AND v.maSuatChieu = p_maSuatChieu
        AND v.trangThai NOT IN ('cancelled', 'deleted')
    WHERE v.maVe IS NULL
    AND g.maPhong = (SELECT maPhong FROM SuatChieu WHERE maSuatChieu = p_maSuatChieu);
END //
DELIMITER ;

-- Tạo bảng SuatChieu (thêm ràng buộc thời gian)
CREATE TABLE IF NOT EXISTS SuatChieu (
    maSuatChieu INT AUTO_INCREMENT PRIMARY KEY,
    maPhim INT NOT NULL,
    maPhong INT NOT NULL,
    ngayGioChieu DATETIME NOT NULL,
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

-- Tránh xung đột thời gian giữa các suất chiếu khi cập nhật
DELIMITER //
CREATE TRIGGER before_update_suatchieu
BEFORE UPDATE ON SuatChieu
FOR EACH ROW
BEGIN
    DECLARE phim_end_time DATETIME;

    -- Nếu không thay đổi thời gian hoặc phòng chiếu thì không cần kiểm tra
    IF NOT (NEW.ngayGioChieu = OLD.ngayGioChieu AND NEW.maPhong = OLD.maPhong) THEN

        -- Tính thời gian kết thúc của suất chiếu mới
        SELECT DATE_ADD(NEW.ngayGioChieu, INTERVAL p.thoiLuong MINUTE)
        INTO phim_end_time
        FROM Phim p
        WHERE p.maPhim = NEW.maPhim;

        -- Kiểm tra xung đột với các suất chiếu khác
        IF EXISTS (
            SELECT 1
            FROM SuatChieu sc
            JOIN Phim p ON sc.maPhim = p.maPhim
            WHERE sc.maPhong = NEW.maPhong
            AND sc.maSuatChieu != NEW.maSuatChieu
            AND (
                (NEW.ngayGioChieu BETWEEN sc.ngayGioChieu AND DATE_ADD(sc.ngayGioChieu, INTERVAL p.thoiLuong MINUTE))
                OR (phim_end_time BETWEEN sc.ngayGioChieu AND DATE_ADD(sc.ngayGioChieu, INTERVAL p.thoiLuong MINUTE))
                OR (NEW.ngayGioChieu < sc.ngayGioChieu AND phim_end_time > DATE_ADD(sc.ngayGioChieu, INTERVAL p.thoiLuong MINUTE))
            )
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Phòng chiếu đã được sử dụng trong khoảng thời gian này.';
        END IF;

        -- Kiểm tra xem suất chiếu đã có vé bán chưa
        IF EXISTS (
            SELECT 1 FROM Ve 
            WHERE maSuatChieu = NEW.maSuatChieu 
            AND trangThai = 'paid'
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Không thể thay đổi thời gian của suất chiếu đã có vé bán.';
        END IF;
    END IF;
END //
DELIMITER ;

-- Tạo bảng HoaDon
CREATE TABLE IF NOT EXISTS HoaDon (
    maHoaDon INT AUTO_INCREMENT PRIMARY KEY,
    maNhanVien INT,
    maKhachHang INT,
    ngayLap DATETIME DEFAULT NOW(),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNguoiDung) ON DELETE SET NULL,
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maNguoiDung) ON DELETE SET NULL
);

-- trigger tính điểm tích luỹ cho khách hàng
DELIMITER //
CREATE TRIGGER after_hoadon_insert
AFTER INSERT ON HoaDon
FOR EACH ROW
BEGIN
    DECLARE total_amount DECIMAL(10,2); -- Khai báo biến để tính tổng tiền

    -- Kiểm tra nếu hóa đơn có khách hàng
    IF NEW.maKhachHang IS NOT NULL THEN
        -- Tính tổng tiền từ các vé liên quan đến hóa đơn
        SELECT COALESCE(SUM(gv.giaVe), 0)
        INTO total_amount
        FROM ChiTietHoaDon cthd
        JOIN Ve v ON cthd.maVe = v.maVe
        JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
        WHERE cthd.maHoaDon = NEW.maHoaDon
        AND v.trangThai = 'paid';

        -- Cập nhật điểm tích lũy cho khách hàng
        UPDATE KhachHang
        SET diemTichLuy = diemTichLuy + FLOOR(total_amount / 100000)
        WHERE maNguoiDung = NEW.maKhachHang;
    END IF;
END //
DELIMITER ;

-- Tạo bảng GiaVe
CREATE TABLE IF NOT EXISTS GiaVe (
    maGiaVe INT AUTO_INCREMENT PRIMARY KEY,
    loaiGhe ENUM('Thuong', 'VIP') NOT NULL,
    ngayApDung DATE NOT NULL,
    ngayKetThuc DATE NOT NULL,
    giaVe DECIMAL(10,2) NOT NULL,
    ghiChu TEXT
);

select * from giave;

DELIMITER //
CREATE TRIGGER before_giave_insert
BEFORE INSERT ON GiaVe
FOR EACH ROW
BEGIN
    -- Cập nhật ngayKetThuc cho giá vé cũ của cùng loại ghế
    UPDATE GiaVe
    SET ngayKetThuc = DATE_SUB(NEW.ngayApDung, INTERVAL 1 DAY)
    WHERE loaiGhe = NEW.loaiGhe
    AND (ngayKetThuc IS NULL OR ngayKetThuc > NEW.ngayApDung)
    AND ngayApDung < NEW.ngayApDung;
END //
DELIMITER ;

-- Tạo bảng Ve
CREATE TABLE IF NOT EXISTS Ve (
    maVe INT AUTO_INCREMENT PRIMARY KEY,
    maSuatChieu INT NOT NULL,
    maGhe INT NOT NULL,
    maHoaDon INT NULL,
    maGiaVe INT NOT NULL,
    maKhuyenMai INT NULL, -- Thêm cột để liên kết với KhuyenMai
    trangThai ENUM('booked', 'paid', 'cancelled', 'pending') NOT NULL,
    ngayDat DATETIME NULL,
    FOREIGN KEY (maSuatChieu) REFERENCES SuatChieu(maSuatChieu) ON DELETE CASCADE,
    FOREIGN KEY (maGhe) REFERENCES Ghe(maGhe) ON DELETE NO ACTION,
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE RESTRICT,
    FOREIGN KEY (maGiaVe) REFERENCES GiaVe(maGiaVe),
    FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai) ON DELETE SET NULL,
    CONSTRAINT UQ_SuatChieu_MaGhe UNIQUE (maSuatChieu, maGhe),
    CONSTRAINT check_paid_hoadon CHECK (trangThai != 'paid' OR maHoaDon IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS LichSuGiaVe (
    maLichSu INT AUTO_INCREMENT PRIMARY KEY,
    loaiGhe ENUM('Thuong', 'VIP') NOT NULL,
    giaVeCu DECIMAL(10,2) NOT NULL,
    giaVeMoi DECIMAL(10,2) NOT NULL,
    ngayThayDoi DATETIME DEFAULT NOW(),
    nguoiThayDoi INT,
    FOREIGN KEY (nguoiThayDoi) REFERENCES NhanVien(maNguoiDung) ON DELETE SET NULL
);

-- Trigger để lưu lịch sử khi giá vé thay đổi
DELIMITER //
CREATE TRIGGER after_giave_update
AFTER UPDATE ON GiaVe
FOR EACH ROW
BEGIN
    INSERT INTO LichSuGiaVe (loaiGhe, giaVeCu, giaVeMoi, ngayThayDoi)
    VALUES (NEW.loaiGhe, OLD.giaVe, NEW.giaVe, NOW());
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER before_ve_insert
BEFORE INSERT ON Ve
FOR EACH ROW
BEGIN
    DECLARE phim_ngayKhoiChieu DATE;
    
    SELECT p.ngayKhoiChieu INTO phim_ngayKhoiChieu
    FROM Phim p
    JOIN SuatChieu sc ON p.maPhim = sc.maPhim
    WHERE sc.maSuatChieu = NEW.maSuatChieu;
    
    IF DATE(NEW.ngayDat) < phim_ngayKhoiChieu THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể đặt vé trước ngày khởi chiếu phim.';
    END IF;
END //
DELIMITER ;

-- View lịch chiếu phim
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

SELECT * FROM lichchieuphim;

-- Tạo bảng ChiTietHoaDon
CREATE TABLE IF NOT EXISTS ChiTietHoaDon (
    maHoaDon INT NOT NULL,
    maVe INT NOT NULL,
    PRIMARY KEY (maHoaDon, maVe),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    FOREIGN KEY (maVe) REFERENCES Ve(maVe) ON DELETE CASCADE
);

-- View thống kê hóa đơn
CREATE OR REPLACE VIEW ThongKeHoaDon AS
SELECT 
    h.maHoaDon,
    h.maNhanVien,
    h.maKhachHang,
    h.ngayLap,
    COALESCE(SUM(CASE 
        WHEN v.maKhuyenMai IS NOT NULL THEN
            CASE 
                WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (1 - km.giaTriGiam/100)
                WHEN km.loaiGiamGia = 'CoDinh' THEN GREATEST(gv.giaVe - km.giaTriGiam, 0)
                ELSE gv.giaVe
            END
        ELSE gv.giaVe
    END), 0) as tongTien
FROM HoaDon h
LEFT JOIN ChiTietHoaDon cthd ON h.maHoaDon = cthd.maHoaDon
LEFT JOIN Ve v ON cthd.maVe = v.maVe
LEFT JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
WHERE v.trangThai = 'paid'
GROUP BY h.maHoaDon, h.maNhanVien, h.maKhachHang, h.ngayLap;

-- Thêm dữ liệu mẫu cho bảng KhuyenMai
CREATE TABLE IF NOT EXISTS KhuyenMai (
    maKhuyenMai INT AUTO_INCREMENT PRIMARY KEY,
    tenKhuyenMai NVARCHAR(100) NOT NULL UNIQUE,
    moTa TEXT,
    loaiGiamGia ENUM('PhanTram', 'CoDinh') NOT NULL,
    giaTriGiam DECIMAL(10,2) CHECK (giaTriGiam >= 0),
    ngayBatDau DATE NOT NULL,
    ngayKetThuc DATE NOT NULL,
    trangThai ENUM('HoatDong', 'HetHan', 'DaHuy') DEFAULT 'HoatDong',
    CHECK (ngayKetThuc >= ngayBatDau)
);

-- Tạo bảng DieuKienKhuyenMai
CREATE TABLE IF NOT EXISTS DieuKienKhuyenMai (
    maDieuKien INT AUTO_INCREMENT PRIMARY KEY,
    maKhuyenMai INT NOT NULL,
    loaiDieuKien ENUM('Phim') NOT NULL,
    maPhim INT NOT NULL,
    FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai) ON DELETE CASCADE,
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim) ON DELETE CASCADE,
    CONSTRAINT UQ_KhuyenMai_Phim UNIQUE (maKhuyenMai, maPhim)
);

-- Thêm bảng DanhGia để lưu feedback của khách hàng
CREATE TABLE IF NOT EXISTS DanhGia (
    maDanhGia INT AUTO_INCREMENT PRIMARY KEY,
    maPhim INT NOT NULL,
    maNguoiDung INT NOT NULL,
    maVe INT NOT NULL, -- Thêm cột để liên kết với Ve
    diemDanhGia INT CHECK (diemDanhGia BETWEEN 1 AND 5),
    nhanXet TEXT,
    ngayDanhGia DATETIME DEFAULT NOW(),
    FOREIGN KEY (maPhim) REFERENCES Phim(maPhim),
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung),
    FOREIGN KEY (maVe) REFERENCES Ve(maVe) ON DELETE CASCADE
);

DELIMITER //
CREATE TRIGGER before_danhgia_insert
BEFORE INSERT ON DanhGia
FOR EACH ROW
BEGIN
    DECLARE phim_ngayKhoiChieu DATE;
    DECLARE ve_trangThai ENUM('booked', 'paid', 'cancelled', 'pending');
    
    SELECT ngayKhoiChieu INTO phim_ngayKhoiChieu
    FROM Phim
    WHERE maPhim = NEW.maPhim;
    
    SELECT trangThai INTO ve_trangThai
    FROM Ve
    WHERE maVe = NEW.maVe;
    
    IF DATE(NEW.ngayDanhGia) < phim_ngayKhoiChieu THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể đánh giá phim trước ngày khởi chiếu.';
    END IF;
    
    IF ve_trangThai != 'paid' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Chỉ có thể đánh giá với vé đã thanh toán.';
    END IF;
END //
DELIMITER ;

-- View thống kê doanh thu theo phim
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

select * from VeView;

CREATE OR REPLACE VIEW VeView AS
SELECT 
    v.maVe AS MaVe,
    v.trangThai AS TrangThai,
    g.soGhe AS SoGhe,
    gv.giaVe AS GiaVeGoc,
    CASE 
        WHEN v.maKhuyenMai IS NOT NULL THEN
            CASE 
                WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (km.giaTriGiam / 100)
                WHEN km.loaiGiamGia = 'CoDinh' THEN km.giaTriGiam
                ELSE 0
            END
        ELSE 0
    END AS TienGiam,
    CASE 
        WHEN v.maKhuyenMai IS NOT NULL THEN
            CASE 
                WHEN km.loaiGiamGia = 'PhanTram' THEN gv.giaVe * (1 - km.giaTriGiam / 100)
                WHEN km.loaiGiamGia = 'CoDinh' THEN GREATEST(gv.giaVe - km.giaTriGiam, 0)
                ELSE gv.giaVe
            END
        ELSE gv.giaVe
    END AS GiaVeSauGiam,
    v.ngayDat AS NgayDat,
    pc.tenPhong AS TenPhong,
    sc.ngayGioChieu AS NgayGioChieu,
    p.tenPhim AS TenPhim,
    COALESCE(km.tenKhuyenMai, 'Không có') AS TenKhuyenMai
FROM 
    Ve v
    INNER JOIN Ghe g ON v.maGhe = g.maGhe
    INNER JOIN PhongChieu pc ON g.maPhong = pc.maPhong
    INNER JOIN SuatChieu sc ON v.maSuatChieu = sc.maSuatChieu
    INNER JOIN Phim p ON sc.maPhim = p.maPhim
    INNER JOIN GiaVe gv ON v.maGiaVe = gv.maGiaVe
    LEFT JOIN KhuyenMai km ON v.maKhuyenMai = km.maKhuyenMai
WHERE 
    p.trangThai = 'active'
ORDER BY 
    v.maVe;

-- Tạo các chỉ mục để tối ưu hóa truy vấn
CREATE INDEX idx_phim_maTheLoai ON Phim(maTheLoai);
CREATE INDEX idx_ve_maSuatChieu ON Ve(maSuatChieu);
CREATE INDEX idx_ve_maGhe ON Ve(maGhe);
CREATE INDEX idx_hoadon_maKhachHang ON HoaDon(maKhachHang);

-- Chỉ mục cho các trường thường xuyên tìm kiếm
CREATE INDEX idx_suatchieu_ngaygiochieu ON SuatChieu(ngayGioChieu);
CREATE INDEX idx_ve_trangthai ON Ve(trangThai);
CREATE INDEX idx_phim_trangthai_ngaykhoichieu ON Phim(trangThai, ngayKhoiChieu);
CREATE INDEX idx_danhgia_maphim ON DanhGia(maPhim);

-- Tạo chỉ mục mới
CREATE INDEX idx_phimtheloai_maphim ON PhimTheLoai(maPhim);
CREATE INDEX idx_phimtheloai_matheloai ON PhimTheLoai(maTheLoai);

-- Thêm cột phiên bản cho các bảng quan trọng để hỗ trợ khóa lạc quan (optimistic locking)
ALTER TABLE Ve ADD COLUMN phienBan INT DEFAULT 1;
ALTER TABLE HoaDon ADD COLUMN phienBan INT DEFAULT 1;
ALTER TABLE SuatChieu ADD COLUMN phienBan INT DEFAULT 1;
ALTER TABLE Ghe ADD COLUMN phienBan INT DEFAULT 1;

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

select * from khachhang;

-- Dữ liệu cho bảng NhanVien
INSERT INTO NhanVien (maNguoiDung, luong, vaiTro) VALUES
(4, 15000000.00, 'QuanLyPhim'),
(5, 8000000.00, 'ThuNgan'),
(7, 7000000.00, 'BanVe'),
(9, 20000000.00, 'Admin'),
(11, 7000000.00, 'BanVe');

-- Dữ liệu cho bảng TaiKhoan
INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES
('letranminhkhoi', 'pass123', 'User', 1),
('nguyenvana', 'pass123', 'User', 2),
('tranthib', 'pass456', 'User', 3),
('levanc', 'pass789', 'QuanLyPhim', 4),
('phamthid', 'pass101', 'ThuNgan', 5),
('hoangvane', 'pass112', 'User', 6),
('dothif', 'pass131', 'BanVe', 7),
('buivang', 'pass415', 'User', 8),
('vuthih', 'pass161', 'Admin', 9),
('ngovani', 'pass718', 'User', 10),
('maithik', 'pass192', 'BanVe', 11);

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
INSERT INTO Phim (tenPhim, thoiLuong, ngayKhoiChieu, nuocSanXuat, kieuPhim, moTa, daoDien, duongDanPoster) VALUES
('Avatar 3', 180, '2025-01-20', 'Mỹ', '3D IMAX', 'Phần tiếp theo của Avatar', 'James Cameron', 'Avatar3.jpg'),
('Mission: Impossible 8', 150, '2025-05-23', 'Mỹ', 'IMAX', 'Nhiệm vụ bất khả thi mới', 'Christopher McQuarrie', 'MI8.jpg'),
('The Batman 2', 165, '2025-10-03', 'Mỹ', 'IMAX', 'Phần tiếp theo của Batman', 'Matt Reeves', 'Batman2.jpg'),
('Fantastic Beasts 4', 140, '2025-07-15', 'Mỹ', '3D', 'Phần mới của Sinh vật huyền bí', 'David Yates', 'FB4.jpg'),
('Captain America 4', 155, '2025-03-14', 'Mỹ', 'IMAX', 'Cuộc phiêu lưu mới của Captain America', 'Julius Onah', 'Cap4.jpg'),
('Deadpool 3', 130, '2025-07-26', 'Mỹ', 'IMAX', 'Deadpool trở lại với Wolverine', 'Shawn Levy', 'Deadpool3.jpg'),
('Joker 2', 138, '2025-10-04', 'Mỹ', 'IMAX', 'Phần tiếp theo của Joker', 'Todd Phillips', 'Joker2.jpg'),
('Spider-Man 4', 145, '2025-06-27', 'Mỹ', 'IMAX', 'Cuộc phiêu lưu mới của Spider-Man', 'Jon Watts', 'SpiderMan4.jpg'),
('The Matrix 5', 160, '2025-08-22', 'Mỹ', 'IMAX', 'Phần tiếp theo của The Matrix', 'Lana Wachowski', 'Matrix5.jpg'),
('Black Panther 3', 150, '2025-11-07', 'Mỹ', 'IMAX', 'Phần tiếp theo của Black Panther', 'Ryan Coogler', 'BP3.jpg');
INSERT INTO Phim (tenPhim, thoiLuong, ngayKhoiChieu, nuocSanXuat, kieuPhim, moTa, daoDien, duongDanPoster) VALUES
('Dune: Part Three', 165, '2025-06-09', 'Mỹ', 'IMAX', 'Phần cuối của series Dune', 'Denis Villeneuve', 'Dune3.jpg'),
('Fast & Furious 11', 140, '2025-06-24', 'Mỹ', 'IMAX', 'Phần mới nhất của Fast Saga', 'Louis Leterrier', 'FF11.jpg'),
('Blade', 135, '2025-06-24', 'Mỹ', 'IMAX', 'Phiên bản reboot của Marvel', 'Yann Demange', 'Blade.jpg'),
('Planet of the Apes 4', 155, '2025-06-24', 'Mỹ', '3D', 'Phần tiếp theo của Planet of the Apes', 'Wes Ball', 'Apes4.jpg'),
('John Wick 5', 130, '2025-06-24', 'Mỹ', 'IMAX', 'Chapter cuối của John Wick', 'Chad Stahelski', 'JW5.jpg'),
('Godzilla vs Kong 2', 145, '2025-06-24', 'Mỹ', 'IMAX', 'Cuộc chiến tiếp theo của hai quái vật', 'Adam Wingard', 'GvK2.jpg'),
('Indiana Jones 6', 140, '2025-06-24', 'Mỹ', 'IMAX', 'Cuộc phiêu lưu mới của Indiana Jones', 'James Mangold', 'IJ6.jpg'),
('Mad Max: The Wasteland', 150, '2025-06-24', 'Úc', 'IMAX', 'Phần tiếp theo của series Mad Max', 'George Miller', 'MM5.jpg'),
('The Flash 2', 135, '2025-06-24', 'Mỹ', 'IMAX', 'Phần tiếp theo của The Flash', 'Andy Muschietti', 'Flash2.jpg');

INSERT INTO Phim (tenPhim, thoiLuong, ngayKhoiChieu, nuocSanXuat, kieuPhim, moTa, daoDien, duongDanPoster) VALUES
('Fast & Furious 11', 140, '2025-06-24', 'Mỹ', 'IMAX', 'Phần mới nhất của Fast Saga', 'Louis Leterrier', 'FF11.jpg'),
('Blade', 135, '2025-06-24', 'Mỹ', 'IMAX', 'Phiên bản reboot của Marvel', 'Yann Demange', 'Blade.jpg');

INSERT INTO SuatChieu (maPhim, maPhong, ngayGioChieu) VALUES
(13, 1, '2025-06-10 18:00:00'),
(14, 1, '2025-06-10 18:00:00');

-- Dữ liệu cho bảng PhongChieu
INSERT INTO PhongChieu (tenPhong, soLuongGhe, loaiPhong) VALUES
('Phòng 1', 100, 'Thường'),
('Phòng 2', 80, 'VIP'),
('Phòng 3', 120, 'Thường'),
('Phòng 4', 60, 'VIP'),
('Phòng 5', 150, 'Thường');

select * from phim;

-- Dữ liệu cho bảng Ghe
INSERT INTO Ghe (maPhong, loaiGhe, soGhe) VALUES
-- Phòng 1 (100 ghế)
(1, 'Thuong', 'A1'), (1, 'Thuong', 'A2'), (1, 'Thuong', 'A3'), (1, 'Thuong', 'A4'), (1, 'Thuong', 'A5'), (1, 'Thuong', 'A6'), (1, 'Thuong', 'A7'), (1, 'Thuong', 'A8'), (1, 'Thuong', 'A9'), (1, 'Thuong', 'A10'),
(1, 'VIP', 'B1'), (1, 'VIP', 'B2'), (1, 'VIP', 'B3'), (1, 'VIP', 'B4'), (1, 'VIP', 'B5'), (1, 'VIP', 'B6'), (1, 'VIP', 'B7'), (1, 'VIP', 'B8'), (1, 'VIP', 'B9'), (1, 'VIP', 'B10'),
(1, 'Thuong', 'C1'), (1, 'Thuong', 'C2'), (1, 'Thuong', 'C3'), (1, 'Thuong', 'C4'), (1, 'Thuong', 'C5'), (1, 'Thuong', 'C6'), (1, 'Thuong', 'C7'), (1, 'Thuong', 'C8'), (1, 'Thuong', 'C9'), (1, 'Thuong', 'C10'),
(1, 'VIP', 'D1'), (1, 'VIP', 'D2'), (1, 'VIP', 'D3'), (1, 'VIP', 'D4'), (1, 'VIP', 'D5'), (1, 'VIP', 'D6'), (1, 'VIP', 'D7'), (1, 'VIP', 'D8'), (1, 'VIP', 'D9'), (1, 'VIP', 'D10'),
(1, 'Thuong', 'E1'), (1, 'Thuong', 'E2'), (1, 'Thuong', 'E3'), (1, 'Thuong', 'E4'), (1, 'Thuong', 'E5'), (1, 'Thuong', 'E6'), (1, 'Thuong', 'E7'), (1, 'Thuong', 'E8'), (1, 'Thuong', 'E9'), (1, 'Thuong', 'E10'),
(1, 'Thuong', 'F1'), (1, 'Thuong', 'F2'), (1, 'Thuong', 'F3'), (1, 'Thuong', 'F4'), (1, 'Thuong', 'F5'), (1, 'Thuong', 'F6'), (1, 'Thuong', 'F7'), (1, 'Thuong', 'F8'), (1, 'Thuong', 'F9'), (1, 'Thuong', 'F10'),
(1, 'Thuong', 'G1'), (1, 'Thuong', 'G2'), (1, 'Thuong', 'G3'), (1, 'Thuong', 'G4'), (1, 'Thuong', 'G5'), (1, 'Thuong', 'G6'), (1, 'Thuong', 'G7'), (1, 'Thuong', 'G8'), (1, 'Thuong', 'G9'), (1, 'Thuong', 'G10'),
(1, 'Thuong', 'H1'), (1, 'Thuong', 'H2'), (1, 'Thuong', 'H3'), (1, 'Thuong', 'H4'), (1, 'Thuong', 'H5'), (1, 'Thuong', 'H6'), (1, 'Thuong', 'H7'), (1, 'Thuong', 'H8'), (1, 'Thuong', 'H9'), (1, 'Thuong', 'H10'),
(1, 'Thuong', 'I1'), (1, 'Thuong', 'I2'), (1, 'Thuong', 'I3'), (1, 'Thuong', 'I4'), (1, 'Thuong', 'I5'), (1, 'Thuong', 'I6'), (1, 'Thuong', 'I7'), (1, 'Thuong', 'I8'), (1, 'Thuong', 'I9'), (1, 'Thuong', 'I10'),
(1, 'Thuong', 'J1'), (1, 'Thuong', 'J2'), (1, 'Thuong', 'J3'), (1, 'Thuong', 'J4'), (1, 'Thuong', 'J5'), (1, 'Thuong', 'J6'), (1, 'Thuong', 'J7'), (1, 'Thuong', 'J8'), (1, 'Thuong', 'J9'), (1, 'Thuong', 'J10'),
-- Phòng 2 (80 ghế, full VIP, 8 hàng mỗi hàng 10 ghế)
(2, 'VIP', 'A1'), (2, 'VIP', 'A2'), (2, 'VIP', 'A3'), (2, 'VIP', 'A4'), (2, 'VIP', 'A5'), (2, 'VIP', 'A6'), (2, 'VIP', 'A7'), (2, 'VIP', 'A8'), (2, 'VIP', 'A9'), (2, 'VIP', 'A10'),
(2, 'VIP', 'B1'), (2, 'VIP', 'B2'), (2, 'VIP', 'B3'), (2, 'VIP', 'B4'), (2, 'VIP', 'B5'), (2, 'VIP', 'B6'), (2, 'VIP', 'B7'), (2, 'VIP', 'B8'), (2, 'VIP', 'B9'), (2, 'VIP', 'B10'),
(2, 'VIP', 'C1'), (2, 'VIP', 'C2'), (2, 'VIP', 'C3'), (2, 'VIP', 'C4'), (2, 'VIP', 'C5'), (2, 'VIP', 'C6'), (2, 'VIP', 'C7'), (2, 'VIP', 'C8'), (2, 'VIP', 'C9'), (2, 'VIP', 'C10'),
(2, 'VIP', 'D1'), (2, 'VIP', 'D2'), (2, 'VIP', 'D3'), (2, 'VIP', 'D4'), (2, 'VIP', 'D5'), (2, 'VIP', 'D6'), (2, 'VIP', 'D7'), (2, 'VIP', 'D8'), (2, 'VIP', 'D9'), (2, 'VIP', 'D10'),
(2, 'VIP', 'E1'), (2, 'VIP', 'E2'), (2, 'VIP', 'E3'), (2, 'VIP', 'E4'), (2, 'VIP', 'E5'), (2, 'VIP', 'E6'), (2, 'VIP', 'E7'), (2, 'VIP', 'E8'), (2, 'VIP', 'E9'), (2, 'VIP', 'E10'),
(2, 'VIP', 'F1'), (2, 'VIP', 'F2'), (2, 'VIP', 'F3'), (2, 'VIP', 'F4'), (2, 'VIP', 'F5'), (2, 'VIP', 'F6'), (2, 'VIP', 'F7'), (2, 'VIP', 'F8'), (2, 'VIP', 'F9'), (2, 'VIP', 'F10'),
(2, 'VIP', 'G1'), (2, 'VIP', 'G2'), (2, 'VIP', 'G3'), (2, 'VIP', 'G4'), (2, 'VIP', 'G5'), (2, 'VIP', 'G6'), (2, 'VIP', 'G7'), (2, 'VIP', 'G8'), (2, 'VIP', 'G9'), (2, 'VIP', 'G10'),
(2, 'VIP', 'H1'), (2, 'VIP', 'H2'), (2, 'VIP', 'H3'), (2, 'VIP', 'H4'), (2, 'VIP', 'H5'), (2, 'VIP', 'H6'), (2, 'VIP', 'H7'), (2, 'VIP', 'H8'), (2, 'VIP', 'H9'), (2, 'VIP', 'H10'),
-- Phòng 3 (120 ghế, full thường, 12 hàng mỗi hàng 10 ghế)
(3, 'Thuong', 'A1'), (3, 'Thuong', 'A2'), (3, 'Thuong', 'A3'), (3, 'Thuong', 'A4'), (3, 'Thuong', 'A5'), (3, 'Thuong', 'A6'), (3, 'Thuong', 'A7'), (3, 'Thuong', 'A8'), (3, 'Thuong', 'A9'), (3, 'Thuong', 'A10'),
(3, 'Thuong', 'B1'), (3, 'Thuong', 'B2'), (3, 'Thuong', 'B3'), (3, 'Thuong', 'B4'), (3, 'Thuong', 'B5'), (3, 'Thuong', 'B6'), (3, 'Thuong', 'B7'), (3, 'Thuong', 'B8'), (3, 'Thuong', 'B9'), (3, 'Thuong', 'B10'),
(3, 'Thuong', 'C1'), (3, 'Thuong', 'C2'), (3, 'Thuong', 'C3'), (3, 'Thuong', 'C4'), (3, 'Thuong', 'C5'), (3, 'Thuong', 'C6'), (3, 'Thuong', 'C7'), (3, 'Thuong', 'C8'), (3, 'Thuong', 'C9'), (3, 'Thuong', 'C10'),
(3, 'Thuong', 'D1'), (3, 'Thuong', 'D2'), (3, 'Thuong', 'D3'), (3, 'Thuong', 'D4'), (3, 'Thuong', 'D5'), (3, 'Thuong', 'D6'), (3, 'Thuong', 'D7'), (3, 'Thuong', 'D8'), (3, 'Thuong', 'D9'), (3, 'Thuong', 'D10'),
(3, 'VIP',  'E1'), (3, 'VIP','E2'), (3,'VIP','E3'), (3, 'VIP', 'E4'), (3, 'VIP', 'E5'), (3, 'VIP', 'E6'), (3, 'VIP', 'E7'), (3, 'VIP', 'E8'), (3, 'VIP', 'E9'), (3, 'VIP', 'E10'),
(3, 'Thuong', 'F1'), (3, 'Thuong', 'F2'), (3, 'Thuong', 'F3'), (3, 'Thuong', 'F4'), (3, 'Thuong', 'F5'), (3, 'Thuong', 'F6'), (3, 'Thuong', 'F7'), (3, 'Thuong', 'F8'), (3, 'Thuong', 'F9'), (3, 'Thuong', 'F10'),
(3, 'Thuong', 'G1'), (3, 'Thuong', 'G2'), (3, 'Thuong', 'G3'), (3, 'Thuong', 'G4'), (3, 'Thuong', 'G5'), (3, 'Thuong', 'G6'), (3, 'Thuong', 'G7'), (3, 'Thuong', 'G8'), (3, 'Thuong', 'G9'), (3, 'Thuong', 'G10'),
(3, 'VIP', 'H1'), (3, 'VIP', 'H2'), (3, 'VIP', 'H3'), (3, 'VIP', 'H4'), (3, 'VIP', 'H5'), (3, 'VIP', 'H6'), (3, 'VIP', 'H7'), (3, 'VIP', 'H8'), (3, 'VIP', 'H9'), (3, 'VIP', 'H10'),
(3, 'VIP', 'I1'), (3, 'VIP', 'I2'), (3, 'VIP', 'I3'), (3, 'VIP', 'I4'), (3, 'VIP', 'I5'), (3, 'VIP', 'I6'), (3, 'VIP', 'I7'), (3, 'VIP', 'I8'), (3, 'VIP', 'I9'), (3, 'VIP', 'I10'),
(3, 'VIP', 'J1'), (3, 'VIP', 'J2'), (3, 'VIP', 'J3'), (3, 'VIP', 'J4'), (3, 'VIP', 'J5'), (3, 'VIP', 'J6'), (3, 'VIP', 'J7'), (3, 'VIP', 'J8'), (3, 'VIP', 'J9'), (3, 'VIP', 'J10'),
-- Phòng 4 (60 ghế, full VIP, 6 hàng mỗi hàng 10 ghế)
(4, 'VIP', 'A1'), (4, 'VIP', 'A2'), (4, 'VIP', 'A3'), (4, 'VIP', 'A4'), (4, 'VIP', 'A5'), (4, 'VIP', 'A6'),
(4, 'VIP', 'B1'), (4, 'VIP', 'B2'), (4, 'VIP', 'B3'), (4, 'VIP', 'B4'), (4, 'VIP', 'B5'), (4, 'VIP', 'B6'),
(4, 'VIP', 'C1'), (4, 'VIP', 'C2'), (4, 'VIP', 'C3'), (4, 'VIP', 'C4'), (4, 'VIP', 'C5'), (4, 'VIP', 'C6'),
(4, 'VIP', 'D1'), (4, 'VIP', 'D2'), (4, 'VIP', 'D3'), (4, 'VIP', 'D4'), (4, 'VIP', 'D5'), (4, 'VIP', 'D6'),
(4, 'VIP', 'E1'), (4, 'VIP', 'E2'), (4, 'VIP', 'E3'), (4, 'VIP', 'E4'), (4, 'VIP', 'E5'), (4, 'VIP', 'E6'),
(4, 'VIP', 'F1'), (4, 'VIP', 'F2'), (4, 'VIP', 'F3'), (4, 'VIP', 'F4'), (4, 'VIP', 'F5'), (4, 'VIP', 'F6'),
-- Phòng 5 (150 ghế, full thường, 10 hàng mỗi hàng 15 ghế, thêm tạm 70 ghế)
(5, 'Thuong', 'A1'), (5, 'Thuong', 'A2'), (5, 'Thuong', 'A3'), (5, 'Thuong', 'A4'), (5, 'Thuong', 'A5'), (5, 'Thuong', 'A6'), (5, 'Thuong', 'A7'), (5, 'Thuong', 'A8'), (5, 'Thuong', 'A9'), (5, 'Thuong', 'A10'),
(5, 'Thuong', 'B1'), (5, 'Thuong', 'B2'), (5, 'Thuong', 'B3'), (5, 'Thuong', 'B4'), (5, 'Thuong', 'B5'), (5, 'Thuong', 'B6'), (5, 'Thuong', 'B7'), (5, 'Thuong', 'B8'), (5, 'Thuong', 'B9'), (5, 'Thuong', 'B10'),
(5, 'Thuong', 'C1'), (5, 'Thuong', 'C2'), (5, 'Thuong', 'C3'), (5, 'Thuong', 'C4'), (5, 'Thuong', 'C5'), (5, 'Thuong', 'C6'), (5, 'Thuong', 'C7'), (5, 'Thuong', 'C8'), (5, 'Thuong', 'C9'), (5, 'Thuong', 'C10'),
(5, 'Thuong', 'D1'), (5, 'Thuong', 'D2'), (5, 'Thuong', 'D3'), (5, 'Thuong', 'D4'), (5, 'Thuong', 'D5'), (5, 'Thuong', 'D6'), (5, 'Thuong', 'D7'), (5, 'Thuong', 'D8'), (5, 'Thuong', 'D9'), (5, 'Thuong', 'D10'),
(5, 'Thuong', 'E1'), (5, 'Thuong', 'E2'), (5, 'Thuong', 'E3'), (5, 'Thuong', 'E4'), (5, 'Thuong', 'E5'), (5, 'Thuong', 'E6'), (5, 'Thuong', 'E7'), (5, 'Thuong', 'E8'), (5, 'Thuong', 'E9'), (5, 'Thuong', 'E10'),
(5, 'Thuong', 'F1'), (5, 'Thuong', 'F2'), (5, 'Thuong', 'F3'), (5, 'Thuong', 'F4'), (5, 'Thuong', 'F5'), (5, 'Thuong', 'F6'), (5, 'Thuong', 'F7'), (5, 'Thuong', 'F8'), (5, 'Thuong', 'F9'), (5, 'Thuong', 'F10'),
(5, 'Thuong', 'G1'), (5, 'Thuong', 'G2'), (5, 'Thuong', 'G3'), (5, 'Thuong', 'G4'), (5, 'Thuong', 'G5'), (5, 'Thuong', 'G6'), (5, 'Thuong', 'G7'), (5, 'Thuong', 'G8'), (5, 'Thuong', 'G9'), (5, 'Thuong', 'G10');

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
(10, 5, '2025-11-07 14:00:00'), -- Black Panther 3
-- Thêm suất chiếu cho Dune: Part Three (ID: 11)
(11, 1, '2025-06-24 10:00:00'),
(11, 2, '2025-06-25 14:00:00'),
(11, 3, '2025-06-26 18:00:00'),
(11, 4, '2025-06-27 10:00:00'),
(11, 5, '2025-06-28 14:00:00'),
(11, 1, '2025-06-29 18:00:00'),
-- Fast & Furious 11 (ID: 12)
(12, 2, '2025-06-24 14:00:00'),
(12, 3, '2025-06-25 18:00:00'),
(12, 4, '2025-06-26 10:00:00'),
(12, 5, '2025-06-27 14:00:00'),
(12, 1, '2025-06-28 18:00:00'),
(12, 2, '2025-06-29 10:00:00'),
-- Blade (ID: 13)
(13, 3, '2025-06-24 18:00:00'),
(13, 4, '2025-06-25 10:00:00'),
(13, 5, '2025-06-26 14:00:00'),
(13, 1, '2025-06-27 18:00:00'),
(13, 2, '2025-06-28 10:00:00'),
(13, 3, '2025-06-29 14:00:00'),
-- Planet of the Apes 4 (ID: 14)
(14, 4, '2025-06-24 10:00:00'),
(14, 5, '2025-06-25 14:00:00'),
(14, 1, '2025-06-26 18:00:00'),
(14, 2, '2025-06-27 10:00:00'),
(14, 3, '2025-06-28 14:00:00'),
(14, 4, '2025-06-29 18:00:00'),
-- John Wick 5 (ID: 15)
(15, 5, '2025-06-24 14:00:00'),
(15, 1, '2025-06-25 18:00:00'),
(15, 2, '2025-06-26 10:00:00'),
(15, 3, '2025-06-27 14:00:00'),
(15, 4, '2025-06-28 18:00:00'),
(15, 5, '2025-06-29 10:00:00'),
-- Godzilla vs Kong 2 (ID: 16)
(16, 1, '2025-06-24 18:00:00'),
(16, 2, '2025-06-25 10:00:00'),
(16, 3, '2025-06-26 14:00:00'),
(16, 4, '2025-06-27 18:00:00'),
(16, 5, '2025-06-28 10:00:00'),
(16, 1, '2025-06-29 14:00:00'),
-- Indiana Jones 6 (ID: 17)
(17, 2, '2025-06-24 10:00:00'),
(17, 3, '2025-06-25 14:00:00'),
(17, 4, '2025-06-26 18:00:00'),
(17, 5, '2025-06-27 10:00:00'),
(17, 1, '2025-06-28 14:00:00'),
(17, 2, '2025-06-29 18:00:00'),
-- Mad Max: The Wasteland (ID: 18)
(18, 3, '2025-06-24 14:00:00'),
(18, 4, '2025-06-25 18:00:00'),
(18, 5, '2025-06-26 10:00:00'),
(18, 1, '2025-06-27 14:00:00'),
(18, 2, '2025-06-28 18:00:00'),
(18, 3, '2025-06-29 10:00:00'),
-- The Flash 2 (ID: 19)
(19, 4, '2025-06-24 18:00:00'),
(19, 5, '2025-06-25 10:00:00'),
(19, 1, '2025-06-26 14:00:00'),
(19, 2, '2025-06-27 18:00:00'),
(19, 3, '2025-06-28 10:00:00'),
(19, 4, '2025-06-29 14:00:00');

-- Dữ liệu cho bảng HoaDon
INSERT INTO HoaDon (maNhanVien, maKhachHang, ngayLap) VALUES
(3, 1, '2025-01-15 09:00:00'),  -- 3 vé thường
(4, 2, '2025-01-15 10:00:00'),  -- 2 vé VIP
(6, 5, '2025-01-15 13:00:00'),  -- 2 vé VIP (thay 3 bằng 5)
(8, 1, '2025-01-15 14:00:00'),  -- 1 vé VIP
(10, 2, '2025-01-15 16:00:00'); -- 2 vé thường

INSERT INTO Ve (maSuatChieu, maGhe, maHoaDon, maGiaVe, maKhuyenMai, trangThai, ngayDat) VALUES
(1, 1, 1, 1, 1, 'paid', '2025-01-15 09:00:00'),    -- Áp dụng Khuyến Mãi Tết 2025 cho phim Avatar 3
(1, 2, 1, 1, 1, 'paid', '2025-01-15 09:00:00'),    -- Áp dụng Khuyến Mãi Tết 2025
(1, 3, 1, 1, 1, 'paid', '2025-01-15 09:00:00'),    -- Áp dụng Khuyến Mãi Tết 2025
(1, 11, 2, 2, 1, 'paid', '2025-01-15 10:00:00'),   -- Áp dụng Khuyến Mãi Tết 2025
(1, 12, 2, 2, 1, 'paid', '2025-01-15 10:00:00'),   -- Áp dụng Khuyến Mãi Tết 2025
(1, 21, NULL, 1, 1, 'booked', '2025-01-15 11:00:00'), -- Áp dụng Khuyến Mãi Tết 2025
(1, 22, NULL, 1, 1, 'pending', '2025-01-15 11:00:00'), -- Áp dụng Khuyến Mãi Tết 2025
(2, 181, 3, 2, 1, 'paid', '2025-01-15 13:00:00'),  -- Áp dụng Khuyến Mãi Tết 2025 cho phim Mission: Impossible 8
(2, 182, 3, 2, 1, 'paid', '2025-01-15 13:00:00'),  -- Áp dụng Khuyến Mãi Tết 2025
(2, 191, 4, 2, 1, 'paid', '2025-01-15 14:00:00'),  -- Áp dụng Khuyến Mãi Tết 2025
(2, 192, NULL, 2, 1, 'booked', '2025-01-15 15:00:00'), -- Áp dụng Khuyến Mãi Tết 2025
(2, 201, NULL, 2, 1, 'pending', '2025-01-15 15:00:00'), -- Áp dụng Khuyến Mãi Tết 2025
(3, 317, 5, 1, NULL, 'paid', '2025-01-15 16:00:00'),  -- Không áp dụng khuyến mãi
(3, 318, 5, 1, NULL, 'paid', '2025-01-15 16:00:00'),  -- Không áp dụng khuyến mãi
(3, 327, NULL, 2, NULL, 'booked', '2025-01-15 17:00:00'), -- Không áp dụng khuyến mãi
(3, 328, NULL, 2, NULL, 'cancelled', '2025-01-15 17:00:00'), -- Không áp dụng khuyến mãi
(3, 337, NULL, 1, NULL, 'pending', '2025-01-15 18:00:00'); -- Không áp dụng khuyến mãi

-- Dữ liệu cho bảng ChiTietHoaDon
INSERT INTO ChiTietHoaDon (maHoaDon, maVe) VALUES
(1, 1), (1, 2), (1, 3),  -- Hóa đơn 1: 3 vé
(2, 4), (2, 5),          -- Hóa đơn 2: 2 vé
(3, 8), (3, 9),          -- Hóa đơn 3: 2 vé
(4, 10),                 -- Hóa đơn 4: 1 vé
(5, 12), (5, 13);        -- Hóa đơn 5: 2 vé

-- Dữ liệu cho bảng GiaVe
INSERT INTO GiaVe (loaiGhe, ngayApDung, giaVe, ghiChu) VALUES
('Thuong', '2025-01-01', 110000.00, 'Giá vé thường 2025'),
('VIP', '2025-01-01', 220000.00, 'Giá vé VIP 2025'),
('Thuong', '2025-02-01', 120000.00, 'Tết 2025'),
('VIP', '2025-02-01', 240000.00, 'Tết 2025');

-- Thêm dữ liệu mẫu cho bảng KhuyenMai
INSERT INTO KhuyenMai (tenKhuyenMai, moTa, loaiGiamGia, giaTriGiam, ngayBatDau, ngayKetThuc, trangThai) VALUES
('Khuyến Mãi Tết 2025', 'Giảm giá phim dịp Tết', 'PhanTram', 20.00, '2025-02-01', '2025-02-07', 'HoatDong'),
('Giảm Giá Hè 2025', 'Ưu đãi mùa hè cho phim mới', 'CoDinh', 30000.00, '2025-06-01', '2025-08-31', 'HoatDong'),
('Khuyến Mãi Cuối Năm 2025', 'Giảm giá phim bom tấn', 'PhanTram', 15.00, '2025-12-20', '2025-12-31', 'HoatDong'),
('Black Friday Phim 2025', 'Giảm giá cố định cho phim', 'CoDinh', 20000.00, '2025-11-28', '2025-11-30', 'HoatDong');

-- Thêm dữ liệu mẫu cho bảng DieuKienKhuyenMai
INSERT INTO DieuKienKhuyenMai (maKhuyenMai, loaiDieuKien, maPhim) VALUES
(1, 'Phim', 1),  -- Áp dụng cho Avatar 3
(1, 'Phim', 2),  -- Áp dụng cho Mission: Impossible 8
(2, 'Phim', 6),  -- Áp dụng cho Deadpool 3
(2, 'Phim', 8),  -- Áp dụng cho Spider-Man 4
(3, 'Phim', 10), -- Áp dụng cho Black Panther 3
(4, 'Phim', 5),  -- Áp dụng cho Captain America 4
(4, 'Phim', 7);  -- Áp dụng cho Joker 2         

-- Dữ liệu cho bảng DanhGia
INSERT INTO DanhGia (maPhim, maNguoiDung, maVe, diemDanhGia, nhanXet, ngayDanhGia) VALUES
(1, 1, 1, 5, 'Phim hay, hiệu ứng đẹp', '2025-01-20 20:30:00'), -- Liên kết với vé 1 (khách hàng 1, phim 1)
(1, 2, 4, 4, 'Phim hấp dẫn, diễn viên đóng tốt', '2025-01-20 21:00:00'), -- Liên kết với vé 4 (khách hàng 2, phim 1)
(2, 1, 8, 5, 'Phim hành động xuất sắc', '2025-05-23 19:45:00'), -- Liên kết với vé 8 (khách hàng 1, phim 2)
(2, 5, 9, 3, 'Phim dài hơi', '2025-05-23 22:15:00'), -- Liên kết với vé 9 (khách hàng 5, phim 2)
(3, 2, 13, 4, 'Phim siêu anh hùng đúng chất', '2025-10-03 20:00:00'), -- Liên kết với vé 13 (khách hàng 2, phim 3)
(3, 5, 14, 5, 'Hiệu ứng đặc biệt tuyệt vời', '2025-10-03 21:30:00'); -- Liên kết với vé 14 (khách hàng 5, phim 3)

-- Dữ liệu mẫu cho bảng PhienLamViec
INSERT INTO PhienLamViec (maNhanVien, thoiGianBatDau, thoiGianKetThuc, tongDoanhThu, soVeDaBan) VALUES
(3, '2025-01-15 08:00:00', '2025-01-15 16:00:00', 1500000.00, 5),
(4, '2025-01-15 09:00:00', '2025-01-15 17:00:00', 2200000.00, 8),
(6, '2025-01-15 12:00:00', '2025-01-15 20:00:00', 1800000.00, 6),
(10, '2025-01-15 16:00:00', '2025-01-15 23:59:00', 3000000.00, 12),
(8, '2025-01-16 08:00:00', '2025-01-16 16:00:00', 2500000.00, 10);

-- Thêm một số dữ liệu mẫu
INSERT INTO ActivityLog (loaiHoatDong, moTa, thoiGian, maNguoiDung) VALUES
('Đăng nhập', 'Đăng nhập vào hệ thống', NOW() - INTERVAL 1 HOUR, 9),
('Thêm phim', 'Thêm phim mới: Avatar 3', NOW() - INTERVAL 2 HOUR, 4),
('Sửa phim', 'Cập nhật thông tin phim: The Batman 2', NOW() - INTERVAL 3 HOUR, 4),
('Bán vé', 'Bán vé cho khách hàng: Nguyễn Văn A', NOW() - INTERVAL 4 HOUR, 5),
('Xóa suất chiếu', 'Xóa suất chiếu ngày 25/06/2025', NOW() - INTERVAL 5 HOUR, 4),
('Thêm khuyến mãi', 'Thêm khuyến mãi mới: Giảm giá hè 2025', NOW() - INTERVAL 6 HOUR, 9),
('Đăng xuất', 'Đăng xuất khỏi hệ thống', NOW() - INTERVAL 7 HOUR, 9);

-- View Thống kê nhân viên theo phiên làm việc
DROP VIEW IF EXISTS ThongKeNhanVienTheoPhien;
CREATE VIEW ThongKeNhanVienTheoPhien AS
SELECT 
    plv.maNhanVien,
    nd.hoTen AS tenNhanVien,
    nv.vaiTro,
    plv.thoiGianBatDau,
    plv.thoiGianKetThuc,
    COALESCE(plv.tongDoanhThu, 0) AS tongDoanhThu,
    COALESCE(plv.soVeDaBan, 0) AS soVeDaBan
FROM PhienLamViec plv
JOIN NhanVien nv ON plv.maNhanVien = nv.maNguoiDung
JOIN NguoiDung nd ON nv.maNguoiDung = nd.maNguoiDung;