package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.cinema.controllers.DatVeController;
import com.cinema.controllers.PaymentController;
import com.cinema.controllers.PhimController;
import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.models.Ghe;
import com.cinema.models.SuatChieu;
import com.cinema.services.GheService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.PermissionManager;
import com.cinema.views.admin.AdminViewManager;
import com.cinema.views.admin.PhimView;
import com.cinema.views.admin.UserManagementView;
import com.cinema.views.login.LoginView;
import com.formdev.flatlaf.FlatLightLaf;

public class MainView extends JFrame {
    private final String username;
    private final LoaiTaiKhoan loaiTaiKhoan;
    private final PermissionManager permissionManager;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private PhimController phimController;
    private final PaymentController paymentController;
    private DatabaseConnection databaseConnection;
    private JPanel sidebarPanel;
    private boolean isSidebarExpanded = true;
    private Map<JButton, String> buttonTextMap = new HashMap<>();
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Font BUTTON_FONT = new Font("Roboto", Font.BOLD, 16);
    private static final Font HEADER_FONT = new Font("Roboto", Font.BOLD, 20);
    private JButton selectedButton; // Biến để lưu nút đang được chọn

    public MainView(String username, LoaiTaiKhoan loaiTaiKhoan) throws IOException, SQLException {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        this.username = username;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.permissionManager = new PermissionManager(loaiTaiKhoan);
        this.paymentController = new PaymentController();

        try {
            databaseConnection = new DatabaseConnection();
            phimController = new PhimController(new PhimView());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể đọc file cấu hình cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Cinema App");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Tối đa hóa cửa sổ
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() throws IOException, SQLException {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CINESTAR_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Cinema App");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel("Xin chào, " + username);
        userLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Roboto", Font.PLAIN, 12));
        logoutButton.setBackground(CINESTAR_YELLOW);
        logoutButton.setForeground(CINESTAR_BLUE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        logoutButton.addActionListener(_ -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
        });
        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(logoutButton);
        headerPanel.add(userPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Sidebar
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(CINESTAR_BLUE);
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Nút thu gọn/mở rộng sidebar
        JButton toggleSidebarButton = createSidebarButton(isSidebarExpanded ? "<<" : ">>");
        toggleSidebarButton.addActionListener(_ -> toggleSidebar());
        sidebarPanel.add(toggleSidebarButton);
        sidebarPanel.add(Box.createVerticalStrut(10));

        // Thêm các nút chức năng vào sidebar
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim()) {
                sidebarPanel.add(createSidebarButton("Quản lý phim", "Phim"));
                sidebarPanel.add(Box.createVerticalStrut(5));
                sidebarPanel.add(createSidebarButton("Quản lý suất chiếu", "Suất chiếu"));
                sidebarPanel.add(Box.createVerticalStrut(5));
            }
            if (permissionManager.isAdmin() || permissionManager.isThuNgan()) {
                sidebarPanel.add(createSidebarButton("Báo cáo doanh thu", "Báo cáo"));
                sidebarPanel.add(Box.createVerticalStrut(5));
            }
            if (permissionManager.isAdmin() || permissionManager.isBanVe()) {
                sidebarPanel.add(createSidebarButton("Quản lý vé", "Vé"));
                sidebarPanel.add(Box.createVerticalStrut(5));
                sidebarPanel.add(createSidebarButton("Bán vé", "Bán vé"));
                sidebarPanel.add(Box.createVerticalStrut(5));
                sidebarPanel.add(createSidebarButton("Quản lý hoá đơn", "Hoá đơn"));
                sidebarPanel.add(Box.createVerticalStrut(5));
            }
            if (permissionManager.isAdmin()) {
                sidebarPanel.add(createSidebarButton("Quản lý người dùng", "Người dùng"));
                sidebarPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            sidebarPanel.add(createSidebarButton("Phim đang chiếu", "Phim"));
            sidebarPanel.add(Box.createVerticalStrut(5));
            sidebarPanel.add(createSidebarButton("Thông tin cá nhân", "Thông tin cá nhân"));
            sidebarPanel.add(Box.createVerticalStrut(5));
        }

        sidebarPanel.add(Box.createVerticalGlue());
        add(sidebarPanel, BorderLayout.WEST);

        // Main content
        mainContentPanel = new JPanel();
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            cardLayout = new CardLayout();
            mainContentPanel.setLayout(cardLayout);
            AdminViewManager adminViewManager = new AdminViewManager(loaiTaiKhoan, mainContentPanel, cardLayout, username);
            adminViewManager.initializeAdminPanels();
            // Thêm panel Quản lý người dùng
            UserManagementView userManagementView = new UserManagementView();
            mainContentPanel.add(userManagementView, "Người dùng");
        } else {
            mainContentPanel.setLayout(new BorderLayout());
            PhimListView phimListView = new PhimListView(phimController, this::openBookingView, username);
            mainContentPanel.add(phimListView, BorderLayout.CENTER);
        }
        mainContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mainContentPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setPreferredSize(new Dimension(0, 50));
        footerPanel.setBackground(CINESTAR_BLUE);
        JLabel footerLabel = new JLabel("© 2025 Cinema App - Liên hệ: contact@cinema.com", SwingConstants.CENTER);
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(CINESTAR_BLUE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) { // Không thay đổi màu nếu nút đã được chọn
                    button.setBackground(CINESTAR_YELLOW);
                    button.setForeground(CINESTAR_BLUE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) { // Giữ màu nếu nút đang được chọn
                    button.setBackground(CINESTAR_BLUE);
                    button.setForeground(Color.WHITE);
                }
            }
        });
        buttonTextMap.put(button, text); // Lưu văn bản gốc
        return button;
    }

    private JButton createSidebarButton(String text, String feature) {
        JButton button = createSidebarButton(text);
        button.addActionListener(_ -> {
            handleMenuSelection(feature, button);
        });
        return button;
    }

    private void toggleSidebar() {
        isSidebarExpanded = !isSidebarExpanded;
        sidebarPanel.setPreferredSize(new Dimension(isSidebarExpanded ? 200 : 60, 0));
        Component[] components = sidebarPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton button) {
                if (button.getText().equals("<<") || button.getText().equals(">>")) {
                    button.setText(isSidebarExpanded ? "<<" : ">>");
                } else {
                    button.setText(isSidebarExpanded ? buttonTextMap.get(button) : ""); // Khôi phục văn bản khi mở rộng
                }
            }
        }
        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }

    private void handleMenuSelection(String feature, JButton button) {
        // Đặt lại màu của nút trước đó
        if (selectedButton != null && selectedButton != button) {
            selectedButton.setBackground(CINESTAR_BLUE);
            selectedButton.setForeground(Color.WHITE);
        }
        // Đặt màu cho nút được chọn
        button.setBackground(CINESTAR_YELLOW);
        button.setForeground(CINESTAR_BLUE);
        selectedButton = button;

        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            try {
                cardLayout.show(mainContentPanel, feature);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi chuyển đổi view: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else if (permissionManager.isUser()) {
            if (feature.equals("Phim đang chiếu") || feature.equals("Đặt vé")) {
                for (Component comp : mainContentPanel.getComponents()) {
                    if (comp instanceof PhimListView) {
                        ((PhimListView) comp).loadPhimList("");
                        break;
                    }
                }
            } else if (feature.equals("Thông tin cá nhân")) {
                UserInfoView userInfoView;
                try {
                    userInfoView = new UserInfoView(this, username);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                userInfoView.setVisible(true);
            }
        }
    }

    private void openBookingView(int maPhim, int maKhachHang) {
        DatVeController datVeController = new DatVeController(
                new SuatChieuService(databaseConnection),
                new GheService(databaseConnection),
                new VeService(databaseConnection)
        );
        BookingView bookingView = new BookingView(this, datVeController, paymentController, maPhim, maKhachHang, bookingResult -> {
            saveDatVe(
                    bookingResult.suatChieu(),
                    bookingResult.ghe(),
                    bookingResult.giaVe(),
                    bookingResult.transactionId()
            );
        });
        bookingView.setVisible(true);
    }

    private void saveDatVe(SuatChieu suatChieu, Ghe ghe, BigDecimal giaVe, String transactionId) {
        try {
            System.out.println("Đã lưu thông tin đặt vé: Suất chiếu - " + suatChieu.getMaSuatChieu() +
                    ", Ghế - " + ghe.getSoGhe() +
                    ", Số tiền - " + giaVe +
                    ", Transaction ID - " + transactionId);
            JOptionPane.showMessageDialog(this, "Đặt vé và thanh toán thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu thông tin đặt vé: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        if (databaseConnection != null) {
            databaseConnection.closeConnection();
        }
        super.dispose();
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void openBookingViewForEmployee(int maPhim, int maKhachHang) {
        openBookingView(maPhim, maKhachHang);
    }
}