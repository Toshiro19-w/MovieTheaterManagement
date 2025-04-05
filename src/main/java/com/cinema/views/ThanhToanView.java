package com.cinema.views;

import com.cinema.controllers.HoaDonController;
import com.cinema.models.HoaDon;
import com.cinema.models.Ve;
import com.cinema.services.HoaDonService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ThanhToanView extends JPanel {
    private HoaDonController hoaDonController;
    private JTextField maKhachHangField;
    private JTextField maNhanVienField;
    private JTextArea veListArea;
    private JButton thanhToanButton;
    private JButton xemLichSuButton;

    public ThanhToanView() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        try {
            DatabaseConnection dbConnection = new DatabaseConnection();
            hoaDonController = new HoaDonController(new HoaDonService(dbConnection), new VeService(dbConnection));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình!");
        }

        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form nhập thông tin
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.add(new JLabel("Mã khách hàng:"));
        maKhachHangField = new JTextField();
        formPanel.add(maKhachHangField);
        formPanel.add(new JLabel("Mã nhân viên:"));
        maNhanVienField = new JTextField();
        formPanel.add(maNhanVienField);
        formPanel.add(new JLabel("Danh sách mã vé (cách nhau bằng dấu phẩy):"));
        veListArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(veListArea));
        this.add(formPanel, BorderLayout.CENTER);

        // Nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout());
        thanhToanButton = new JButton("Thanh toán");
        thanhToanButton.addActionListener(e -> thanhToan());
        buttonPanel.add(thanhToanButton);
        xemLichSuButton = new JButton("Xem lịch sử hóa đơn");
        xemLichSuButton.addActionListener(e -> xemLichSu());
        buttonPanel.add(xemLichSuButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void thanhToan() {
        try {
            Integer maKhachHang = maKhachHangField.getText().isEmpty() ? null : Integer.parseInt(maKhachHangField.getText());
            Integer maNhanVien = maNhanVienField.getText().isEmpty() ? null : Integer.parseInt(maNhanVienField.getText());
            List<Integer> maVeList = new ArrayList<>();
            String[] veArray = veListArea.getText().split(",");
            for (String maVe : veArray) {
                maVeList.add(Integer.parseInt(maVe.trim()));
            }

            int maHoaDon = hoaDonController.thanhToanHoaDon(maNhanVien, maKhachHang, maVeList);
            JOptionPane.showMessageDialog(this, "Thanh toán thành công! Mã hóa đơn: " + maHoaDon);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thanh toán!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xemLichSu() {
        try {
            Integer maKhachHang = maKhachHangField.getText().isEmpty() ? null : Integer.parseInt(maKhachHangField.getText());
            if (maKhachHang == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mã khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<HoaDon> hoaDonList = hoaDonController.getLichSuHoaDon(maKhachHang);
            StringBuilder lichSu = new StringBuilder("Lịch sử hóa đơn:\n");
            for (HoaDon hd : hoaDonList) {
                lichSu.append("Mã hóa đơn: ").append(hd.getMaHoaDon())
                        .append(", Ngày lập: ").append(hd.getNgayLap())
                        .append(", Tổng tiền: ").append(hd.getTongTien()).append("\n");
                List<Ve> veList = hoaDonController.getVeByHoaDon(hd.getMaHoaDon());
                lichSu.append("Vé:\n");
                for (Ve ve : veList) {
                    lichSu.append("- Mã vé: ").append(ve.getMaVe())
                            .append(", Ghế: ").append(ve.getSoGhe())
                            .append(", Giá: ").append(ve.getGiaVe()).append("\n");
                }
            }
            JOptionPane.showMessageDialog(this, lichSu.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã khách hàng hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
