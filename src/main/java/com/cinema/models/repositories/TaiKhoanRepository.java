package com.cinema.models.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import com.cinema.models.TaiKhoan;
import com.cinema.models.repositories.Interface.ITaiKhoanRepository;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.PasswordHasher;

public class TaiKhoanRepository implements ITaiKhoanRepository {
    private static final Logger LOGGER = Logger.getLogger(TaiKhoanRepository.class.getName());
    private final DatabaseConnection dbConnection;

    public TaiKhoanRepository(DatabaseConnection dbConnection) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection cannot be null");
        }
        this.dbConnection = dbConnection;
    }

    @Override
    public void createTaiKhoan(TaiKhoan taiKhoan) throws SQLException {
        String sql = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, taiKhoan.getTenDangNhap());
            pstmt.setString(2, taiKhoan.getMatKhau());
            pstmt.setString(3, taiKhoan.getLoaiTaiKhoan());
            pstmt.setInt(4, taiKhoan.getMaNguoiDung());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi tạo tài khoản: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean existsByTenDangNhap(String tenDangNhap) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenDangNhap);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi kiểm tra tên đăng nhập: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean existsByMaNguoiDung(Integer maNguoiDung) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE maNguoiDung = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maNguoiDung);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi kiểm tra mã người dùng: " + e.getMessage());
            throw e;
        }
    }

    public boolean verifyUser(String username, String email, String phone) throws SQLException {
        String sql = "SELECT t.tenDangNhap FROM TaiKhoan t " +
                "JOIN NguoiDung n ON t.maNguoiDung = n.maNguoiDung " +
                "WHERE t.tenDangNhap = ? AND n.email = ? AND n.soDienThoai = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi xác minh người dùng: " + e.getMessage());
            throw e;
        }
    }

    public boolean updatePassword(String username, String hashedPassword) throws SQLException {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi cập nhật mật khẩu: " + e.getMessage());
            throw e;
        }
    }

    public int registerUser(String username, String fullName, String phone, String email, String password) throws SQLException {
        // Kiểm tra tham số đầu vào
        if (username == null || username.trim().isEmpty() ||
                fullName == null || fullName.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Thông tin người dùng không được để trống");
        }

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            // Kiểm tra username, email, và số điện thoại đã tồn tại
            String checkSQL = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ? " +
                    "UNION SELECT COUNT(*) FROM NguoiDung WHERE email = ? " +
                    "UNION SELECT COUNT(*) FROM NguoiDung WHERE soDienThoai = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSQL)) {
                checkStmt.setString(1, username);
                checkStmt.setString(2, email);
                checkStmt.setString(3, phone);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getInt(1) > 0) {
                            throw new SQLException ("tên đăng nhập, số điện thoại hoặc email đã được sử dụng !!!");  // Trùng lặp
                        }
                }
            }

            // Thêm vào bảng NguoiDung
            String insertNguoiDung = "INSERT INTO NguoiDung (hoTen, soDienThoai, email, loaiNguoiDung) VALUES (?, ?, ?, ?)";
            int maNguoiDung;
            try (PreparedStatement nguoiDungStmt = connection.prepareStatement(insertNguoiDung, PreparedStatement.RETURN_GENERATED_KEYS)) {
                nguoiDungStmt.setString(1, fullName);
                nguoiDungStmt.setString(2, phone);
                nguoiDungStmt.setString(3, email);
                nguoiDungStmt.setString(4, "KhachHang");
                nguoiDungStmt.executeUpdate();

                try (ResultSet generatedKeys = nguoiDungStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        maNguoiDung = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Không thể lấy mã người dùng!");
                    }
                }
            }

            // Thêm vào bảng TaiKhoan với mật khẩu đã mã hoá
            String insertTaiKhoan = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, loaiTaiKhoan, maNguoiDung) VALUES (?, ?, ?, ?)";
            try (PreparedStatement taiKhoanStmt = connection.prepareStatement(insertTaiKhoan)) {
                taiKhoanStmt.setString(1, username);
                String hashedPassword = PasswordHasher.hashPassword(password);
                taiKhoanStmt.setString(2, hashedPassword);
                taiKhoanStmt.setString(3, "User");
                taiKhoanStmt.setInt(4, maNguoiDung);
                taiKhoanStmt.executeUpdate();
            }

            // Thêm vào bảng KhachHang
            String insertKhachHang = "INSERT INTO KhachHang (maNguoiDung, diemTichLuy) VALUES (?, ?)";
            try (PreparedStatement khachHangStmt = connection.prepareStatement(insertKhachHang)) {
                khachHangStmt.setInt(1, maNguoiDung);
                khachHangStmt.setInt(2, 0);
                khachHangStmt.executeUpdate();
            }

            connection.commit();
            return maNguoiDung;

        } catch (SQLException ex) {
            LOGGER.severe("Lỗi đăng ký người dùng: " + ex.getMessage());
            throw ex;
        }
        }
    }

    public int getUserIdFromUsername(String username) {
        String sql = "SELECT nd.maNguoiDung FROM NguoiDung nd " +
                     "JOIN TaiKhoan tk ON nd.maNguoiDung = tk.maNguoiDung " +
                     "WHERE tk.tenDangNhap = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maNguoiDung");
                }
            }
        } catch (SQLException ex) {
            LOGGER.severe("Lỗi khi lấy mã người dùng: " + ex.getMessage());
        }
        return -1;
    }

    public TaiKhoan findByUsername(String username) throws SQLException {
        String sql = "SELECT t.*, n.hoTen, n.email, n.soDienThoai FROM TaiKhoan t " +
                "JOIN NguoiDung n ON t.maNguoiDung = n.maNguoiDung " +
                "WHERE t.tenDangNhap = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan taiKhoan = new TaiKhoan();
                    taiKhoan.setTenDangNhap(rs.getString("tenDangNhap"));
                    taiKhoan.setMatKhau(rs.getString("matKhau"));
                    taiKhoan.setLoaiTaiKhoan(rs.getString("loaiTaiKhoan"));
                    taiKhoan.setMaNguoiDung(rs.getInt("maNguoiDung"));
                    return taiKhoan;
                }
                return null;
            }
        }
    }

    public String findUsernameByEmailOrPhone(String emailOrPhone) throws SQLException {
        String query = "SELECT tk.tenDangNhap FROM TaiKhoan tk " +
                      "JOIN NguoiDung nd ON tk.maNguoiDung = nd.maNguoiDung " +
                      "WHERE nd.email = ? OR nd.soDienThoai = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, emailOrPhone);
            stmt.setString(2, emailOrPhone);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tenDangNhap");
                }
            }
        }

        return null;
    }

    public String getEmailByUsername(String username) throws SQLException {
        String query = "SELECT nd.email FROM TaiKhoan tk " +
                        "JOIN NguoiDung nd ON tk.maNguoiDung = nd.maNguoiDung " +
                        "WHERE tk.tenDangNhap = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }

        return null;
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        TaiKhoan taiKhoan = findByUsername(username);
        if (taiKhoan != null) {
            return BCrypt.checkpw(password, taiKhoan.getMatKhau());
        }
        return false;
    }
}