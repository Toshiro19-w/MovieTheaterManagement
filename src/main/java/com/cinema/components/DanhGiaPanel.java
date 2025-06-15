package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.cinema.controllers.DanhGiaController;
import com.cinema.controllers.KhachHangController;
import com.cinema.models.DanhGia;
import com.cinema.services.DanhGiaService;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;

public class DanhGiaPanel extends JPanel {
    private final DanhGiaController danhGiaController;
    private final KhachHangController khachHangController;
    private final int maPhim;
    private final String username;
    private JPanel danhGiaListPanel;
    private JPanel formPanel;
    private List<DanhGia> danhGiaList;
    private static final int LIMIT_DANHGIA = 10;

    public DanhGiaPanel(int maPhim, String username) throws IOException {
        this.maPhim = maPhim;
        this.username = username;
        this.danhGiaController = new DanhGiaController(new DanhGiaService(new DatabaseConnection()));
        this.khachHangController = new KhachHangController(new KhachHangService(new DatabaseConnection()));
        
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        
        initializeComponents();
        loadDanhGia();
    }
    
    private void initializeComponents() {
        // Tiêu đề
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Đánh giá từ khách hàng", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Hiển thị điểm trung bình
        JLabel avgRatingLabel = new JLabel();
        try {
            double avgRating = danhGiaController.getDiemDanhGiaTrungBinh(maPhim);
            avgRatingLabel.setText(String.format("Điểm trung bình: %.1f/5", avgRating));
        } catch (SQLException e) {
            avgRatingLabel.setText("Điểm trung bình: N/A");
        }
        avgRatingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titlePanel.add(avgRatingLabel, BorderLayout.EAST);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Panel chứa danh sách đánh giá
        danhGiaListPanel = new JPanel();
        danhGiaListPanel.setLayout(new BoxLayout(danhGiaListPanel, BoxLayout.Y_AXIS));
        danhGiaListPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(danhGiaListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(600, 200));
        add(scrollPane, BorderLayout.CENTER);
        
        // Form đánh giá
        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Thêm đánh giá"));
        
        // Kiểm tra xem người dùng có thể đánh giá không
        try {
            int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
            boolean daXemPhim = danhGiaController.daXemPhim(maKhachHang, maPhim);
            boolean daDanhGia = danhGiaController.daDanhGia(maKhachHang, maPhim);
            
            if (daXemPhim && !daDanhGia) {
                createDanhGiaForm();
            } else if (daDanhGia) {
                JLabel messageLabel = new JLabel("Bạn đã đánh giá phim này rồi.");
                messageLabel.setAlignmentX(LEFT_ALIGNMENT);
                formPanel.add(messageLabel);
            } else {
                JLabel messageLabel = new JLabel("Bạn cần mua vé và xem phim này trước khi đánh giá.");
                messageLabel.setAlignmentX(LEFT_ALIGNMENT);
                formPanel.add(messageLabel);
            }
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Lỗi: " + e.getMessage());
            errorLabel.setAlignmentX(LEFT_ALIGNMENT);
            formPanel.add(errorLabel);
        }
        
        add(formPanel, BorderLayout.SOUTH);
    }
    
