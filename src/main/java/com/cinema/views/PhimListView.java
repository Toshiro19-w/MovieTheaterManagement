package com.cinema.views;

import com.cinema.controllers.KhachHangController;
import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;

import javax.swing.*;
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
    private final KhachHangController khachHangController;
    private final BiConsumer<Integer, Integer> bookTicketCallback;
    private final String username;
    private final DatabaseConnection databaseConnection;
    private JPanel phimPanel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PhimListView(PhimController phimController, BiConsumer<Integer, Integer> bookTicketCallback, String username) throws IOException {
        this.phimController = phimController;
        this.khachHangController = new KhachHangController(new KhachHangService(new DatabaseConnection()));
        this.bookTicketCallback = bookTicketCallback;
        this.username = username;
        this.databaseConnection = new DatabaseConnection();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeComponents();
        loadPhimList("");
    }

    private void initializeComponents() {
        // Content panel (tiêu đề, danh sách phim)
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
        phimCard.setPreferredSize(new Dimension(250, 350));
        phimCard.setBackground(Color.WHITE);

        JLabel posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                ImageIcon posterIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/posters/" + phim.getDuongDanPoster())));
                Image scaledImage = posterIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
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
            int maKhachHang = 0;
            try {
                maKhachHang = khachHangController.getMaKhachHangFromSession(username);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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