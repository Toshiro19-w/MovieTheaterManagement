package com.cinema.views;

import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class PhimListView extends JPanel {
    private final PhimController phimController;
    private final BiConsumer<Integer, Integer> bookTicketCallback;
    private final String username;
    private final DatabaseConnection databaseConnection;
    private JTextField searchField;
    private JPanel phimPanel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private JLabel bannerLabel;
    private ImageIcon[] bannerIcons;
    private int currentBannerIndex = 0;
    private int bannerX = 0;
    private Timer slideTimer;

    public PhimListView(PhimController phimController, BiConsumer<Integer, Integer> bookTicketCallback, String username) throws IOException {
        this.phimController = phimController;
        this.bookTicketCallback = bookTicketCallback;
        this.username = username;
        this.databaseConnection = new DatabaseConnection();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeComponents();
        loadPhimList("");
        startBannerCarousel();
    }

    private void initializeComponents() {
        // Banner
        bannerLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                if (bannerIcons != null && currentBannerIndex < bannerIcons.length) {
                    if (bannerIcons[currentBannerIndex] != null) {
                        g2d.drawImage(bannerIcons[currentBannerIndex].getImage(), bannerX, 0, null);
                    }
                    int nextIndex = (currentBannerIndex + 1) % bannerIcons.length;
                    if (bannerX > 0 && bannerIcons[nextIndex] != null) {
                        g2d.drawImage(bannerIcons[nextIndex].getImage(), bannerX - 1280, 0, null);
                    }
                }
                g2d.dispose();
            }
        };
        bannerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bannerLabel.setPreferredSize(new Dimension(1280, 300));
        add(bannerLabel, BorderLayout.NORTH);

        // Content panel (tiêu đề, tìm kiếm, danh sách phim)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Tiêu đề "Phim đang chiếu"
        JLabel titleLabel = new JLabel("Phim đang chiếu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 48, 135));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);

        // Ô tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel searchLabel = new JLabel("Tìm kiếm phim:");
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadPhimList(searchField.getText().trim());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadPhimList(searchField.getText().trim());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadPhimList(searchField.getText().trim());
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        contentPanel.add(searchPanel);

        // Danh sách phim
        phimPanel = new JPanel(new GridBagLayout());
        phimPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(phimPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(1280, 400));
        contentPanel.add(scrollPane);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void startBannerCarousel() {
        String[] banners = {"banner1.jpg", "banner2.jpg", "banner3.jpg"};
        bannerIcons = new ImageIcon[banners.length];
        for (int i = 0; i < banners.length; i++) {
            try {
                bannerIcons[i] = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/banners/" + banners[i])));
                Image scaledImage = bannerIcons[i].getImage().getScaledInstance(1280, 300, Image.SCALE_SMOOTH);
                bannerIcons[i] = new ImageIcon(scaledImage);
            } catch (Exception e) {
                bannerIcons[i] = null;
            }
        }

        updateBanner();

        Timer carouselTimer = new Timer(5000, _ -> {
            bannerX = 0;
            if (slideTimer != null && slideTimer.isRunning()) {
                slideTimer.stop();
            }
            slideTimer = new Timer(20, slideEvent -> {
                bannerX += 40;
                if (bannerX >= 1280) {
                    bannerX = 0;
                    currentBannerIndex = (currentBannerIndex + 1) % bannerIcons.length;
                    updateBanner();
                    ((Timer) slideEvent.getSource()).stop();
                }
                bannerLabel.repaint();
            });
            slideTimer.start();
        });
        carouselTimer.start();
    }

    private void updateBanner() {
        if (bannerIcons[currentBannerIndex] != null) {
            bannerLabel.setText("");
            bannerLabel.setOpaque(false);
        } else {
            bannerLabel.setIcon(null);
            bannerLabel.setText("Không có banner");
            bannerLabel.setBackground(new Color(0, 48, 135));
            bannerLabel.setOpaque(true);
            bannerLabel.setForeground(Color.WHITE);
            bannerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        }
        bannerLabel.repaint();
    }

    public void loadPhimList(String searchText) {
        phimPanel.removeAll();
        List<Phim> phimList;
        try {
            phimList = phimController.getAllPhimDetail();
            if (!searchText.isEmpty()) {
                String searchLower = searchText.toLowerCase();
                phimList = phimList.stream()
                        .filter(phim -> phim.getTenPhim().toLowerCase().contains(searchLower) ||
                                phim.getTenTheLoai().toLowerCase().contains(searchLower))
                        .toList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phim!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        for (Phim phim : phimList) {
            JPanel phimCard = createPhimCard(phim);
            phimPanel.add(phimCard, gbc);
            gbc.gridx++;
            if (gbc.gridx > 3) {
                gbc.gridx = 0;
                gbc.gridy++;
            }
        }
        phimPanel.revalidate();
        phimPanel.repaint();
    }

    private JPanel createPhimCard(Phim phim) {
        JPanel phimCard = new JPanel(new BorderLayout(5, 5));
        phimCard.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        phimCard.setPreferredSize(new Dimension(250, 450));
        phimCard.setBackground(Color.WHITE);

        JLabel posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                ImageIcon posterIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/posters/" + phim.getDuongDanPoster())));
                Image scaledImage = posterIcon.getImage().getScaledInstance(250, 350, Image.SCALE_SMOOTH);
                posterLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                e.printStackTrace();
                posterLabel.setText("Không có ảnh");
            }
        } else {
            posterLabel.setText("Không có ảnh");
        }
        phimCard.add(posterLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        JLabel phimLabel = new JLabel("<html><center><b>" + phim.getTenPhim() + "</b><br>" +
                phim.getTenTheLoai() + " | " + phim.getThoiLuong() + " phút</center></html>", SwingConstants.CENTER);
        phimLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(phimLabel, BorderLayout.CENTER);

        JButton datVeButton = new JButton("Đặt vé");
        datVeButton.setBackground(new Color(0, 48, 135));
        datVeButton.setForeground(Color.WHITE);
        datVeButton.setFont(new Font("Arial", Font.BOLD, 14));
        datVeButton.addActionListener(_ -> {
            int maKhachHang = getMaKhachHangFromSession();
            bookTicketCallback.accept(phim.getMaPhim(), maKhachHang);
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
        infoPanel.add(datVeButton, BorderLayout.SOUTH);

        phimCard.add(infoPanel, BorderLayout.CENTER);

        phimCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == phimCard && !datVeButton.getBounds().contains(e.getPoint())) {
                    showPhimDetail(phim);
                }
            }
        });

        return phimCard;
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
                ImageIcon posterIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/posters/" + phim.getDuongDanPoster())));
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
        infoPanel.add(new JLabel(phim.getThoiLuong() + " phút"));

        infoPanel.add(new JLabel("Ngày khởi chiếu:"));
        infoPanel.add(new JLabel(phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "N/A"));

        infoPanel.add(new JLabel("Nước sản xuất:"));
        infoPanel.add(new JLabel(phim.getNuocSanXuat() != null ? phim.getNuocSanXuat() : "N/A"));

        infoPanel.add(new JLabel("Định dạng:"));
        infoPanel.add(new JLabel(phim.getDinhDang() != null ? phim.getDinhDang() : "N/A"));

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
            int maKhachHang = getMaKhachHangFromSession();
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

    private int getMaKhachHangFromSession() {
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT nd.maNguoiDung FROM NguoiDung nd JOIN TaiKhoan tk ON nd.maNguoiDung = tk.maNguoiDung " +
                             "WHERE tk.tenDangNhap = ? AND nd.loaiNguoiDung = 'KhachHang'")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("maNguoiDung");
            }
            throw new SQLException("Không tìm thấy khách hàng cho tài khoản: " + username);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin khách hàng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Lỗi khi lấy maKhachHang: " + e.getMessage(), e);
        }
    }
}