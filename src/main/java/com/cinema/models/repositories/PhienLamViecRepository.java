package com.cinema.models.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cinema.models.PhienLamViec;
import com.cinema.models.repositories.Interface.IPhienLamViecRepository;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.TransactionManager;

public class PhienLamViecRepository extends BaseRepository<PhienLamViec> implements IPhienLamViecRepository {
    
    private final TransactionManager transactionManager;

    public PhienLamViecRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
        this.transactionManager = new TransactionManager(dbConnection);
    }

    @Override
    public List<PhienLamViec> findAll() throws SQLException {
        String query = "SELECT * FROM PhienLamViec";
        
        return transactionManager.executeTransaction(conn -> {
            List<PhienLamViec> phienLamViecs = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    phienLamViecs.add(extractFromResultSet(rs));
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            return phienLamViecs;
        });
    }

    @Override
    public PhienLamViec findById(int id) throws SQLException {
        String query = "SELECT * FROM PhienLamViec WHERE maPhien = ?";
        
        return transactionManager.executeTransaction(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return extractFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return null;
        });
    }

    @Override
    public PhienLamViec save(PhienLamViec entity) throws SQLException {
        String query = "INSERT INTO PhienLamViec (maNhanVien, thoiGianBatDau, thoiGianKetThuc, tongDoanhThu, soVeDaBan) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        return transactionManager.executeTransaction((Connection conn) -> {
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, entity.getMaNhanVien());
                stmt.setTimestamp(2, Timestamp.valueOf(entity.getThoiGianBatDau()));
                
                if (entity.getThoiGianKetThuc() != null) {
                    stmt.setTimestamp(3, Timestamp.valueOf(entity.getThoiGianKetThuc()));
                } else {
                    stmt.setNull(3, Types.TIMESTAMP);
                }
                
                stmt.setDouble(4, entity.getTongDoanhThu());
                stmt.setInt(5, entity.getSoVeDaBan());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Tạo phiên làm việc thất bại, không có dòng nào được thêm vào.");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setMaPhien(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Tạo phiên làm việc thất bại, không lấy được ID.");
                    }
                }
                
                return entity;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public PhienLamViec update(PhienLamViec entity) throws SQLException {
        String query = "UPDATE PhienLamViec SET maNhanVien = ?, thoiGianBatDau = ?, thoiGianKetThuc = ?, " +
                      "tongDoanhThu = ?, soVeDaBan = ? WHERE maPhien = ?";
        
        return transactionManager.executeTransaction(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, entity.getMaNhanVien());
                stmt.setTimestamp(2, Timestamp.valueOf(entity.getThoiGianBatDau()));
                
                if (entity.getThoiGianKetThuc() != null) {
                    stmt.setTimestamp(3, Timestamp.valueOf(entity.getThoiGianKetThuc()));
                } else {
                    stmt.setNull(3, Types.TIMESTAMP);
                }
                
                stmt.setDouble(4, entity.getTongDoanhThu());
                stmt.setInt(5, entity.getSoVeDaBan());
                stmt.setInt(6, entity.getMaPhien());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Cập nhật phiên làm việc thất bại, không tìm thấy phiên với ID: " + entity.getMaPhien());
                }
                
                return entity;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM PhienLamViec WHERE maPhien = ?";
        
        transactionManager.executeTransactionWithoutResult(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public PhienLamViec findActiveSessionByNhanVien(int maNhanVien) throws SQLException {
        String query = "SELECT * FROM PhienLamViec WHERE maNhanVien = ? AND thoiGianKetThuc IS NULL";
        
        return transactionManager.executeTransaction(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, maNhanVien);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return extractFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return null;
        });
    }

    @Override
    public List<PhienLamViec> findByTimeRange(LocalDateTime tuNgay, LocalDateTime denNgay) throws SQLException {
        String query = "SELECT * FROM PhienLamViec WHERE thoiGianBatDau BETWEEN ? AND ?";
        
        return transactionManager.executeTransaction(conn -> {
            List<PhienLamViec> phienLamViecs = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setTimestamp(1, Timestamp.valueOf(tuNgay));
                stmt.setTimestamp(2, Timestamp.valueOf(denNgay));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        phienLamViecs.add(extractFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return phienLamViecs;
        });
    }

    @Override
    public void updateAfterSale(int maPhien, double giaVe) throws SQLException {
        // Kiểm tra phiên làm việc trước khi cập nhật
        PhienLamViec phienTruocCapNhat = findById(maPhien);
        if (phienTruocCapNhat == null) {
            System.out.println("KIỂM TRA LỖI: Không tìm thấy phiên làm việc với ID: " + maPhien);
            throw new SQLException("Không tìm thấy phiên làm việc với ID: " + maPhien);
        }
        
        System.out.println("KIỂM TRA: Phiên trước khi cập nhật - ID: " + maPhien 
                + ", Doanh thu: " + phienTruocCapNhat.getTongDoanhThu() 
                + ", Số vé: " + phienTruocCapNhat.getSoVeDaBan());
        
        String query = "UPDATE PhienLamViec SET tongDoanhThu = tongDoanhThu + ?, soVeDaBan = soVeDaBan + 1 WHERE maPhien = ?";
        
        transactionManager.executeTransactionWithoutResult(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDouble(1, giaVe);
                stmt.setInt(2, maPhien);
                
                System.out.println("KIỂM TRA: Đang cập nhật phiên " + maPhien + " với giá vé: " + giaVe);
                int affectedRows = stmt.executeUpdate();
                System.out.println("KIỂM TRA: Số dòng được cập nhật: " + affectedRows);
                
                if (affectedRows == 0) {
                    throw new SQLException("Cập nhật phiên làm việc sau khi bán vé thất bại, không tìm thấy phiên với ID: " + maPhien);
                }
                
                // Kiểm tra sau khi cập nhật
                try {
                    PhienLamViec phienSauCapNhat = findById(maPhien);
                    System.out.println("KIỂM TRA: Phiên sau khi cập nhật - ID: " + maPhien 
                            + ", Doanh thu: " + phienSauCapNhat.getTongDoanhThu() 
                            + ", Số vé: " + phienSauCapNhat.getSoVeDaBan());
                } catch (Exception e) {
                    System.out.println("KIỂM TRA: Lỗi khi đọc phiên sau cập nhật: " + e.getMessage());
                }
            } catch (SQLException e) {
                System.out.println("KIỂM TRA LỖI: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        });
    }

    @Override
    public void endSession(int maPhien) throws SQLException {
        String query = "UPDATE PhienLamViec SET thoiGianKetThuc = ? WHERE maPhien = ?";
        
        transactionManager.executeTransactionWithoutResult(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(2, maPhien);
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Kết thúc phiên làm việc thất bại, không tìm thấy phiên với ID: " + maPhien);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
        });
    }

    @Override
    public List<PhienLamViec> getRevenueByNhanVien(int maNhanVien) throws SQLException {
        String query = "SELECT * FROM PhienLamViec WHERE maNhanVien = ? ORDER BY thoiGianBatDau DESC";
        
        return transactionManager.executeTransaction(conn -> {
            List<PhienLamViec> phienLamViecs = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, maNhanVien);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        phienLamViecs.add(extractFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            return phienLamViecs;
        });
    }
    
    private PhienLamViec extractFromResultSet(ResultSet rs) throws SQLException {
        PhienLamViec phienLamViec = new PhienLamViec();
        
        phienLamViec.setMaPhien(rs.getInt("maPhien"));
        phienLamViec.setMaNhanVien(rs.getInt("maNhanVien"));
        phienLamViec.setThoiGianBatDau(rs.getTimestamp("thoiGianBatDau").toLocalDateTime());
        
        Timestamp thoiGianKetThuc = rs.getTimestamp("thoiGianKetThuc");
        if (thoiGianKetThuc != null) {
            phienLamViec.setThoiGianKetThuc(thoiGianKetThuc.toLocalDateTime());
        }
        
        phienLamViec.setTongDoanhThu(rs.getDouble("tongDoanhThu"));
        phienLamViec.setSoVeDaBan(rs.getInt("soVeDaBan"));
        
        return phienLamViec;
    }
}