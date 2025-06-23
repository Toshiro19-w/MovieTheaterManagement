package com.cinema.views.admin;

import com.cinema.controllers.PhimController;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

public class GenreDialog extends JDialog {
    private final PhimController controller;
    private DefaultListModel<String> genreModel;
    private JList<String> genreList;

    public GenreDialog(Window parent, PhimController controller) {
        super(parent, "Quản lý thể loại", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        setSize(420, 420);
        setMinimumSize(new Dimension(320, 320));
        setResizable(true);
        setLocationRelativeTo(parent);
        buildUI();
        loadGenres();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        genreModel = new DefaultListModel<>();
        genreList = new JList<>(genreModel);
        genreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(genreList);
        main.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton btnAdd = new JButton("Thêm");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDel = new JButton("Xóa");
        JButton btnClose = new JButton("Đóng");

        bottom.add(btnAdd); bottom.add(btnEdit); bottom.add(btnDel); bottom.add(btnClose);
        main.add(bottom, BorderLayout.SOUTH);
        setContentPane(main);

        btnAdd.addActionListener(e -> addGenre());
        btnEdit.addActionListener(e -> editGenre());
        btnDel.addActionListener(e -> deleteGenre());
        btnClose.addActionListener(e -> dispose());
    }

    private void loadGenres() {
        try {
            genreModel.clear();
            Map<Integer, String> genres = controller.getAllTheLoaiMap();
            for (String s : genres.values()) genreModel.addElement(s);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không tải được danh sách thể loại!\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addGenre() {
        String input = JOptionPane.showInputDialog(this, "Nhập tên thể loại mới:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                controller.getService().addTheLoai(input.trim());
                loadGenres();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi thêm thể loại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editGenre() {
        int idx = genreList.getSelectedIndex();
        if (idx >= 0) {
            String oldName = genreModel.get(idx);
            String input = JOptionPane.showInputDialog(this, "Sửa tên thể loại:", oldName);
            if (input != null && !input.trim().isEmpty() && !input.trim().equals(oldName)) {
                try {
                    int ma = controller.getService().getMaTheLoaiByTen(oldName);
                    controller.getService().updateTheLoai(ma, input.trim());
                    loadGenres();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi sửa thể loại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Chọn thể loại cần sửa!", "Chú ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteGenre() {
        int idx = genreList.getSelectedIndex();
        if (idx >= 0) {
            String ten = genreModel.get(idx);
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa thể loại này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int ma = controller.getService().getMaTheLoaiByTen(ten);
                    controller.getService().deleteTheLoai(ma);
                    loadGenres();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi xóa thể loại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Chọn thể loại để xóa!", "Chú ý", JOptionPane.WARNING_MESSAGE);
        }
    }
}