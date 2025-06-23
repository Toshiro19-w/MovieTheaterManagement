package com.cinema.views.admin;

import com.cinema.components.*;
import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MovieDialog extends JDialog {
    private final PhimController controller;
    private final Phim editingPhim;
    private final Runnable onSavedCb;

    private UnderlineTextField txtTenPhim, txtThoiLuong, txtDaoDien, txtMoTa;
    private CountryComboBox cbNuocSanXuat;
    private MultiSelectComboBox cbTenTheLoai;
    private JComboBox<String> cbKieuPhim;
    private JLabel lblPoster;
    private JButton btnChonAnh, btnLuu, btnHuy, btnXoa;
    private String selectedPosterPath = null;
    private DatePicker datePicker;

    public MovieDialog(Window parent, PhimController controller, Phim phim, Runnable onSavedCb) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.editingPhim = phim;
        this.onSavedCb = onSavedCb;

        setTitle(phim == null ? "Thêm phim mới" : "Chi tiết/Sửa phim");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(540, 660);
        setMinimumSize(new Dimension(480, 520));
        setResizable(true);
        buildUI();

        if (editingPhim != null) fillPhimToForm(editingPhim);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        txtTenPhim = new UnderlineTextField(20);
        mainPanel.add(labelAnd(txtTenPhim, "Tên phim"));

        cbTenTheLoai = new MultiSelectComboBox();
        try {
            Map<Integer, String> genres = controller.getAllTheLoaiMap();
            cbTenTheLoai.removeAllItems();
            for (Map.Entry<Integer, String> e : genres.entrySet())
                cbTenTheLoai.addItem(e.getKey(), e.getValue(), false);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải thể loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        mainPanel.add(labelAnd(cbTenTheLoai, "Thể loại"));

        txtThoiLuong = new UnderlineTextField(10);
        mainPanel.add(labelAnd(txtThoiLuong, "Thời lượng (phút)"));

        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("dd/MM/yyyy");
        datePicker = new DatePicker(dateSettings);
        mainPanel.add(labelAnd(datePicker, "Ngày khởi chiếu"));

        txtDaoDien = new UnderlineTextField(20);
        mainPanel.add(labelAnd(txtDaoDien, "Đạo diễn"));

        cbNuocSanXuat = new CountryComboBox();
        cbNuocSanXuat.setEditable(true);
        mainPanel.add(labelAnd(cbNuocSanXuat, "Nước sản xuất"));

        cbKieuPhim = new JComboBox<>();
        try {
            List<String> dinhDang = controller.getAllDinhDang();
            for (String s : dinhDang) cbKieuPhim.addItem(s);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải kiểu phim: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        mainPanel.add(labelAnd(cbKieuPhim, "Định dạng"));

        txtMoTa = new UnderlineTextField(30);
        mainPanel.add(labelAnd(txtMoTa, "Mô tả"));

        lblPoster = new JLabel("Không có ảnh", SwingConstants.CENTER);
        lblPoster.setPreferredSize(new Dimension(100, 150));
        btnChonAnh = new JButton("Chọn ảnh");
        JPanel posterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        posterPanel.add(lblPoster);
        posterPanel.add(btnChonAnh);
        mainPanel.add(labelAnd(posterPanel, "Poster"));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        btnLuu = new JButton(editingPhim == null ? "Thêm" : "Lưu");
        btnHuy = new JButton("Đóng");
        btnPanel.add(btnLuu);
        btnPanel.add(btnHuy);
        if (editingPhim != null) {
            btnXoa = new JButton("Xóa");
            btnPanel.add(btnXoa);
        }
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(btnPanel);
        setContentPane(mainPanel);

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> onSave());
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> onSave());
        if (btnXoa != null) btnXoa.addActionListener(e -> onDelete());
        btnChonAnh.addActionListener(e -> onChooseImage());
    }

    private JPanel labelAnd(Component comp, String label) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(120, 32));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return p;
    }

    private void fillPhimToForm(Phim phim) {
        txtTenPhim.setText(phim.getTenPhim());
        txtThoiLuong.setText(String.valueOf(phim.getThoiLuong()));
        if (phim.getNgayKhoiChieu() != null) {
            datePicker.setDate(phim.getNgayKhoiChieu());
        }
        txtDaoDien.setText(phim.getDaoDien());
        cbNuocSanXuat.setSelectedItem(phim.getNuocSanXuat());
        cbKieuPhim.setSelectedItem(phim.getKieuPhim());
        txtMoTa.setText(phim.getMoTa());

        // Đảm bảo maTheLoaiList không null
        List<Integer> theLoaiList = phim.getMaTheLoaiList();
        if (theLoaiList == null) theLoaiList = java.util.Collections.emptyList();
        // Chỉ setSelectedIds nếu combo đã có item
        if (cbTenTheLoai.getItemCount() > 0) {
            cbTenTheLoai.setSelectedIds(theLoaiList);
        }

        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            String path = "src/main/resources/images/posters/" + phim.getDuongDanPoster();
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH));
                lblPoster.setIcon(icon);
                lblPoster.setText("");
                selectedPosterPath = f.getAbsolutePath();
            }
        }
    }
      
    private void onSave() {
        try {
            Phim phim = editingPhim == null ? new Phim() : editingPhim;
            phim.setTenPhim(txtTenPhim.getText().trim());
            phim.setThoiLuong(Integer.parseInt(txtThoiLuong.getText().trim()));
            if (datePicker.getDate() != null) {
                phim.setNgayKhoiChieu(datePicker.getDate()); // LocalDate
            }
            phim.setDaoDien(txtDaoDien.getText().trim());
            phim.setNuocSanXuat(cbNuocSanXuat.getText().trim());
            phim.setKieuPhim((String) cbKieuPhim.getSelectedItem());
            phim.setMoTa(txtMoTa.getText().trim());
            phim.setMaTheLoaiList(cbTenTheLoai.getSelectedIds().stream().map(id -> (Integer) id).toList());
            phim.setTenTheLoai(cbTenTheLoai.getSelectedItemsText());

            if (editingPhim == null) {
                controller.themPhim(phim, selectedPosterPath);
                JOptionPane.showMessageDialog(this, "Đã thêm phim!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                controller.suaPhim(phim, selectedPosterPath);
                JOptionPane.showMessageDialog(this, "Đã cập nhật phim!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
            if (onSavedCb != null) onSavedCb.run(); // chỉ callback thôi!
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onDelete() {
        if (editingPhim == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa phim này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.xoaPhim(editingPhim.getMaPhim());
                JOptionPane.showMessageDialog(this, "Đã xóa phim!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                if (onSavedCb != null) onSavedCb.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onChooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn poster phim");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPosterPath = fileChooser.getSelectedFile().getAbsolutePath();
            ImageIcon img = new ImageIcon(selectedPosterPath);
            lblPoster.setIcon(new ImageIcon(img.getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH)));
            lblPoster.setText("");
        }
//        btnChonAnh.addActionListener(e -> onChooseImage());
}
}