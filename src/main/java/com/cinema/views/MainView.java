package com.cinema.views;

import com.cinema.controllers.DatVeController;
import com.cinema.controllers.PhimController;
import com.cinema.models.*;
import com.cinema.services.GheService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class MainView extends JFrame {
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private PhimController phimController;
    private DatabaseConnection databaseConnection;
    private JComboBox<String> theLoaiCombo;
    private JTextField ngayChieuField;
    private JSlider thoiLuongSlider;
    private JPanel phimPanel;
    private final String username;
    private final LoaiTaiKhoan loaiTaiKhoan; // "admin" or "user"

    public MainView(String username, LoaiTaiKhoan loaiTaiKhoan) throws IOException, SQLException {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        this.username = username;
        this.loaiTaiKhoan = loaiTaiKhoan;

        try {
            databaseConnection = new DatabaseConnection();
            phimController = new PhimController(new PhimView());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            System.exit(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        setTitle("Cinema App" + (isAdminRole() ? " - Quản lý" : " - Người dùng"));
        setSize(1280, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        if (!isAdminRole()) {
            loadPhimList();
        }
    }

    private boolean isAdminRole() {
        return loaiTaiKhoan == LoaiTaiKhoan.admin;
    }

    private void initUI() throws IOException, SQLException {
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        mainContentPanel = new JPanel();
        if (isAdminRole()) {
            cardLayout = new CardLayout();
            mainContentPanel.setLayout(cardLayout);
            initializeAdminPanels();
        } else {
            mainContentPanel.setLayout(new BorderLayout());
            mainContentPanel.setBackground(Color.WHITE);
            initializeUserPanels();
        }
        add(mainContentPanel, BorderLayout.CENTER);

        // Footer (for user role only)
        if (!isAdminRole()) {
            JPanel footerPanel = new JPanel();
            footerPanel.setPreferredSize(new Dimension(1280, 50));
            footerPanel.setBackground(new Color(0, 102, 204));
            JLabel footerLabel = new JLabel("© 2025 Cinema App - Liên hệ: contact@cinema.com", SwingConstants.CENTER);
            footerLabel.setForeground(Color.WHITE);
            footerPanel.add(footerLabel);
            add(footerPanel, BorderLayout.SOUTH);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(1280, 80));
        headerPanel.setBackground(new Color(0, 102, 204));

        JLabel logoLabel = new JLabel(isAdminRole() ? "Cinema Management" : "Cinema App");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        menuPanel.setOpaque(false);
        if (isAdminRole()) {
            String[] sections = {"Phim", "Suất chiếu", "Vé", "Nhân viên", "Hoá đơn", "Báo cáo"};
            for (String section : sections) {
                JButton button = new JButton(section);
                button.setForeground(Color.WHITE);
                button.setBackground(new Color(0, 102, 204));
                button.setBorderPainted(false);
                button.addActionListener(_ -> cardLayout.show(mainContentPanel, section));
                menuPanel.add(button);
            }
        } else {
            String[] menus = {"Phim đang chiếu", "Đặt vé", "Thông tin cá nhân"};
            for (String menu : menus) {
                JButton button = new JButton(menu);
                button.setForeground(Color.WHITE);
                button.setBackground(new Color(0, 102, 204));
                button.setBorderPainted(false);
                if (menu.equals("Phim đang chiếu")) {
                    button.addActionListener(_ -> loadPhimList());
                }
                menuPanel.add(button);
            }
        }
        headerPanel.add(menuPanel, BorderLayout.CENTER);

        JPanel userPanel = getUserPanel();
        headerPanel.add(userPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel getUserPanel() {
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel("Xin chào " + username);
        userLabel.setForeground(Color.WHITE);
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(0, 102, 204));
        logoutButton.setBorderPainted(false);
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
            }
        });
        userPanel.add(userLabel);
        userPanel.add(logoutButton);
        return userPanel;
    }

    private void initializeAdminPanels() throws IOException, SQLException {
        mainContentPanel.add(new PhimView(), "Phim");
        mainContentPanel.add(new SuatChieuView(), "Suất chiếu");
        mainContentPanel.add(new VeView(), "Vé");
        mainContentPanel.add(new NhanVienView(), "Nhân viên");
        mainContentPanel.add(new HoaDonView(), "Hoá đơn");
        mainContentPanel.add(new BaoCaoView(), "Báo cáo");
    }

    private void initializeUserPanels() {
        // Sidebar (Bộ lọc)
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBackground(new Color(240, 240, 240));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel filterTitle = new JLabel("Bộ lọc phim");
        filterTitle.setFont(new Font("Arial", Font.BOLD, 16));
        filterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(filterTitle);
        sidebarPanel.add(Box.createVerticalStrut(10));

        JLabel theLoaiLabel = new JLabel("Thể loại:");
        theLoaiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(theLoaiLabel);
        theLoaiCombo = new JComboBox<>(new String[]{"Tất cả", "Hành động", "Tình cảm", "Kinh dị", "Hài hước"});
        theLoaiCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        sidebarPanel.add(theLoaiCombo);
        sidebarPanel.add(Box.createVerticalStrut(10));

        JLabel ngayChieuLabel = new JLabel("Ngày chiếu (yyyy-MM-dd):");
        ngayChieuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(ngayChieuLabel);
        ngayChieuField = new JTextField();
        ngayChieuField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        sidebarPanel.add(ngayChieuField);
        sidebarPanel.add(Box.createVerticalStrut(10));

        JLabel thoiLuongLabel = new JLabel("Thời lượng (phút):");
        thoiLuongLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(thoiLuongLabel);
        thoiLuongSlider = new JSlider(JSlider.HORIZONTAL, 0, 300, 0);
        thoiLuongSlider.setMajorTickSpacing(60);
        thoiLuongSlider.setPaintTicks(true);
        thoiLuongSlider.setPaintLabels(true);
        thoiLuongSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        sidebarPanel.add(thoiLuongSlider);
        sidebarPanel.add(Box.createVerticalStrut(20));

        JButton applyFilterButton = new JButton("Áp dụng bộ lọc");
        applyFilterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyFilterButton.addActionListener(_ -> applyFilters());
        sidebarPanel.add(applyFilterButton);

        // Danh sách phim
        phimPanel = new JPanel(new GridBagLayout());
        mainContentPanel.add(sidebarPanel, BorderLayout.WEST);
        mainContentPanel.add(new JScrollPane(phimPanel), BorderLayout.CENTER);
    }

    private void loadPhimList() {
        phimPanel.removeAll();
        List<Phim> phimList = phimController.getAllPhimDetail();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
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

    private void applyFilters() {
        phimPanel.removeAll();
        List<Phim> phimList = phimController.getAllPhimDetail();

        String selectedTheLoai = (String) theLoaiCombo.getSelectedItem();
        if (!"Tất cả".equals(selectedTheLoai)) {
            phimList = phimList.stream()
                    .filter(phim -> phim.getTenTheLoai().equalsIgnoreCase(selectedTheLoai))
                    .collect(Collectors.toList());
        }

        String ngayChieuText = ngayChieuField.getText().trim();
        if (!ngayChieuText.isEmpty()) {
            try {
                LocalDate ngayChieu = LocalDate.parse(ngayChieuText);
                phimList = phimList.stream()
                        .filter(phim -> phim.getNgayKhoiChieu().equals(ngayChieu))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ngày chiếu không hợp lệ (yyyy-MM-dd)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                loadPhimList();
                return;
            }
        }

        int maxThoiLuong = thoiLuongSlider.getValue();
        if (maxThoiLuong > 0) {
            phimList = phimList.stream()
                    .filter(phim -> phim.getThoiLuong() <= maxThoiLuong)
                    .collect(Collectors.toList());
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
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
        JPanel phimCard = new JPanel(new BorderLayout());
        phimCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JLabel phimLabel = new JLabel("<html><b>" + phim.getTenPhim() + "</b><br>" +
                phim.getTenTheLoai() + " | " + phim.getThoiLuong() + " phút</html>", SwingConstants.CENTER);
        phimCard.add(phimLabel, BorderLayout.CENTER);
        JButton datVeButton = new JButton("Đặt vé");
        datVeButton.addActionListener(_ -> datVe(phim.getMaPhim()));
        phimCard.add(datVeButton, BorderLayout.SOUTH);
        return phimCard;
    }

    private void datVe(int maPhim) {
        try {
            DatVeController datVeController = new DatVeController(
                    new SuatChieuService(databaseConnection),
                    new GheService(databaseConnection),
                    new VeService(databaseConnection)
            );
            JDialog dialog = new JDialog(this, "Đặt vé", true);
            dialog.setSize(600, 400);
            dialog.setLayout(new BorderLayout());

            JPanel suatChieuPanel = new JPanel(new FlowLayout());
            JLabel suatChieuLabel = new JLabel("Chọn suất chiếu:");
            JComboBox<SuatChieu> suatChieuCombo = new JComboBox<>();
            List<SuatChieu> suatChieuList = datVeController.getSuatChieuByPhim(maPhim);
            for (SuatChieu sc : suatChieuList) {
                suatChieuCombo.addItem(sc);
            }
            suatChieuPanel.add(suatChieuLabel);
            suatChieuPanel.add(suatChieuCombo);

            JPanel ghePanel = new JPanel(new FlowLayout());
            JLabel gheLabel = new JLabel("Chọn ghế:");
            JComboBox<Ghe> gheCombo = new JComboBox<>();
            suatChieuCombo.addActionListener(_ -> {
                gheCombo.removeAllItems();
                SuatChieu selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
                if (selectedSuatChieu != null) {
                    try {
                        List<Ghe> gheList = datVeController.getGheTrongByPhongAndSuatChieu(
                                selectedSuatChieu.getMaPhong(), selectedSuatChieu.getMaSuatChieu());
                        for (Ghe ghe : gheList) {
                            gheCombo.addItem(ghe);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                                "Lỗi khi tải danh sách ghế: " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            ghePanel.add(gheLabel);
            ghePanel.add(gheCombo);

            JButton confirmButton = new JButton("Xác nhận đặt vé");
            confirmButton.addActionListener(_ -> {
                SuatChieu selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
                Ghe selectedGhe = (Ghe) gheCombo.getSelectedItem();
                if (selectedSuatChieu == null || selectedGhe == null) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng chọn suất chiếu và ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    BigDecimal giaVe = new BigDecimal("50000");
                    datVeController.datVe(
                            selectedSuatChieu.getMaSuatChieu(),
                            selectedGhe.getMaPhong(),
                            selectedGhe.getSoGhe(),
                            giaVe
                    );
                    JOptionPane.showMessageDialog(dialog, "Đặt vé thành công!");
                    dialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Lỗi khi đặt vé!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            dialog.add(suatChieuPanel, BorderLayout.NORTH);
            dialog.add(ghePanel, BorderLayout.CENTER);
            dialog.add(confirmButton, BorderLayout.SOUTH);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        if (databaseConnection != null) {
            databaseConnection.closeConnection();
        }
        super.dispose();
    }
}