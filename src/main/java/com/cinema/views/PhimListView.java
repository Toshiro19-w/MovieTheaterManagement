package com.cinema.views;

import com.cinema.controllers.KhachHangController;
import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class PhimListView extends JPanel {
    private final PhimController phimController;
    private final KhachHangController khachHangController;
    private final BiConsumer<Integer, Integer> bookTicketCallback;
    private final String username;
    private JPanel phimPanel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PhimListView(PhimController phimController, BiConsumer<Integer, Integer> bookTicketCallback, String username) throws IOException {
        this.phimController = phimController;
        this.khachHangController = new KhachHangController(new KhachHangService(new DatabaseConnection()));
        this.bookTicketCallback = bookTicketCallback;
        this.username = username;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeComponents();
        loadPhimList("");
    }

    private void initializeComponents() {
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(0, 48, 135)); // Nền xanh đậm Cinestar

        // Tiêu đề
        JLabel titleLabel = new JLabel("Phim đang chiếu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);

        // Danh sách phim
        phimPanel = new JPanel(new GridBagLayout());
        phimPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(phimPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(1280, 600));
        contentPanel.add(scrollPane);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void loadPhimList(String searchText) {
        phimPanel.removeAll();
        List<Phim> phimList;
        try {
            phimList = phimController.getAllPhim();
            if (!searchText.isEmpty()) {
                String searchLower = searchText.toLowerCase();
                phimList = phimList.stream()
                        .filter(phim -> phim.getTenPhim().toLowerCase().contains(searchLower) ||
                                phim.getTenTheLoai().toLowerCase().contains(searchLower))
                        .toList();
            }
        } catch (Exception e) {
            int retry = JOptionPane.showConfirmDialog(this, "Lỗi khi tải danh sách phim! Thử lại?", "Lỗi", JOptionPane.YES_NO_OPTION);
            if (retry == JOptionPane.YES_OPTION) {
                loadPhimList(searchText);
            }
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;
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
        JPanel phimCard = new JPanel(new BorderLayout(10, 10));
        phimCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        phimCard.setPreferredSize(new Dimension(280, 400));
        phimCard.setBackground(Color.WHITE);
        phimCard.setToolTipText(phim.getMoTa() != null ? phim.getMoTa() : "Không có mô tả");

        JLabel posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                ImageIcon posterIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/posters/" + phim.getDuongDanPoster())));
                Image scaledImage = posterIcon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                posterLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                posterLabel.setText("Không có ảnh");
                posterLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
            }
        } else {
            posterLabel.setText("Không có ảnh");
            posterLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
        }
        phimCard.add(posterLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel phimLabel = new JLabel("<html><center><b>" + phim.getTenPhim() + "</b></center></html>");
        phimLabel.setFont(new Font("Roboto", Font.BOLD, 16));
        phimLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(phimLabel, gbc);

        gbc.gridy++;
        JLabel detailLabel = new JLabel(phim.getTenTheLoai() + " | " + phim.getThoiLuong() + " phút");
        detailLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
        detailLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(detailLabel, gbc);

        gbc.gridy++;
        JButton datVeButton = createStyledButton("Đặt vé");
        datVeButton.addActionListener(_ -> {
            try {
                int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
                bookTicketCallback.accept(phim.getMaPhim(), maKhachHang);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đặt vé!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        infoPanel.add(datVeButton, gbc);

        phimCard.add(infoPanel, BorderLayout.CENTER);

        // Hiệu ứng hover
        phimCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                phimCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 48, 135), 2, true),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                phimCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!datVeButton.getBounds().contains(e.getPoint())) {
                    showPhimDetail(phim);
                }
            }
        });

        return phimCard;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(255, 215, 0)); // Màu vàng Cinestar
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Roboto", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 48, 135)); // Xanh đậm khi hover
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 215, 0));
                button.setForeground(Color.BLACK);
            }
        });
        return button;
    }

    private void showPhimDetail(Phim phim) {
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết phim: " + phim.getTenPhim(), true);
        detailDialog.setSize(700, 500);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setBackground(Color.WHITE);

        

        // Poster
        JLabel detailPosterLabel = new JLabel();
        detailPosterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                ImageIcon posterIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/Posters/" + phim.getDuongDanPoster())));
                Image scaledImage = posterIcon.getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH);
                detailPosterLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                detailPosterLabel.setText("Không có ảnh");
                detailPosterLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
            }
        } else {
            detailPosterLabel.setText("Không có ảnh");
            detailPosterLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
        }

        // Thông tin chi tiết
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addInfoLabel(infoPanel, gbc, "Tên phim:", phim.getTenPhim());
        addInfoLabel(infoPanel, gbc, "Thể loại:", phim.getTenTheLoai());
        addInfoLabel(infoPanel, gbc, "Thời lượng:", phim.getThoiLuong() + " phút");
        addInfoLabel(infoPanel, gbc, "Ngày khởi chiếu:", phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "N/A");
        addInfoLabel(infoPanel, gbc, "Nước sản xuất:", phim.getNuocSanXuat() != null ? phim.getNuocSanXuat() : "N/A");
        addInfoLabel(infoPanel, gbc, "Định dạng:", phim.getDinhDang() != null ? phim.getDinhDang() : "N/A");
        addInfoLabel(infoPanel, gbc, "Đạo diễn:", phim.getDaoDien() != null ? phim.getDaoDien() : "N/A");
        addInfoLabel(infoPanel, gbc, "Mô tả:", phim.getMoTa() != null ? phim.getMoTa() : "N/A");

        JPanel mainDetailPanel = new JPanel(new BorderLayout(10, 10));
        mainDetailPanel.setBackground(Color.WHITE);
        mainDetailPanel.add(detailPosterLabel, BorderLayout.WEST);
        mainDetailPanel.add(infoPanel, BorderLayout.CENTER);

        JButton datVeButton = createStyledButton("Đặt vé");
        datVeButton.addActionListener(_ -> {
            try {
                int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
                bookTicketCallback.accept(phim.getMaPhim(), maKhachHang);
                detailDialog.dispose();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đặt vé!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        detailDialog.add(mainDetailPanel, BorderLayout.CENTER);
        detailDialog.add(datVeButton, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }

    private void addInfoLabel(JPanel panel, GridBagConstraints gbc, String label, String value) {
        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 14));
        panel.add(titleLabel, gbc);
        gbc.gridx++;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
        panel.add(valueLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
    }
}