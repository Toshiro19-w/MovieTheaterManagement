package com.cinema.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import com.cinema.models.GiaVe;
import com.cinema.models.repositories.GiaVeRepository;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.FormatUtils;
import com.cinema.views.admin.GiaVeDialog;

public class GiaVeController {
    private GiaVeDialog view;
    private DatabaseConnection dbConnection;
    private GiaVeRepository giaVeRepository;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public GiaVeController(GiaVeDialog view) throws SQLException, IOException {
        this.view = view;
        this.dbConnection = new DatabaseConnection();
        this.giaVeRepository = new GiaVeRepository(dbConnection);
    }
    
    public void loadGiaVeData() throws SQLException {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        
        List<GiaVe> giaVeList = giaVeRepository.findAll();
        
        for (GiaVe giaVe : giaVeList) {
            Vector<Object> row = new Vector<>();
            row.add(giaVe.getMaGiaVe());
            row.add(giaVe.getLoaiGhe());
            row.add(FormatUtils.formatCurrency(giaVe.getGiaVe()));
            row.add(dateFormat.format(java.sql.Date.valueOf(giaVe.getNgayApDung())));
            
            // Hiển thị ngày kết thúc nếu có
            if (giaVe.getNgayKetThuc() != null) {
                row.add(dateFormat.format(java.sql.Date.valueOf(giaVe.getNgayKetThuc())));
            } else {
                row.add("Không giới hạn");
            }
            
            row.add(giaVe.getGhiChu());
            model.addRow(row);
        }
    }
    
    public void addGiaVe(GiaVe giaVe) throws SQLException {
        try {
            giaVeRepository.save(giaVe);
            JOptionPane.showMessageDialog(view, "Thêm giá vé thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadGiaVeData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi thêm giá vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
    
    public void updateGiaVe(GiaVe giaVe) throws SQLException {
        try {
            giaVeRepository.update(giaVe);
            JOptionPane.showMessageDialog(view, "Cập nhật giá vé thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadGiaVeData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi cập nhật giá vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
    
    public void deleteGiaVe(int maGiaVe) throws SQLException {
        // Kiểm tra xem giá vé có đang được sử dụng không
        String checkQuery = "SELECT COUNT(*) FROM Ve WHERE maGiaVe = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            
            checkStmt.setInt(1, maGiaVe);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(view, 
                        "Không thể xóa giá vé này vì đang được sử dụng bởi các vé!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        
        // Thực hiện xóa nếu không có vé nào sử dụng
        try {
            giaVeRepository.delete(maGiaVe);
            JOptionPane.showMessageDialog(view, "Xóa giá vé thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadGiaVeData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi xóa giá vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
    
    public GiaVe findActiveGiaVe(String loaiGhe, LocalDate date) throws SQLException {
        return giaVeRepository.findActiveByLoaiGheAndDate(loaiGhe, date);
    }
}