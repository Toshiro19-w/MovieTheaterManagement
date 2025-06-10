package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.cinema.components.PhimCarouselPanel;
import com.cinema.controllers.KhachHangController;
import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.services.KhachHangService;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.TimeFormatter;

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
        detailDialog.setSize(800, 500);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setBackground(Color.WHITE);

        // Panel chính chứa thông tin chi tiết
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel bên trái chứa poster lớn
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(300, 400));

        JLabel posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                // Thử nhiều cách để tìm ảnh
                ImageIcon posterIcon = null;
                
                // Cách 1: Thử tải trực tiếp từ đường dẫn
                File file = new File(phim.getDuongDanPoster());
                if (file.exists()) {
                    posterIcon = new ImageIcon(phim.getDuongDanPoster());
                } else {
                    // Cách 2: Thử tìm trong resources
                    String fileName = phim.getDuongDanPoster();
                    if (fileName.contains("\\") || fileName.contains("/")) {
                        fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1);
                    }
                    
                    URL resourceUrl = getClass().getClassLoader().getResource("images/posters/" + fileName);
                    if (resourceUrl != null) {
                        posterIcon = new ImageIcon(resourceUrl);
                    } else {
                        // Cách 3: Thử tìm với đường dẫn tương đối
                        String relativePath = "src/main/resources/images/posters/" + fileName;
                        File relativeFile = new File(relativePath);
                        if (relativeFile.exists()) {
                            posterIcon = new ImageIcon(relativePath);
                        }
                    }
                }
                
                // Nếu vẫn không tìm thấy, hiển thị thông báo
                if (posterIcon == null || posterIcon.getIconWidth() <= 0) {
                    posterLabel.setText("Không tìm thấy ảnh");
                } else {
                    Image scaledImage = posterIcon.getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH);
                    posterLabel.setIcon(new ImageIcon(scaledImage));
                }
            } catch (Exception e) {
                e.printStackTrace();
                posterLabel.setText("Lỗi tải ảnh");
            }
        } else {
            posterLabel.setText("Không có ảnh");
        }
        leftPanel.add(posterLabel, BorderLayout.CENTER);

        // Panel bên phải chứa thông tin chi tiết
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);

        // Tên phim
        JLabel titleLabel = new JLabel(phim.getTenPhim());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Thông tin chi tiết
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Thể loại
        JLabel genreLabel = new JLabel("\uD83C\uDFAC Thể loại: " + phim.getTenTheLoai());
        genreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        genreLabel.setAlignmentX(LEFT_ALIGNMENT);
        genreLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Thời lượng
        JLabel durationLabel = new JLabel("\u23F1 Thời lượng: " + TimeFormatter.formatMinutesToHoursAndMinutes(phim.getThoiLuong()));
        durationLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        durationLabel.setAlignmentX(LEFT_ALIGNMENT);
        durationLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Quốc gia
        JLabel countryLabel = new JLabel("\uD83C\uDF0E Quốc gia: " + (phim.getNuocSanXuat() != null ? phim.getNuocSanXuat() : "N/A"));
        countryLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        countryLabel.setAlignmentX(LEFT_ALIGNMENT);
        countryLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Đạo diễn
        JLabel directorLabel = new JLabel("\uD83C\uDFA5 Đạo diễn: " + (phim.getDaoDien() != null ? phim.getDaoDien() : "N/A"));
        directorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        directorLabel.setAlignmentX(LEFT_ALIGNMENT);
        directorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Ngày khởi chiếu
        JLabel releaseDateLabel = new JLabel("\uD83D\uDCC5 Ngày khởi chiếu: " + 
                (phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "N/A"));
        releaseDateLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        releaseDateLabel.setAlignmentX(LEFT_ALIGNMENT);
        releaseDateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Mô tả
        JLabel descriptionTitle = new JLabel("Mô tả:");
        descriptionTitle.setFont(new Font("Arial", Font.BOLD, 16));
        descriptionTitle.setAlignmentX(LEFT_ALIGNMENT);
        descriptionTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JLabel descriptionLabel = new JLabel("<html><div style='width: 400px;'>" + 
                (phim.getMoTa() != null ? phim.getMoTa() : "Không có mô tả") + "</div></html>");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Thêm các thành phần vào panel thông tin
        infoPanel.add(genreLabel);
        infoPanel.add(durationLabel);
        infoPanel.add(countryLabel);
        infoPanel.add(directorLabel);
        infoPanel.add(releaseDateLabel);
        infoPanel.add(descriptionTitle);
        infoPanel.add(descriptionLabel);

        // Nút đặt vé hoặc đóng
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton actionButton;
        if ("active".equals(phim.getTrangThai())) {
            actionButton = new JButton("Đặt vé");
            actionButton.addActionListener(e -> {
                try {
                    int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
                    bookTicketCallback.accept(phim.getMaPhim(), maKhachHang);
                    detailDialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(detailDialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            actionButton = new JButton("Đóng");
            actionButton.addActionListener(e -> detailDialog.dispose());
        }

        actionButton.setBackground(new Color(0, 48, 135));
        actionButton.setForeground(Color.WHITE);
        actionButton.setFont(new Font("Arial", Font.BOLD, 16));
        actionButton.setPreferredSize(new Dimension(150, 40));
        actionButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                actionButton.setBackground(new Color(0, 72, 202));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                actionButton.setBackground(new Color(0, 48, 135));
            }
        });

        buttonPanel.add(actionButton, BorderLayout.WEST);

        // Thêm các thành phần vào panel bên phải
        rightPanel.add(titleLabel);
        rightPanel.add(infoPanel);
        rightPanel.add(buttonPanel);

        // Thêm các panel vào panel chính
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Thêm panel chính vào dialog
        detailDialog.add(mainPanel, BorderLayout.CENTER);
        detailDialog.setVisible(true);
    }
}