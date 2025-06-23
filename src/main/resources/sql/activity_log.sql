-- Tạo bảng ActivityLog để lưu trữ hoạt động của người dùng
CREATE TABLE IF NOT EXISTS ActivityLog (
    maLog INT AUTO_INCREMENT PRIMARY KEY,
    loaiHoatDong NVARCHAR(50) NOT NULL,
    moTa NVARCHAR(255) NOT NULL,
    thoiGian DATETIME DEFAULT NOW(),
    maNguoiDung INT NOT NULL,
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo index để tối ưu truy vấn
CREATE INDEX idx_activitylog_thoigian ON ActivityLog(thoiGian);
CREATE INDEX idx_activitylog_manguoidung ON ActivityLog(maNguoiDung);

-- Thêm một số dữ liệu mẫu
INSERT INTO ActivityLog (loaiHoatDong, moTa, thoiGian, maNguoiDung) VALUES
('Đăng nhập', 'Đăng nhập vào hệ thống', NOW() - INTERVAL 1 HOUR, 9),
('Thêm phim', 'Thêm phim mới: Avatar 3', NOW() - INTERVAL 2 HOUR, 4),
('Sửa phim', 'Cập nhật thông tin phim: The Batman 2', NOW() - INTERVAL 3 HOUR, 4),
('Bán vé', 'Bán vé cho khách hàng: Nguyễn Văn A', NOW() - INTERVAL 4 HOUR, 5),
('Xóa suất chiếu', 'Xóa suất chiếu ngày 25/06/2025', NOW() - INTERVAL 5 HOUR, 4),
('Thêm khuyến mãi', 'Thêm khuyến mãi mới: Giảm giá hè 2025', NOW() - INTERVAL 6 HOUR, 9),
('Đăng xuất', 'Đăng xuất khỏi hệ thống', NOW() - INTERVAL 7 HOUR, 9);

-- Tạo stored procedure để thêm log
DELIMITER //
CREATE PROCEDURE AddActivityLog(
    IN p_loaiHoatDong NVARCHAR(50),
    IN p_moTa NVARCHAR(255),
    IN p_maNguoiDung INT
)
BEGIN
    INSERT INTO ActivityLog (loaiHoatDong, moTa, thoiGian, maNguoiDung)
    VALUES (p_loaiHoatDong, p_moTa, NOW(), p_maNguoiDung);
END //
DELIMITER ;

-- Tạo stored procedure để lấy log gần đây
DELIMITER //
CREATE PROCEDURE GetRecentLogs(
    IN p_limit INT
)
BEGIN
    SELECT l.*, nd.hoTen 
    FROM ActivityLog l
    JOIN NguoiDung nd ON l.maNguoiDung = nd.maNguoiDung
    ORDER BY l.thoiGian DESC
    LIMIT p_limit;
END //
DELIMITER ;