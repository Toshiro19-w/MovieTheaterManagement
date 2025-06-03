package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FlowLayout;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.cinema.controllers.KhachHangController;
import com.cinema.models.Phim;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.TimeFormatter;

public class PhimCardPanel extends JPanel {
    private Phim phim;
    private BiConsumer<Integer, Integer> bookTicketCallback;
    private Consumer<Phim> detailCallback;
    private String username;
    private JPanel posterCardContainer;
    private JPanel hoverPanel;
    private CardLayout posterCardLayout;
    private JLabel posterLabel;
    private float alpha = 0.0f;
    private Timer fadeTimer;
    private JPanel posterContainer;
    private boolean isHovering = false;
    
    public PhimCardPanel(Phim phim, BiConsumer<Integer, Integer> bookTicketCallback, 
                        Consumer<Phim> detailCallback, String username) {
        this.phim = phim;
        this.bookTicketCallback = bookTicketCallback;
        this.detailCallback = detailCallback;
        this.username = username;
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(220, 370));
        setBackground(Color.WHITE);
        
        initComponents();
    }
    
    private void initComponents() {
        // Panel chính sử dụng BoxLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        
        // Poster container với CardLayout để chuyển đổi giữa poster và hover panel
        posterCardContainer = new JPanel();
        posterCardLayout = new CardLayout();
        posterCardContainer.setLayout(posterCardLayout);
        posterCardContainer.setPreferredSize(new Dimension(220, 280));
        posterCardContainer.setMaximumSize(new Dimension(220, 280));
        
        // Poster container với border
        posterContainer = new JPanel(new BorderLayout());
        posterContainer.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        posterContainer.setBackground(Color.WHITE);
        
        // Poster
        posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(220, 280));
        
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                // Thử nhiều cách để tìm ảnh
                ImageIcon icon = null;
                
                // Cách 1: Thử tải trực tiếp từ đường dẫn
                File file = new File(phim.getDuongDanPoster());
                if (file.exists()) {
                    icon = new ImageIcon(phim.getDuongDanPoster());
                } else {
                    // Cách 2: Thử tìm trong resources
                    String fileName = phim.getDuongDanPoster();
                    if (fileName.contains("\\") || fileName.contains("/")) {
                        fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1);
                    }
                    
                    URL resourceUrl = getClass().getClassLoader().getResource("images/posters/" + fileName);
                    if (resourceUrl != null) {
                        icon = new ImageIcon(resourceUrl);
                    } else {
                        // Cách 3: Thử tìm với đường dẫn tương đối
                        String relativePath = "src/main/resources/images/posters/" + fileName;
                        File relativeFile = new File(relativePath);
                        if (relativeFile.exists()) {
                            icon = new ImageIcon(relativePath);
                        }
                    }
                }
                
                // Nếu vẫn không tìm thấy, hiển thị thông báo
                if (icon == null || icon.getIconWidth() <= 0) {
                    posterLabel.setText("Không tìm thấy ảnh");
                } else {
                    Image img = icon.getImage().getScaledInstance(220, 280, Image.SCALE_SMOOTH);
                    posterLabel.setIcon(new ImageIcon(img));
                }
            } catch (Exception e) {
                e.printStackTrace();
                posterLabel.setText("Lỗi tải ảnh");
            }
        } else {
            posterLabel.setText("Không có ảnh");
        }
        
        posterContainer.add(posterLabel, BorderLayout.CENTER);
        
        // Tạo hover panel với thông tin chi tiết
        createHoverPanel();
        
        // Thêm cả hai panel vào poster card container
        posterCardContainer.add(posterContainer, "normal");
        posterCardContainer.add(hoverPanel, "hover");
        
        // Thêm sự kiện hover chỉ cho poster container
        posterContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isHovering) {
                    isHovering = true;
                    showHoverPanel();
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Kiểm tra xem chuột có thực sự ra khỏi vùng poster không
                if (!posterContainer.getBounds().contains(e.getPoint())) {
                    isHovering = false;
                    hideHoverPanel();
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                detailCallback.accept(phim);
            }
        });
        
        // Thêm poster card container vào main panel
        mainPanel.add(posterCardContainer);
        
        // Thêm khoảng cách
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Tên phim
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>" + phim.getTenPhim() + "</div></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel);
        
        // Thêm khoảng cách
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
        
        // Nút chi tiết
        JButton detailButton = new JButton("Chi tiết");
        detailButton.setBackground(new Color(220, 220, 220));
        detailButton.setForeground(Color.BLACK);
        detailButton.setFont(new Font("Arial", Font.BOLD, 12));
        detailButton.setBorderPainted(false);
        detailButton.setFocusPainted(false);
        detailButton.addActionListener(e -> detailCallback.accept(phim));
        detailButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                detailButton.setBackground(new Color(200, 200, 200));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                detailButton.setBackground(new Color(220, 220, 220));
            }
        });
        buttonPanel.add(detailButton);
        
        // Nút đặt vé hoặc tìm hiểu phim
        String buttonText = "active".equals(phim.getTrangThai()) ? "Mua vé" : "Sắp chiếu";
        JButton actionButton = new JButton(buttonText);
        actionButton.setBackground("active".equals(phim.getTrangThai()) ? new Color(230, 0, 0) : new Color(0, 48, 135));
        actionButton.setForeground(Color.WHITE);
        actionButton.setFont(new Font("Arial", Font.BOLD, 12));
        actionButton.setBorderPainted(false);
        actionButton.setFocusPainted(false);
        
        actionButton.addActionListener(e -> {
            if ("active".equals(phim.getTrangThai())) {
                try {
                    KhachHangController khachHangController = new KhachHangController(
                            new KhachHangService(new DatabaseConnection()));
                    int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
                    bookTicketCallback.accept(phim.getMaPhim(), maKhachHang);
                } catch (SQLException | IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                detailCallback.accept(phim);
            }
        });
        
        actionButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                actionButton.setBackground("active".equals(phim.getTrangThai()) ? 
                    new Color(200, 0, 0) : new Color(0, 72, 202));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                actionButton.setBackground("active".equals(phim.getTrangThai()) ? 
                    new Color(230, 0, 0) : new Color(0, 48, 135));
            }
        });
        buttonPanel.add(actionButton);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void createHoverPanel() {
        hoverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        hoverPanel.setLayout(new GridBagLayout());
        hoverPanel.setOpaque(false);
        hoverPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        // Tiêu đề phim
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'><b>" + phim.getTenPhim() + "</b></div></html>");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.insets = new Insets(10, 10, 15, 10);
        hoverPanel.add(titleLabel, gbc);
        
        // Thể loại
        JLabel genreLabel = new JLabel("\uD83C\uDFAC Thể loại: " + phim.getTenTheLoai());
        genreLabel.setForeground(Color.WHITE);
        genreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.insets = new Insets(5, 10, 5, 10);
        hoverPanel.add(genreLabel, gbc);
        
        // Thời lượng
        JLabel durationLabel = new JLabel("\u23F1 Thời lượng: " + 
                TimeFormatter.formatMinutesToHoursAndMinutes(phim.getThoiLuong()));
        durationLabel.setForeground(Color.WHITE);
        durationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        hoverPanel.add(durationLabel, gbc);
        
        // Quốc gia
        JLabel countryLabel = new JLabel("\uD83C\uDF0E Quốc gia: " + 
                (phim.getNuocSanXuat() != null ? phim.getNuocSanXuat() : "N/A"));
        countryLabel.setForeground(Color.WHITE);
        countryLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        hoverPanel.add(countryLabel, gbc);
        
        // Thêm sự kiện mouse listener cho hover panel để tránh flicker
        hoverPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                isHovering = false;
                hideHoverPanel();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                detailCallback.accept(phim);
            }
        });
    }
    
    private void showHoverPanel() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        
        posterCardLayout.show(posterCardContainer, "hover");
        
        fadeTimer = new Timer(20, e -> {
            alpha += 0.1f;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();
            }
            hoverPanel.repaint();
        });
        
        fadeTimer.start();
    }
    
    private void hideHoverPanel() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        
        fadeTimer = new Timer(20, e -> {
            alpha -= 0.1f;
            if (alpha <= 0.0f) {
                alpha = 0.0f;
                fadeTimer.stop();
                posterCardLayout.show(posterCardContainer, "normal");
            }
            hoverPanel.repaint();
        });
        
        fadeTimer.start();
    }
}