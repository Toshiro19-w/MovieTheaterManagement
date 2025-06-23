package com.cinema.models.repositories;

import com.cinema.models.UserSession;
import com.cinema.models.repositories.Interface.IUserSessionRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSessionRepository extends BaseRepository<UserSession> implements IUserSessionRepository {

    public UserSessionRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    @Override
    public boolean createSession(UserSession userSession) {
        String sql = "{CALL ManageUserSession(?, ?, ?, ?)}";
        System.out.println("KIỂM TRA: Đang tạo phiên người dùng với SQL: " + sql);
        System.out.println("KIỂM TRA: Thông tin phiên - MaPhien: " + userSession.getMaPhien() + 
                          ", MaNguoiDung: " + userSession.getMaNguoiDung() + 
                          ", ThongTinThietBi: " + userSession.getThongTinThietBi());
        
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, "CREATE");
            stmt.setString(2, userSession.getMaPhien());
            stmt.setInt(3, userSession.getMaNguoiDung());
            stmt.setString(4, userSession.getThongTinThietBi());
            
            System.out.println("KIỂM TRA: Đang thực thi stored procedure ManageUserSession");
            stmt.execute();
            System.out.println("KIỂM TRA: Tạo phiên người dùng thành công");
            
            // Kiểm tra xem phiên đã được tạo chưa
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM UserSession WHERE maPhien = ?")) {
                checkStmt.setString(1, userSession.getMaPhien());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("KIỂM TRA: Số bản ghi tìm thấy sau khi tạo: " + count);
                    }
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.out.println("KIỂM TRA LỖI: Tạo phiên người dùng thất bại - " + e.getMessage());
            e.printStackTrace();
            
            // Kiểm tra xem bảng UserSession có tồn tại không
            try (Connection conn = getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement("SHOW TABLES LIKE 'UserSession'")) {
                ResultSet rs = checkStmt.executeQuery();
                System.out.println("KIỂM TRA: Bảng UserSession " + (rs.next() ? "tồn tại" : "không tồn tại"));
            } catch (SQLException ex) {
                System.out.println("KIỂM TRA LỖI: Không thể kiểm tra bảng - " + ex.getMessage());
            }
            
            return false;
        }
    }

    @Override
    public boolean updateSessionActivity(String maPhien) {
        String sql = "{CALL ManageUserSession(?, ?, ?, ?)}";
        
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, "UPDATE");
            stmt.setString(2, maPhien);
            stmt.setNull(3, java.sql.Types.INTEGER);
            stmt.setNull(4, java.sql.Types.VARCHAR);
            
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean closeSession(String maPhien) {
        String sql = "{CALL ManageUserSession(?, ?, ?, ?)}";
        
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, "CLOSE");
            stmt.setString(2, maPhien);
            stmt.setNull(3, java.sql.Types.INTEGER);
            stmt.setNull(4, java.sql.Types.VARCHAR);
            
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isSessionActive(String maPhien) {
        String sql = "{CALL ManageUserSession(?, ?, ?, ?)}";
        
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, "CHECK");
            stmt.setString(2, maPhien);
            stmt.setNull(3, java.sql.Types.INTEGER);
            stmt.setNull(4, java.sql.Types.VARCHAR);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("sessionExists") > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<UserSession> getActiveSessions() {
        List<UserSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM UserSession WHERE trangThai = 'active'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                sessions.add(mapResultSetToUserSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return sessions;
    }

    @Override
    public List<UserSession> getSessionsByUser(int maNguoiDung) {
        List<UserSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM UserSession WHERE maNguoiDung = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maNguoiDung);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToUserSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return sessions;
    }

    @Override
    public int closeInactiveSessions(int minutes) {
        String sql = "UPDATE UserSession SET trangThai = 'inactive' " +
                     "WHERE trangThai = 'active' AND " +
                     "TIMESTAMPDIFF(MINUTE, thoiGianHoatDongCuoi, NOW()) > ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, minutes);
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public UserSession findById(int id) throws SQLException {
        // Không cần thiết vì UserSession sử dụng String làm khóa chính
        return null;
    }
    
    public UserSession findByMaPhien(String maPhien) {
        String sql = "SELECT * FROM UserSession WHERE maPhien = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, maPhien);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserSession(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public List<UserSession> findAll() {
        List<UserSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM UserSession";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                sessions.add(mapResultSetToUserSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return sessions;
    }

    public boolean add(UserSession userSession) {
        if (userSession.getMaPhien() == null || userSession.getMaPhien().isEmpty()) {
            userSession.setMaPhien(UUID.randomUUID().toString());
        }
        return createSession(userSession);
    }

    @Override
    public UserSession update(UserSession userSession) throws SQLException {
        String sql = "UPDATE UserSession SET maNguoiDung = ?, thoiGianBatDau = ?, " +
                     "thoiGianHoatDongCuoi = ?, trangThai = ?, thongTinThietBi = ? " +
                     "WHERE maPhien = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userSession.getMaNguoiDung());
            stmt.setTimestamp(2, Timestamp.valueOf(userSession.getThoiGianBatDau()));
            stmt.setTimestamp(3, Timestamp.valueOf(userSession.getThoiGianHoatDongCuoi()));
            stmt.setString(4, userSession.getTrangThai());
            stmt.setString(5, userSession.getThongTinThietBi());
            stmt.setString(6, userSession.getMaPhien());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return userSession;
            }
            return null;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        // Không cần thiết vì UserSession sử dụng String làm khóa chính
    }
    
    public boolean deleteByMaPhien(String maPhien) {
        String sql = "DELETE FROM UserSession WHERE maPhien = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, maPhien);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private UserSession mapResultSetToUserSession(ResultSet rs) throws SQLException {
        UserSession userSession = new UserSession();
        userSession.setMaPhien(rs.getString("maPhien"));
        userSession.setMaNguoiDung(rs.getInt("maNguoiDung"));
        userSession.setThoiGianBatDau(rs.getTimestamp("thoiGianBatDau").toLocalDateTime());
        userSession.setThoiGianHoatDongCuoi(rs.getTimestamp("thoiGianHoatDongCuoi").toLocalDateTime());
        userSession.setTrangThai(rs.getString("trangThai"));
        userSession.setThongTinThietBi(rs.getString("thongTinThietBi"));
        return userSession;
    }

    @Override
    public UserSession save(UserSession entity) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }
}