-- Tạo bảng UserSession để theo dõi phiên làm việc của người dùng
DROP TABLE IF EXISTS UserSession;
CREATE TABLE UserSession (
    maPhien VARCHAR(50) PRIMARY KEY,
    maNguoiDung INT NOT NULL,
    thoiGianBatDau DATETIME DEFAULT NOW(),
    thoiGianHoatDongCuoi DATETIME DEFAULT NOW(),
    trangThai ENUM('active', 'inactive') DEFAULT 'active',
    thongTinThietBi VARCHAR(255),
    FOREIGN KEY (maNguoiDung) REFERENCES NguoiDung(maNguoiDung) ON DELETE CASCADE
);

-- Tạo stored procedure để quản lý phiên làm việc
DELIMITER //
CREATE PROCEDURE ManageUserSession(
    IN p_action VARCHAR(20),
    IN p_maPhien VARCHAR(50),
    IN p_maNguoiDung INT,
    IN p_thongTinThietBi VARCHAR(255)
)
BEGIN
    DECLARE v_count INT;
    
    -- Ghi log hoạt động
    INSERT INTO ActivityLog (action, details, timestamp)
    VALUES (CONCAT('UserSession_', p_action), 
            CONCAT('maPhien=', IFNULL(p_maPhien, 'NULL'), 
                  ', maNguoiDung=', IFNULL(p_maNguoiDung, 'NULL')), 
            NOW());
    
    IF p_action = 'CREATE' THEN
        -- Tạo phiên mới
        INSERT INTO UserSession (maPhien, maNguoiDung, thoiGianBatDau, thoiGianHoatDongCuoi, thongTinThietBi)
        VALUES (p_maPhien, p_maNguoiDung, NOW(), NOW(), p_thongTinThietBi);
        
        -- Ghi log kết quả
        SET v_count = ROW_COUNT();
        INSERT INTO ActivityLog (action, details, timestamp)
        VALUES ('UserSession_CREATE_RESULT', CONCAT('Rows affected: ', v_count), NOW());
        
    ELSEIF p_action = 'UPDATE' THEN
        -- Cập nhật thời gian hoạt động cuối
        UPDATE UserSession 
        SET thoiGianHoatDongCuoi = NOW() 
        WHERE maPhien = p_maPhien AND trangThai = 'active';
        
    ELSEIF p_action = 'CLOSE' THEN
        -- Đóng phiên
        UPDATE UserSession 
        SET trangThai = 'inactive' 
        WHERE maPhien = p_maPhien;
        
    ELSEIF p_action = 'CHECK' THEN
        -- Kiểm tra phiên có tồn tại và đang hoạt động
        SELECT COUNT(*) INTO v_count 
        FROM UserSession 
        WHERE maPhien = p_maPhien AND trangThai = 'active';
        
        SELECT v_count AS sessionExists;
    END IF;
END //
DELIMITER ;

-- Tạo bảng ActivityLog nếu chưa tồn tại
CREATE TABLE IF NOT EXISTS ActivityLog (
    id INT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    timestamp DATETIME DEFAULT NOW()
);

-- Thêm truy vấn để kiểm tra bảng UserSession
SELECT 'Kiểm tra bảng UserSession' AS message;
SHOW CREATE TABLE UserSession;
SELECT COUNT(*) AS total_sessions FROM UserSession;

-- Tạo chỉ mục cho bảng UserSession
CREATE INDEX idx_usersession_manguoidung ON UserSession(maNguoiDung);
CREATE INDEX idx_usersession_trangthai ON UserSession(trangThai);

-- Tạo event để tự động đóng các phiên không hoạt động
DELIMITER //
CREATE EVENT IF NOT EXISTS close_inactive_sessions
ON SCHEDULE EVERY 1 HOUR
DO
BEGIN
    UPDATE UserSession
    SET trangThai = 'inactive'
    WHERE trangThai = 'active'
    AND TIMESTAMPDIFF(MINUTE, thoiGianHoatDongCuoi, NOW()) > 30;
END //
DELIMITER ;

-- Thêm cột phiên bản cho các bảng quan trọng để hỗ trợ khóa lạc quan (optimistic locking)
ALTER TABLE Ve ADD COLUMN IF NOT EXISTS phienBan INT DEFAULT 1;
ALTER TABLE HoaDon ADD COLUMN IF NOT EXISTS phienBan INT DEFAULT 1;
ALTER TABLE SuatChieu ADD COLUMN IF NOT EXISTS phienBan INT DEFAULT 1;
ALTER TABLE Ghe ADD COLUMN IF NOT EXISTS phienBan INT DEFAULT 1;

select * from usersession;