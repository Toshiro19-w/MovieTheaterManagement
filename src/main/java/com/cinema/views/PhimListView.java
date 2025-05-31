package com.cinema.views;

import com.cinema.components.PhimCarouselPanel;
import com.cinema.controllers.KhachHangController;
import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.services.KhachHangService;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.TimeFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PhimListView extends JPanel {
    private final PhimService phimService;
    private final KhachHangController khachHangController;
    private final BiConsumer<Integer, Integer> bookTicketCallback;
    private final String username;
    private PhimCarouselPanel dangChieuPanel;
    private PhimCarouselPanel sapChieuPanel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PhimListView(PhimController phimController, BiConsumer<Integer, Integer> bookTicketCallback, String username) throws IOException, SQLException {
        this.phimService = new PhimService(new DatabaseConnection());
        this.khachHangController = new KhachHangController(new KhachHangService(new DatabaseConnection()));
        this.bookTicketCallback = bookTicketCallback;
        this.username = username;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeComponents();
        loadPhimList("");
    }

    private void initializeComponents() {
        // Content panel (chứa cả hai danh sách phim)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Panel phim đang chiếu
        dangChieuPanel = new PhimCarouselPanel("Phim đang chiếu", bookTicketCallback, this::showPhimDetail, username);
        contentPanel.add(dangChieuPanel);
        
        // Panel phim sắp chiếu
        sapChieuPanel = new PhimCarouselPanel("Phim sắp chiếu", bookTicketCallback, this::showPhimDetail, username);
        contentPanel.add(sapChieuPanel);

        // Thêm vào scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadPhimList(String searchText) {
        try {
            List<Phim> allPhims = phimService.getAllPhim();
            LocalDate today = LocalDate.now();
            
            // Lọc phim đang chiếu (trạng thái active)
            List<Phim> phimDangChieu = allPhims.stream()
                    .filter(p -> "active".equals(p.getTrangThai()))
                    .collect(Collectors.toList());
            
            // Lọc phim sắp chiếu (trạng thái upcoming hoặc ngày khởi chiếu trong tương lai)
            List<Phim> phimSapChieu = allPhims.stream()
                    .filter(p -> "upcoming".equals(p.getTrangThai()) || 
                           (p.getNgayKhoiChieu() != null && p.getNgayKhoiChieu().isAfter(today)))
                    .collect(Collectors.toList());
            
            // Nếu có từ khóa tìm kiếm
            if (!searchText.isEmpty()) {
                String searchLower = searchText.toLowerCase();
                phimDangChieu = phimDangChieu.stream()
                        .filter(phim -> phim.getTenPhim().toLowerCase().contains(searchLower) ||
                                phim.getTenTheLoai().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                
                phimSapChieu = phimSapChieu.stream()
                        .filter(phim -> phim.getTenPhim().toLowerCase().contains(searchLower) ||
                                phim.getTenTheLoai().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
            }
            
            // Cập nhật các carousel
            dangChieuPanel.setPhimList(phimDangChieu);
            sapChieuPanel.setPhimList(phimSapChieu);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phim: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPhimDetail(Phim phim) {
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết phim: " + phim.getTenPhim(), true);
        detailDialog.setSize(600, 500);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setBackground(Color.WHITE);

        JLabel detailPosterLabel = new JLabel();
        detailPosterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                ImageIcon posterIcon = new ImageIcon(phim.getDuongDanPoster());
                Image scaledImage = posterIcon.getImage().getScaledInstance(300, 350, Image.SCALE_SMOOTH);
                detailPosterLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                e.printStackTrace();
                detailPosterLabel.setText("Không có ảnh");
            }
        } else {
            detailPosterLabel.setText("Không có ảnh");
        }

        JPanel infoPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.setBackground(Color.WHITE);

        infoPanel.add(new JLabel("Tên phim:"));
        infoPanel.add(new JLabel(phim.getTenPhim()));

        infoPanel.add(new JLabel("Thể loại:"));
        infoPanel.add(new JLabel(phim.getTenTheLoai()));

        infoPanel.add(new JLabel("Thời lượng:"));
        infoPanel.add(new JLabel(TimeFormatter.formatMinutesToHoursAndMinutes(phim.getThoiLuong())));

        infoPanel.add(new JLabel("Ngày khởi chiếu:"));
        infoPanel.add(new JLabel(phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "N/A"));

        infoPanel.add(new JLabel("Nước sản xuất:"));
        infoPanel.add(new JLabel(phim.getNuocSanXuat() != null ? phim.getNuocSanXuat() : "N/A"));

        infoPanel.add(new JLabel("Định dạng:"));
        infoPanel.add(new JLabel(phim.getKieuPhim() != null ? phim.getKieuPhim() : "N/A"));

        infoPanel.add(new JLabel("Đạo diễn:"));
        infoPanel.add(new JLabel(phim.getDaoDien() != null ? phim.getDaoDien() : "N/A"));

        infoPanel.add(new JLabel("Mô tả:"));
        infoPanel.add(new JLabel(phim.getMoTa() != null ? phim.getMoTa() : "N/A"));

        JPanel mainDetailPanel = new JPanel(new BorderLayout(10, 10));
        mainDetailPanel.setBackground(Color.WHITE);
        mainDetailPanel.add(detailPosterLabel, BorderLayout.WEST);
        mainDetailPanel.add(infoPanel, BorderLayout.CENTER);

        JButton datVeButton = new JButton("Đặt vé");
        datVeButton.setBackground(new Color(0, 48, 135));
        datVeButton.setForeground(Color.WHITE);
        datVeButton.setFont(new Font("Arial", Font.BOLD, 14));
        datVeButton.addActionListener(_ -> {
            int maKhachHang;
            try {
                maKhachHang = khachHangController.getMaKhachHangFromSession(username);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            bookTicketCallback.accept(phim.getMaPhim(), maKhachHang);
            detailDialog.dispose();
        });
        datVeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                datVeButton.setBackground(new Color(0, 72, 202));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                datVeButton.setBackground(new Color(0, 48, 135));
            }
        });

        detailDialog.add(mainDetailPanel, BorderLayout.CENTER);
        detailDialog.add(datVeButton, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }
}