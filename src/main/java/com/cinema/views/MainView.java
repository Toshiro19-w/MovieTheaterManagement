package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
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
    private Map<JButton, String> buttonTextMap = new HashMap<>();
    private Map<String, ImageIcon> menuIcons = new HashMap<>();
    
    // Màu sắc cải tiến
    private static final Color SIDEBAR_COLOR = new Color(248, 249, 250);
    private static final Color SELECTED_COLOR = new Color(79, 70, 229); // Indigo
    private static final Color HOVER_COLOR = new Color(224, 231, 255); // Light indigo
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
    private static final Color HEADER_COLOR = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = new Color(31, 41, 55);
    private static final Color ACCENT_COLOR = new Color(99, 102, 241);
    
    // Font cải tiến
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    
    private JButton selectedButton;

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

        setTitle("CinemaHub");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        
        // Đặt biểu tượng cho cửa sổ ứng dụng
        com.cinema.utils.AppIconUtils.setAppIcon(this);
        
        loadMenuIcons();
        initUI();
    }
    
    private void loadMenuIcons() {
        // Tải các biểu tượng cho menu
        loadIcon("Dashboard", "/images/Icon/dashboard.png", "📊");
        loadIcon("Phim", "/images/Icon/movie.png", "🎬");
        loadIcon("Suất chiếu", "/images/Icon/schedule.png", "⏰");
        loadIcon("Bán vé", "/images/Icon/sell_ticket.png", "🎫");
        loadIcon("Vé", "/images/Icon/ticket.png", "🎟️");
        loadIcon("Hoá đơn", "/images/Icon/invoice.png", "📝");
        loadIcon("Báo cáo", "/images/Icon/report.png", "📈");
        loadIcon("Nhân viên", "/images/Icon/staff.png", "👨‍💼");
        loadIcon("Người dùng", "/images/Icon/user.png", "👥");
        loadIcon("Đặt vé", "/images/Icon/booking.png", "🎟️");
        loadIcon("Thông tin cá nhân", "/images/Icon/profile.png", "👤");
        loadIcon("Đăng xuất", "/images/Icon/logout.png", "🚪");
    }
    
    private void loadIcon(String key, String path, String fallback) {
        try {
            ImageIcon icon = new ImageIcon(MainView.class.getResource(path));
            if (icon.getIconWidth() <= 0) {
                // Nếu không tìm thấy biểu tượng, sử dụng biểu tượng mặc định
                menuIcons.put(key, createTextIcon(fallback));
            } else {
                // Thay đổi kích thước biểu tượng
                Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                menuIcons.put(key, new ImageIcon(img));
            }
        } catch (Exception e) {
            // Nếu có lỗi, sử dụng biểu tượng mặc định
            menuIcons.put(key, createTextIcon(fallback));
        }
    }
    
    private ImageIcon createTextIcon(String text) {
        // Tạo biểu tượng từ văn bản Unicode
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(TEXT_COLOR);
        label.setSize(20, 20);
        
        BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        label.paint(g2d);
        g2d.dispose();
        
        return new ImageIcon(image);
    }

    // Phương thức để lấy logo từ lớp tiện ích
    public static JLabel getAppLogo() {
        return com.cinema.utils.AppIconUtils.getAppLogo();
    }

    private void initUI() throws IOException, SQLException {
        setLayout(new BorderLayout());

        // Sidebar với thiết kế cải tiến
        sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ đường viền mềm bên phải
                g2d.setColor(new Color(226, 232, 240));
                g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2d.dispose();
            }
        };
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBorder(new EmptyBorder(15, 10, 10, 10));

        // Logo và tiêu đề ứng dụng trong sidebar với thiết kế nổi bật hơn
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(5, 5, 15, 5));
        
        // Panel chứa logo và tên ứng dụng
        JPanel logoTitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel logoLabel = getAppLogo();
        logoTitlePanel.add(logoLabel);
        
        JLabel appTitle = new JLabel("CinemaHub");
        appTitle.setFont(TITLE_FONT);
        appTitle.setForeground(SELECTED_COLOR);
        logoTitlePanel.add(appTitle);
        
        logoPanel.add(logoTitlePanel);
        
        // Thêm đường kẻ phân cách dưới logo
        JSeparator logoSeparator = new JSeparator();
        logoSeparator.setForeground(new Color(226, 232, 240));
        logoSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(logoSeparator);
        
        sidebarPanel.add(logoPanel, BorderLayout.NORTH);

        // Sidebar Menu với thiết kế cải tiến
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(SIDEBAR_COLOR);
        menuPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Thêm Dashboard cho tất cả nhân viên
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            menuPanel.add(createSidebarButton("Dashboard", "Dashboard"));
            menuPanel.add(Box.createVerticalStrut(5));
            
            // Quản lý phim và suất chiếu
            if (permissionManager.hasPermission("Phim")) {
                menuPanel.add(createSidebarButton("Quản lý Phim", "Phim"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
            
            if (permissionManager.hasPermission("Suất chiếu")) {
                menuPanel.add(createSidebarButton("Quản lý Suất chiếu", "Suất chiếu"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
            
            // Bán vé
            if (permissionManager.hasPermission("Bán vé")) {
                menuPanel.add(createSidebarButton("Bán vé", "Bán vé"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
            
            // Quản lý vé
            if (permissionManager.hasPermission("Vé")) {
                menuPanel.add(createSidebarButton("Quản lý Vé", "Vé"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
            
            // Hóa đơn
            if (permissionManager.hasPermission("Hoá đơn")) {
                menuPanel.add(createSidebarButton("Quản lý Hóa đơn", "Hoá đơn"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
            
            // Báo cáo & Thống kê
            if (permissionManager.hasPermission("Báo cáo")) {
                menuPanel.add(createSidebarButton("Báo cáo & Thống kê", "Báo cáo"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
            
            // Quản lý nhân viên
            if (permissionManager.hasPermission("Nhân viên")) {
                menuPanel.add(createSidebarButton("Quản lý Nhân viên", "Nhân viên"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
            
            // Quản lý người dùng (khách hàng)
            if (permissionManager.isAdmin()) {
                menuPanel.add(createSidebarButton("Quản lý Người dùng", "Người dùng"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
        } else if (permissionManager.isUser()) {
            // Menu cho khách hàng
            menuPanel.add(createSidebarButton("Phim đang chiếu", "Phim"));
            menuPanel.add(Box.createVerticalStrut(5));
            
            if (permissionManager.hasPermission("Đặt vé")) {
                menuPanel.add(createSidebarButton("Đặt vé", "Đặt vé"));
                menuPanel.add(Box.createVerticalStrut(5));
            }
            
            menuPanel.add(createSidebarButton("Thông tin cá nhân", "Thông tin cá nhân"));
            menuPanel.add(Box.createVerticalStrut(5));
        }

        // User Profile Section cải tiến
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBackground(SIDEBAR_COLOR);
        userPanel.setBorder(new EmptyBorder(20, 10, 10, 10));

        // Separator trước user profile
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(226, 232, 240));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        userPanel.add(separator);
        userPanel.add(Box.createVerticalStrut(15));

        // User info panel
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(SIDEBAR_COLOR);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel userLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        userLabelPanel.setOpaque(false);
        userLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Sử dụng biểu tượng người dùng
        JLabel iconLabel = new JLabel(menuIcons.getOrDefault("Người dùng", createTextIcon("👤")));
        userLabelPanel.add(iconLabel);
        
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        userLabelPanel.add(usernameLabel);
        
        userInfoPanel.add(userLabelPanel);

        JLabel userRole = new JLabel(loaiTaiKhoan.toString());
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userRole.setForeground(new Color(107, 114, 128));
        userRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        userInfoPanel.add(userRole);

        userPanel.add(userInfoPanel);
        userPanel.add(Box.createVerticalStrut(10));

        // Logout button với biểu tượng
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoutPanel.setOpaque(false);
        
        // Lấy biểu tượng đăng xuất
        ImageIcon logoutIcon = menuIcons.getOrDefault("Đăng xuất", createTextIcon("🚪"));
        JLabel logoutIconLabel = new JLabel(logoutIcon);
        JLabel logoutTextLabel = new JLabel("Đăng xuất");
        logoutTextLabel.setFont(BUTTON_FONT);
        logoutTextLabel.setForeground(Color.WHITE);
        
        logoutPanel.add(logoutIconLabel);
        logoutPanel.add(logoutTextLabel);
        
        JButton logoutButton = new JButton();
        logoutButton.setLayout(new BorderLayout());
        logoutButton.add(logoutPanel, BorderLayout.CENTER);
        logoutButton.setBackground(new Color(220, 38, 38));
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.addActionListener(_ -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    new LoginView().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        
        userPanel.add(logoutButton);

        sidebarPanel.add(menuPanel, BorderLayout.CENTER);
        sidebarPanel.add(userPanel, BorderLayout.SOUTH);

        add(sidebarPanel, BorderLayout.WEST);

        // Create a container for header and main content
        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(BACKGROUND_COLOR);

        // Header cải tiến
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ đường viền mềm bên dưới
                g2d.setColor(new Color(226, 232, 240));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2d.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(permissionManager.isAdmin() ? "Quản lý hệ thống" : "Hệ thống đặt vé");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        
        // Nút cài đặt cải tiến
        JButton settingsButton = new JButton("Cài đặt");
        settingsButton.setFont(BUTTON_FONT);
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setBackground(ACCENT_COLOR);
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.setFocusPainted(false);
        settingsButton.setBorder(new EmptyBorder(8, 15, 8, 15));
        settingsButton.addActionListener(_ -> {
            // Hiển thị dialog cài đặt
            JOptionPane.showMessageDialog(this, 
                "Tính năng cài đặt đang được phát triển", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        actionPanel.add(settingsButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        contentContainer.add(headerPanel, BorderLayout.NORTH);

        // Main content với thiết kế cải tiến
        mainContentPanel = new JPanel();
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            cardLayout = new CardLayout();
            mainContentPanel.setLayout(cardLayout);
            AdminViewManager adminViewManager = new AdminViewManager(loaiTaiKhoan, mainContentPanel, cardLayout, username);
            adminViewManager.initializeAdminPanels();

            DashboardView dashboardView = new DashboardView();
            mainContentPanel.add(dashboardView, "Dashboard");

            cardLayout.show(mainContentPanel, "Dashboard");
            UserManagementView userManagementView = new UserManagementView();
            mainContentPanel.add(userManagementView, "Người dùng");
        } else {
            mainContentPanel.setLayout(new BorderLayout());
            PhimListView phimListView = new PhimListView(phimController, this::openBookingView, username);
            mainContentPanel.add(phimListView, BorderLayout.CENTER);
        }
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentContainer.add(mainContentPanel, BorderLayout.CENTER);

        add(contentContainer, BorderLayout.CENTER);
    }

    private JButton createSidebarButton(String text, String feature) {
        // Lấy biểu tượng cho nút
        ImageIcon icon = menuIcons.getOrDefault(feature, createTextIcon("•"));
        
        // Tạo panel để chứa biểu tượng và văn bản
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(BUTTON_FONT);
        
        buttonPanel.add(iconLabel);
        buttonPanel.add(textLabel);
        
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (this == selectedButton) {
                    // Vẽ nền với góc bo tròn cho nút được chọn
                    g2d.setColor(SELECTED_COLOR);
                    g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    textLabel.setForeground(Color.WHITE);
                } else if (getModel().isRollover()) {
                    // Vẽ nền với góc bo tròn cho hover
                    g2d.setColor(HOVER_COLOR);
                    g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    textLabel.setForeground(TEXT_COLOR);
                } else {
                    textLabel.setForeground(TEXT_COLOR);
                }
                
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        
        button.setLayout(new BorderLayout());
        button.add(buttonPanel, BorderLayout.CENTER);
        
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Lưu trữ tên tính năng để sử dụng khi xử lý sự kiện
        buttonTextMap.put(button, feature);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) {
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) {
                    button.repaint();
                }
            }
        });
        
        button.addActionListener(_ -> handleMenuSelection(feature, button));
        return button;
    }

    private void handleMenuSelection(String feature, JButton button) {
        if (selectedButton != null && selectedButton != button) {
            selectedButton.repaint();
        }
        
        selectedButton = button;
        button.repaint();

        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            try {
                // Kiểm tra xem người dùng có quyền truy cập tính năng này không
                if (permissionManager.hasPermission(feature) || feature.equals("Dashboard")) {
                    cardLayout.show(mainContentPanel, feature);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Bạn không có quyền truy cập tính năng này!", 
                        "Cảnh báo", 
                        JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi chuyển đổi view: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else if (permissionManager.isUser()) {
            if (feature.equals("Phim") || feature.equals("Đặt vé")) {
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