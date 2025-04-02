package com.cinema.views;

import com.cinema.controllers.VeController;
import com.cinema.models.TrangThaiVe;
import com.cinema.models.Ve;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class VeView extends JFrame {
    private DefaultTableModel tableModel;
    private VeController veController;
    private JTextField searchField;

    public VeView() {
        veController = new VeController();
        setTitle("Quản lý Vé");
        setSize(1000, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Bảng hiển thị danh sách vé
        tableModel = new DefaultTableModel(new String[]{
                "Mã Vé", "Mã Suất Chiếu", "Mã Khách Hàng", "Mã Hóa Đơn",
                "Số Ghế", "Giá Vé", "Trạng Thái", "Ngày Đặt"}, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Panel chứa nút và ô nhập tìm kiếm
        JPanel controlPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        JButton btnLoad = new JButton("Tải Danh Sách Vé");
        JButton btnSearch = new JButton("Tìm Kiếm");
        JButton btnAdd = new JButton("Thêm Vé");
        JButton btnUpdate = new JButton("Cập Nhật");
        JButton btnDelete = new JButton("Xóa Vé");

        btnLoad.addActionListener(e -> loadTickets());
        btnSearch.addActionListener(e -> searchTickets());
        btnAdd.addActionListener(e -> addTicket());
        btnUpdate.addActionListener(e -> updateTicket());
        btnDelete.addActionListener(e -> deleteTicket());

        controlPanel.add(btnLoad);
        controlPanel.add(new JLabel("Tìm kiếm (Mã vé hoặc Số ghế):"));
        controlPanel.add(searchField);
        controlPanel.add(btnSearch);
        controlPanel.add(btnAdd);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);

        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadTickets() {
        tableModel.setRowCount(0);
        List<Ve> veList = veController.hienThiTatCaVe();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Ve ve : veList) {
            tableModel.addRow(new Object[]{
                    ve.getMaVe(),
                    ve.getMaSuatChieu(),
                    ve.getMaKhachHang(),
                    ve.getMaHoaDon(),
                    ve.getSoGhe(),
                    String.format("%,.2f VND", ve.getGiaVe()),
                    ve.getTrangThai(),
                    ve.getNgayDat() != null ? ve.getNgayDat().format(formatter) : "Chưa đặt"
            });
        }
    }

    private void searchTickets() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }

        tableModel.setRowCount(0);
        List<Ve> veList = veController.hienThiTatCaVe();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        veList.stream()
                .filter(ve -> String.valueOf(ve.getMaVe()).contains(keyword) ||
                        ve.getSoGhe().toLowerCase().contains(keyword.toLowerCase()))
                .forEach(ve -> tableModel.addRow(new Object[]{
                        ve.getMaVe(),
                        ve.getMaSuatChieu(),
                        ve.getMaKhachHang() != null ? ve.getMaKhachHang() : "Chưa đặt",
                        ve.getMaHoaDon() != null ? ve.getMaHoaDon() : "Chưa thanh toán",
                        ve.getSoGhe(),
                        String.format("%,.2f VND", ve.getGiaVe()),
                        ve.getTrangThai(),
                        ve.getNgayDat() != null ? ve.getNgayDat().format(dateFormatter) : "Chưa đặt"
                }));
    }

    private void addTicket() {
        // Tạo dialog/form nhập thông tin vé
        JDialog dialog = new JDialog(this, "Thêm vé mới", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        // Panel chứa các trường nhập liệu
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));

        // Các trường nhập liệu
        JTextField txtMaSuatChieu = new JTextField();
        JTextField txtMaKhachHang = new JTextField();
        JTextField txtSoGhe = new JTextField();
        JTextField txtGiaVe = new JTextField();
        JComboBox<TrangThaiVe> cbTrangThai = new JComboBox<>(TrangThaiVe.values());
        cbTrangThai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TrangThaiVe) {
                    TrangThaiVe status = (TrangThaiVe) value;
                    setText(status.getValue());
                }
                return this;
            }
        });

        inputPanel.add(new JLabel("Mã Suất Chiếu:"));
        inputPanel.add(txtMaSuatChieu);
        inputPanel.add(new JLabel("Mã Khách Hàng (nếu có):"));
        inputPanel.add(txtMaKhachHang);
        inputPanel.add(new JLabel("Số Ghế:"));
        inputPanel.add(txtSoGhe);
        inputPanel.add(new JLabel("Giá Vé:"));
        inputPanel.add(txtGiaVe);
        inputPanel.add(new JLabel("Trạng Thái:"));
        inputPanel.add(cbTrangThai);

        // Panel chứa nút xác nhận/hủy
        JPanel buttonPanel = new JPanel();
        JButton btnConfirm = new JButton("Xác nhận");
        JButton btnCancel = new JButton("Hủy");

        btnConfirm.addActionListener(e -> {
            try {
                // Lấy dữ liệu từ form
                int maSuatChieu = Integer.parseInt(txtMaSuatChieu.getText());
                Integer maKhachHang = txtMaKhachHang.getText().isEmpty() ? null : Integer.parseInt(txtMaKhachHang.getText());
                String soGhe = txtSoGhe.getText();
                double giaVe = Double.parseDouble(txtGiaVe.getText());
                TrangThaiVe selectedStatus = (TrangThaiVe) cbTrangThai.getSelectedItem();

                // Tạo đối tượng vé mới
                Ve newVe = new Ve();
                newVe.setMaSuatChieu(maSuatChieu);
                newVe.setMaKhachHang(maKhachHang);
                newVe.setSoGhe(soGhe);
                newVe.setGiaVe(giaVe);
                newVe.setTrangThai(selectedStatus);
                newVe.setNgayDat(LocalDate.now()); // Ngày đặt là thời điểm hiện tại

                // Gọi controller để thêm vé
                Ve addedVe = veController.save(newVe);

                if (addedVe != null) {
                    JOptionPane.showMessageDialog(dialog, "Thêm vé thành công!");
                    dialog.dispose();
                    loadTickets(); // Tải lại danh sách vé
                } else {
                    JOptionPane.showMessageDialog(dialog, "Thêm vé thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập số hợp lệ cho các trường số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi thêm vé: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateTicket() {
        // Triển khai logic cập nhật vé
        JOptionPane.showMessageDialog(this, "Chức năng cập nhật vé sẽ được triển khai sau");
    }

    private void deleteTicket() {
        // Triển khai logic xóa vé
        JOptionPane.showMessageDialog(this, "Chức năng xóa vé sẽ được triển khai sau");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VeView view = new VeView();
            view.setVisible(true);
        });
    }
}