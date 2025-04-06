package com.cinema.views;

import com.cinema.controllers.DatVeController;
import com.cinema.controllers.PhimController;
import com.cinema.models.Ghe;
import com.cinema.models.Phim;
import com.cinema.models.SuatChieu;
import com.cinema.services.GheService;
import com.cinema.services.PhimService;
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

public class NguoiDungView extends JFrame {
    private PhimController controller;
    private JPanel phimPanel;
    private DatabaseConnection databaseConnection;
    private JComboBox<String> theLoaiCombo;
    private JTextField ngayChieuField;
    private JSlider thoiLuongSlider;
    private final String username;

    public NguoiDungView(String username) {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        try {
            databaseConnection = new DatabaseConnection();
            controller = new PhimController(new PhimService(databaseConnection));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!");
            System.exit(1);
        }

        this.username = username;
        initUI();
        loadPhimList();
    }

    private void initUI() {
        setTitle("Cinema App - Người dùng");
        setSize(1280, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header
        JPanel headerPanel = getJPanel();

        // Main Content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Sidebar (Bộ lọc)
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBackground(new Color(240, 240, 240));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tiêu đề Sidebar
        JLabel filterTitle = new JLabel("Bộ lọc phim");
        filterTitle.setFont(new Font("Arial", Font.BOLD, 16));
        filterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(filterTitle);
        sidebarPanel.add(Box.createVerticalStrut(10));

        // Bộ lọc theo thể loại
        JLabel theLoaiLabel = new JLabel("Thể loại:");
        theLoaiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(theLoaiLabel);
        theLoaiCombo = new JComboBox<>(new String[]{"Tất cả", "Hành động", "Tình cảm", "Kinh dị", "Hài hước"});
        theLoaiCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        sidebarPanel.add(theLoaiCombo);
        sidebarPanel.add(Box.createVerticalStrut(10));

        // Bộ lọc theo ngày chiếu
        JLabel ngayChieuLabel = new JLabel("Ngày chiếu (yyyy-MM-dd):");
        ngayChieuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(ngayChieuLabel);
        ngayChieuField = new JTextField();
        ngayChieuField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        sidebarPanel.add(ngayChieuField);
        sidebarPanel.add(Box.createVerticalStrut(10));

        // Bộ lọc theo thời lượng
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

        // Nút áp dụng bộ lọc
        JButton applyFilterButton = new JButton("Áp dụng bộ lọc");
        applyFilterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyFilterButton.addActionListener(_ -> applyFilters());
        sidebarPanel.add(applyFilterButton);

        // Danh sách phim
        phimPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Thêm Sidebar và danh sách phim vào mainPanel
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(new JScrollPane(phimPanel), BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setPreferredSize(new Dimension(1280, 50));
        footerPanel.setBackground(new Color(0, 102, 204));
        JLabel footerLabel = new JLabel("© 2023 Cinema App - Liên hệ: contact@cinema.com", SwingConstants.CENTER);
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);

        // Thêm vào JFrame
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel getJPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(1280, 80));
        headerPanel.setBackground(new Color(0, 102, 204));
        JLabel logoLabel = new JLabel("Cinema App");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        menuPanel.setOpaque(false);
        String[] menus = {"Phim đang chiếu", "Đặt vé", "Lịch sử vé", "Thông tin cá nhân"};
        for (String menu : menus) {
            JButton button = new JButton(menu);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(0, 102, 204));
            button.setBorderPainted(false);
            menuPanel.add(button);
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

    private void loadPhimList() {
        phimPanel.removeAll();
        List<Phim> phimList = controller.findAllDetail();
        for (Phim phim : phimList) {
            JPanel phimCard = getPhimCard(phim);
            phimPanel.add(phimCard);
        }
        phimPanel.revalidate();
        phimPanel.repaint();
    }

    private void applyFilters() {
        phimPanel.removeAll();
        List<Phim> phimList = controller.findAllDetail();

        // Lọc theo thể loại
        String selectedTheLoai = (String) theLoaiCombo.getSelectedItem();
        if (!"Tất cả".equals(selectedTheLoai)) {
            phimList = phimList.stream()
                    .filter(phim -> phim.getTenTheLoai().equalsIgnoreCase(selectedTheLoai))
                    .collect(Collectors.toList());
        }

        // Lọc theo ngày chiếu
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

        // Lọc theo thời lượng
        int maxThoiLuong = thoiLuongSlider.getValue();
        if (maxThoiLuong > 0) {
            phimList = phimList.stream()
                    .filter(phim -> phim.getThoiLuong() <= maxThoiLuong)
                    .collect(Collectors.toList());
        }

        // Hiển thị danh sách phim đã lọc
        for (Phim phim : phimList) {
            JPanel phimCard = getPhimCard(phim);
            phimPanel.add(phimCard);
        }
        phimPanel.revalidate();
        phimPanel.repaint();
    }

    private JPanel getPhimCard(Phim phim) {
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

            // Panel chọn suất chiếu
            JPanel suatChieuPanel = new JPanel(new FlowLayout());
            JLabel suatChieuLabel = new JLabel("Chọn suất chiếu:");
            JComboBox<SuatChieu> suatChieuCombo = new JComboBox<>();
            List<SuatChieu> suatChieuList = datVeController.getSuatChieuByPhim(maPhim);
            for (SuatChieu sc : suatChieuList) {
                suatChieuCombo.addItem(sc);
            }
            suatChieuPanel.add(suatChieuLabel);
            suatChieuPanel.add(suatChieuCombo);

            // Panel chọn ghế
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
                        System.out.println("So ghe trong: " + gheList.size());
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

            // Nút xác nhận
            JButton confirmButton = new JButton("Xác nhận đặt vé");
            confirmButton.addActionListener(_ -> {
                SuatChieu selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
                Ghe selectedGhe = (Ghe) gheCombo.getSelectedItem();
                if (selectedSuatChieu == null || selectedGhe == null) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng chọn suất chiếu và ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    BigDecimal giaVe = new BigDecimal("50000"); // Giả định giá vé cố định
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
        databaseConnection.closeConnection();
        super.dispose();
    }
}