    private void createDanhGiaForm() {
        // Panel chứa điểm đánh giá
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.setBackground(Color.WHITE);
        
        JLabel ratingLabel = new JLabel("Điểm đánh giá: ");
        ratingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        Integer[] ratings = {1, 2, 3, 4, 5};
        JComboBox<Integer> ratingComboBox = new JComboBox<>(ratings);
        ratingComboBox.setSelectedIndex(4); // Mặc định chọn 5 sao
        
        ratingPanel.add(ratingLabel);
        ratingPanel.add(ratingComboBox);
        ratingPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        // Panel chứa nhận xét
        JPanel commentPanel = new JPanel();
        commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
        commentPanel.setBackground(Color.WHITE);
        
        JLabel commentLabel = new JLabel("Nhận xét của bạn:");
        commentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        commentLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JTextArea commentTextArea = new JTextArea(3, 40);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        JScrollPane commentScrollPane = new JScrollPane(commentTextArea);
        commentScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        
        commentPanel.add(commentLabel);
        commentPanel.add(Box.createVerticalStrut(5));
        commentPanel.add(commentScrollPane);
        commentPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        // Button gửi đánh giá
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton submitButton = new JButton("Gửi đánh giá");
        submitButton.setBackground(new Color(0, 48, 135));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
                    int maVe = danhGiaController.getMaVeDaMua(maKhachHang, maPhim);
                    
                    if (maVe == -1) {
                        JOptionPane.showMessageDialog(DanhGiaPanel.this, 
                                "Không tìm thấy vé đã mua cho phim này.", 
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    DanhGia danhGia = new DanhGia();
                    danhGia.setMaPhim(maPhim);
                    danhGia.setMaNguoiDung(maKhachHang);
                    danhGia.setMaVe(maVe);
                    danhGia.setDiemDanhGia((Integer) ratingComboBox.getSelectedItem());
                    danhGia.setNhanXet(commentTextArea.getText());
                    danhGia.setNgayDanhGia(LocalDateTime.now());
                    
                    int maDanhGia = danhGiaController.themDanhGia(danhGia);
                    
                    if (maDanhGia > 0) {
                        JOptionPane.showMessageDialog(DanhGiaPanel.this, 
                                "Đánh giá của bạn đã được gửi thành công!", 
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Cập nhật lại giao diện
                        removeAll();
                        initializeComponents();
                        loadDanhGia();
                        revalidate();
                        repaint();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(DanhGiaPanel.this, 
                            "Lỗi khi gửi đánh giá: " + ex.getMessage(), 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        buttonPanel.add(submitButton);
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        // Thêm các panel vào form
        formPanel.add(ratingPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(commentPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(buttonPanel);
    }
    
    private void loadDanhGia() {
        try {
            danhGiaList = danhGiaController.getDanhGiaByPhimId(maPhim, LIMIT_DANHGIA);
            danhGiaListPanel.removeAll();
            
            if (danhGiaList.isEmpty()) {
                JLabel emptyLabel = new JLabel("Chưa có đánh giá nào cho phim này.");
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                danhGiaListPanel.add(emptyLabel);
            } else {
                for (DanhGia danhGia : danhGiaList) {
                    danhGiaListPanel.add(createDanhGiaItemPanel(danhGia));
                    danhGiaListPanel.add(Box.createVerticalStrut(10));
                }
            }
            
            danhGiaListPanel.revalidate();
            danhGiaListPanel.repaint();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                    "Lỗi khi tải danh sách đánh giá: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createDanhGiaItemPanel(DanhGia danhGia) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout(10, 5));
        itemPanel.setBackground(new Color(245, 245, 245));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header panel (tên người dùng, điểm đánh giá, ngày)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        
        // Tên người dùng
        String displayName = danhGia.getTenNguoiDung() != null ? 
                danhGia.getTenNguoiDung() : "Người dùng ẩn danh";
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(nameLabel, BorderLayout.WEST);
        
        // Panel bên phải chứa điểm và ngày
        JPanel rightHeaderPanel = new JPanel(new GridLayout(2, 1));
        rightHeaderPanel.setBackground(new Color(245, 245, 245));
        
        // Điểm đánh giá
        JLabel ratingLabel = new JLabel(getRatingStars(danhGia.getDiemDanhGia()));
        ratingLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightHeaderPanel.add(ratingLabel);
        
        // Ngày đánh giá
        JLabel dateLabel = new JLabel(danhGia.getNgayDanhGiaFormatted());
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightHeaderPanel.add(dateLabel);
        
        headerPanel.add(rightHeaderPanel, BorderLayout.EAST);
        
        // Nội dung đánh giá
        JLabel contentLabel = new JLabel("<html><div style='width: 500px;'>" + 
                (danhGia.getNhanXet() != null && !danhGia.getNhanXet().isEmpty() ? 
                        danhGia.getNhanXet() : "Không có nhận xét") + 
                "</div></html>");
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Thêm vào panel chính
        itemPanel.add(headerPanel, BorderLayout.NORTH);
        itemPanel.add(contentLabel, BorderLayout.CENTER);
        
        // Kiểm tra xem đánh giá có phải của người dùng hiện tại không
        try {
            int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
            if (danhGia.getMaNguoiDung() == maKhachHang) {
                // Thêm nút sửa đánh giá
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setBackground(new Color(245, 245, 245));
                
                JButton editButton = new JButton("Sửa");
                editButton.setBackground(new Color(0, 48, 135));
                editButton.setForeground(Color.WHITE);
                editButton.addActionListener(e -> showEditDanhGiaDialog(danhGia));
                
                buttonPanel.add(editButton);
                itemPanel.add(buttonPanel, BorderLayout.SOUTH);
            }
        } catch (SQLException e) {
            // Không làm gì nếu có lỗi
        }
        
        return itemPanel;
    }
    
    private void showEditDanhGiaDialog(DanhGia danhGia) {
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa đánh giá", true);
        editDialog.setSize(500, 300);
        editDialog.setLayout(new BorderLayout());
        editDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);
        
        // Điểm đánh giá
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.setBackground(Color.WHITE);
        
        JLabel ratingLabel = new JLabel("Điểm đánh giá: ");
        ratingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        Integer[] ratings = {1, 2, 3, 4, 5};
        JComboBox<Integer> ratingComboBox = new JComboBox<>(ratings);
        ratingComboBox.setSelectedItem(danhGia.getDiemDanhGia());
        
        ratingPanel.add(ratingLabel);
        ratingPanel.add(ratingComboBox);
        ratingPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        // Nhận xét
        JPanel commentPanel = new JPanel();
        commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
        commentPanel.setBackground(Color.WHITE);
        
        JLabel commentLabel = new JLabel("Nhận xét của bạn:");
        commentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        commentLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JTextArea commentTextArea = new JTextArea(5, 40);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setText(danhGia.getNhanXet());
        JScrollPane commentScrollPane = new JScrollPane(commentTextArea);
        commentScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        
        commentPanel.add(commentLabel);
        commentPanel.add(Box.createVerticalStrut(5));
        commentPanel.add(commentScrollPane);
        commentPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        // Nút lưu và hủy
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelButton = new JButton("Hủy");
        cancelButton.addActionListener(e -> editDialog.dispose());
        
        JButton saveButton = new JButton("Lưu");
        saveButton.setBackground(new Color(0, 48, 135));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            try {
                // Cập nhật đánh giá
                danhGia.setDiemDanhGia((Integer) ratingComboBox.getSelectedItem());
                danhGia.setNhanXet(commentTextArea.getText());
                
                boolean success = danhGiaController.capNhatDanhGia(danhGia);
                if (success) {
                    JOptionPane.showMessageDialog(editDialog, 
                            "Đánh giá đã được cập nhật thành công!", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    editDialog.dispose();
                    
                    // Cập nhật lại giao diện
                    loadDanhGia();
                } else {
                    JOptionPane.showMessageDialog(editDialog, 
                            "Không thể cập nhật đánh giá.", 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(editDialog, 
                        "Lỗi khi cập nhật đánh giá: " + ex.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // Thêm các panel vào dialog
        contentPanel.add(ratingPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(commentPanel);
        
        editDialog.add(contentPanel, BorderLayout.CENTER);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);
        editDialog.setVisible(true);
    }
    
    private String getRatingStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★"); // Unicode star character
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆"); // Unicode empty star character
        }
        return stars.toString();
    }
}