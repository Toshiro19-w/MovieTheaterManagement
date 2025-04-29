package com.cinema.views;

import com.cinema.controllers.DatVeController;
import com.cinema.controllers.PhimController;
import com.cinema.models.*;
import com.cinema.services.GheService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainView extends JFrame {
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private PhimController phimController;
    private DatabaseConnection databaseConnection;
    private JComboBox<String> theLoaiCombo;
    private JDateChooser ngayChieuField;
    private JSlider thoiLuongSlider;
    private JPanel phimPanel;
    private final String username;
    private final LoaiTaiKhoan loaiTaiKhoan;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        return loaiTaiKhoan == LoaiTaiKhoan.ADMIN;
    }

    private void initUI() throws IOException, SQLException {
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

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
            String[] sections = {"Phim", "Suất chiếu", "Phòng chiếu", "Vé", "Nhân viên", "Hoá đơn", "Báo cáo"};
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
                if (menu.equals("Đặt vé")) {
                	button.addActionListener(_ -> loadPhimList());
                }
                if (menu.equals("Thông tin cá nhân")) {
                	
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
        mainContentPanel.add(new PhongChieuView(), "Phòng chiếu");
        mainContentPanel.add(new VeView(), "Vé");
        mainContentPanel.add(new NhanVienView(), "Nhân viên");
        mainContentPanel.add(new HoaDonView(), "Hoá đơn");
        mainContentPanel.add(new BaoCaoView(), "Báo cáo");
    }

    private void initializeUserPanels() {
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

        JLabel ngayChieuLabel = new JLabel("Ngày chiếu:");
        ngayChieuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(ngayChieuLabel);

        // Thay JTextField bằng JDateChooser
        ngayChieuField = new JDateChooser();
        ngayChieuField.setDateFormatString("yyyy-MM-dd");
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

        phimPanel = new JPanel(new GridBagLayout());
        mainContentPanel.add(sidebarPanel, BorderLayout.WEST);
        mainContentPanel.add(new JScrollPane(phimPanel), BorderLayout.CENTER);
    }

    private void loadPhimList() {
        phimPanel.removeAll();
        List<Phim> phimList;
        try {
            phimList = phimController.getAllPhimDetail();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phim!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
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
    
    private void loadThongtinKH() {
 	   phimPanel.removeAll();
 	   NguoiDung ND = new NguoiDung();
 	   KhachHang kh = new KhachHang();
 	   try {
 		   
 	   }catch (Exception e) {
		// TODO: handle exception
	}
 	   return;
    }
    

    private void applyFilters() {
        phimPanel.removeAll();
        List<Phim> phimList;
        try {
            phimList = phimController.getAllPhimDetail();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phim!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedTheLoai = (String) theLoaiCombo.getSelectedItem();
        if (!"Tất cả".equals(selectedTheLoai)) {
            phimList = phimList.stream()
                    .filter(phim -> phim.getTenTheLoai().equalsIgnoreCase(selectedTheLoai))
                    .collect(Collectors.toList());
        }

        Date date = ngayChieuField.getDate();
        if (date != null) {
            LocalDate ngayChieu = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            phimList = phimList.stream()
                    .filter(phim -> phim.getNgayKhoiChieu() != null && phim.getNgayKhoiChieu().equals(ngayChieu))
                    .collect(Collectors.toList());

            if (phimList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có phim nào khởi chiếu vào ngày này!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadPhimList(); // hoặc bạn reset lại list gốc nếu muốn
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
        JPanel phimCard = new JPanel(new BorderLayout(5, 5));
        phimCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        phimCard.setPreferredSize(new Dimension(200, 350));

        // Thêm ảnh poster
        JLabel posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                ImageIcon posterIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("images/posters/" + phim.getDuongDanPoster())));
                Image scaledImage = posterIcon.getImage().getScaledInstance(200, 250, Image.SCALE_SMOOTH);
                posterLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                e.printStackTrace();
                posterLabel.setText("Không có ảnh");
            }
        } else {
            posterLabel.setText("Không có ảnh");
        }
        phimCard.add(posterLabel, BorderLayout.NORTH);

        // Thông tin phim
        JLabel phimLabel = new JLabel("<html><center><b>" + phim.getTenPhim() + "</b><br>" +
                phim.getTenTheLoai() + " | " + phim.getThoiLuong() + " phút</center></html>", SwingConstants.CENTER);
        phimCard.add(phimLabel, BorderLayout.CENTER);

        // Nút đặt vé
        JButton datVeButton = new JButton("Đặt vé");
        datVeButton.addActionListener(_ -> datVe(phim.getMaPhim()));
        phimCard.add(datVeButton, BorderLayout.SOUTH);

        // Thêm sự kiện nhấn chuột để hiển thị chi tiết phim
        phimCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Chỉ mở chi tiết phim nếu không nhấn vào nút "Đặt vé"
                if (e.getSource() == phimCard && !datVeButton.getBounds().contains(e.getPoint())) {
                    showPhimDetail(phim);
                }
            }
        });

        return phimCard;
    }

    private void showPhimDetail(Phim phim) {
        JDialog detailDialog = new JDialog(this, "Chi tiết phim: " + phim.getTenPhim(), true);
        detailDialog.setSize(600, 500);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setLocationRelativeTo(this);

        // Panel chứa ảnh poster
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

        // Panel chứa thông tin chi tiết
        JPanel infoPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        // Kết hợp ảnh và thông tin
        JPanel mainDetailPanel = new JPanel(new BorderLayout(10, 10));
        mainDetailPanel.add(detailPosterLabel, BorderLayout.WEST);
        mainDetailPanel.add(infoPanel, BorderLayout.CENTER);

        // Nút đặt vé trong cửa sổ chi tiết
        JButton datVeButton = new JButton("Đặt vé");
        datVeButton.addActionListener(_ -> {
            detailDialog.dispose();
            datVe(phim.getMaPhim());
        });

        detailDialog.add(mainDetailPanel, BorderLayout.CENTER);
        detailDialog.add(datVeButton, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }

    private void datVe(int maPhim) {
        try {
            DatVeController datVeController = new DatVeController(
                    new SuatChieuService(databaseConnection),
                    new GheService(databaseConnection),
                    new VeService(databaseConnection)
            );
            JDialog dialog = new JDialog(this, "Đặt vé", true);
            dialog.setSize(800, 600);
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

            // Panel sơ đồ ghế
            JPanel seatPanel = new JPanel(new GridLayout(5, 10, 5, 5));
            seatPanel.setBorder(BorderFactory.createTitledBorder("Sơ đồ ghế"));
            JLabel selectedSeatLabel = new JLabel("Ghế đã chọn: None");
            Ghe[] selectedGhe = {null};

            // Cập nhật sơ đồ ghế khi chọn suất chiếu
            suatChieuCombo.addActionListener(_ -> {
                seatPanel.removeAll();
                selectedGhe[0] = null;
                selectedSeatLabel.setText("Ghế đã chọn: None");
                SuatChieu selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
                if (selectedSuatChieu != null) {
                    try {
                        List<Ghe> gheList = datVeController.getGheTrongByPhongAndSuatChieu(
                                selectedSuatChieu.getMaPhong(), selectedSuatChieu.getMaSuatChieu());
                        String[] allSeats = new String[50];
                        for (int i = 0; i < 5; i++) {
                            for (int j = 1; j <= 10; j++) {
                                allSeats[i * 10 + j - 1] = (char) ('A' + i) + String.valueOf(j);
                            }
                        }

                        for (String seat : allSeats) {
                            JButton seatButton = new JButton(seat);
                            seatButton.setPreferredSize(new Dimension(50, 50));
                            boolean isAvailable = gheList.stream().anyMatch(ghe -> ghe.getSoGhe().equals(seat));
                            if (isAvailable) {
                                seatButton.setBackground(Color.GREEN);
                                seatButton.addActionListener(_ -> {
                                    if (selectedGhe[0] != null) {
                                        for (Component comp : seatPanel.getComponents()) {
                                            if (comp instanceof JButton && ((JButton) comp).getText().equals(selectedGhe[0].getSoGhe())) {
                                                comp.setBackground(Color.GREEN);
                                                break;
                                            }
                                        }
                                    }
                                    selectedGhe[0] = gheList.stream()
                                            .filter(ghe -> ghe.getSoGhe().equals(seat))
                                            .findFirst()
                                            .orElse(null);
                                    seatButton.setBackground(Color.YELLOW);
                                    selectedSeatLabel.setText("Ghế đã chọn: " + seat);
                                });
                            } else {
                                seatButton.setBackground(Color.RED);
                                seatButton.setEnabled(false);
                            }
                            seatPanel.add(seatButton);
                        }
                        seatPanel.revalidate();
                        seatPanel.repaint();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                                "Lỗi khi tải danh sách ghế: " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // Panel hiển thị ghế đã chọn và nút xác nhận
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(selectedSeatLabel, BorderLayout.NORTH);

            JButton confirmButton = new JButton("Xác nhận đặt vé");
            confirmButton.addActionListener(_ -> {
                SuatChieu selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
                if (selectedSuatChieu == null || selectedGhe[0] == null) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng chọn suất chiếu và ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    BigDecimal giaVe = new BigDecimal("50000");
                    datVeController.datVe(
                            selectedSuatChieu.getMaSuatChieu(),
                            selectedGhe[0].getMaPhong(),
                            selectedGhe[0].getSoGhe(),
                            giaVe
                    );
                    JOptionPane.showMessageDialog(dialog, "Đặt vé thành công!");
                    dialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Lỗi khi đặt vé: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            bottomPanel.add(confirmButton, BorderLayout.SOUTH);

            dialog.add(suatChieuPanel, BorderLayout.NORTH);
            dialog.add(new JScrollPane(seatPanel), BorderLayout.CENTER);
            dialog.add(bottomPanel, BorderLayout.SOUTH);
            dialog.setLocationRelativeTo(this);

            if (suatChieuCombo.getItemCount() > 0) {
                suatChieuCombo.setSelectedIndex(0);
            }
            dialog.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu đặt vé!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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