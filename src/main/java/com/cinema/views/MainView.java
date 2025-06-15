package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.cinema.components.ThemeToggleButton;

import com.cinema.components.IconManager;
import com.cinema.components.ModernUIApplier;
import com.cinema.components.ThemeToggleButton;
import com.cinema.components.UIConstants;
import com.cinema.components.UIHelper;
import com.cinema.components.UITheme;
import com.cinema.components.theme.Theme;
import com.cinema.components.theme.ThemeManager;
import com.cinema.components.theme.ThemeableComponent;
import com.cinema.controllers.MainViewController;
import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.utils.DatabaseConnection;
import com.cinema.views.admin.ResponsiveScrollPane;
import com.cinema.views.common.ResizableView;
import com.cinema.views.sidebar.SidebarMenuItem;
import com.formdev.flatlaf.FlatLightLaf;

public class MainView extends JFrame implements ThemeableComponent {
    private final MainViewController controller;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private List<SidebarMenuItem> menuItems = new ArrayList<>();
    private SidebarMenuItem selectedMenuItem;
    private JScrollPane contentScrollPane;
    
    // Các biến UI cần thiết cho updateTheme
    private JPanel userPanel;
    private JPanel userInfoPanel;
    private JPanel contentContainer;
    private JPanel centeringPanel;
    private JPanel headerPanel;
    private JPanel logoPanel;
    private JLabel appTitle;
    private JLabel titleLabel;
    private JLabel usernameLabel;
    private JLabel userRole;

    public MainView(String username, LoaiTaiKhoan loaiTaiKhoan) throws IOException, SQLException {
        // Đăng ký listener để lắng nghe sự kiện thay đổi theme
        ThemeManager.getInstance().addThemeChangeListener((oldTheme, newTheme) -> {
            updateTheme(newTheme);
        });
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Set default UI properties
            UIManager.put("Button.arc", UITheme.BUTTON_RADIUS);
            UIManager.put("Component.arc", UITheme.BUTTON_RADIUS);
            UIManager.put("Panel.arc", UITheme.BUTTON_RADIUS);
            UIManager.put("TextComponent.arc", UITheme.BUTTON_RADIUS);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            UIManager.put("Button.margin", new java.awt.Insets(8, 14, 8, 14));
            UIManager.put("TabbedPane.contentOpaque", false);
            UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
            UIManager.put("TabbedPane.showTabSeparators", true);
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // Initialize controller
        this.controller = new MainViewController(this, username, loaiTaiKhoan);

        setTitle("CinemaHub");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(UITheme.BACKGROUND_COLOR);
        
        // Set app icon
        com.cinema.utils.AppIconUtils.setAppIcon(this);
        
        initUI();
    }
    
    private void initUI() throws IOException, SQLException {
        setLayout(new BorderLayout());

        // Sidebar with improved design - fixed size for admin users only
        sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw soft border on the right
                g2d.setColor(new Color(226, 232, 240));
                g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2d.dispose();
            }
            
            // Ensure sidebar always maintains fixed size
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(UITheme.SIDEBAR_WIDTH, super.getPreferredSize().height);
            }
            
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(UITheme.SIDEBAR_WIDTH, super.getMinimumSize().height);
            }
            
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(UITheme.SIDEBAR_WIDTH, super.getMaximumSize().height);
            }
        };
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setBackground(UITheme.SIDEBAR_COLOR);
        sidebarPanel.setBorder(new EmptyBorder(15, 10, 10, 10));

        // Logo and app title in sidebar with more prominent design
        logoPanel = UIHelper.createVerticalBoxPanel();
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(5, 5, 15, 5));
        
        // Panel containing logo and app name
        JPanel logoTitlePanel = UIHelper.createFlowPanel(FlowLayout.CENTER, 10, 5);
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel logoLabel = getAppLogo();
        logoTitlePanel.add(logoLabel);
        
        appTitle = new JLabel("CinemaHub");
        appTitle.setFont(UITheme.TITLE_FONT);
        appTitle.setForeground(UITheme.SELECTED_COLOR);
        logoTitlePanel.add(appTitle);
        
        logoPanel.add(logoTitlePanel);
        
        // Add separator below logo
        JSeparator logoSeparator = new JSeparator();
        logoSeparator.setForeground(new Color(226, 232, 240));
        logoSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(logoSeparator);
        
        sidebarPanel.add(logoPanel, BorderLayout.NORTH);

        // Sidebar Menu with improved design
        menuPanel = UIHelper.createVerticalBoxPanel();
        menuPanel.setBackground(UITheme.SIDEBAR_COLOR);
        menuPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Create menu items based on user role
        createMenuItems(menuPanel);

        // User Profile Section improved
        userPanel = UIHelper.createVerticalBoxPanel();
        userPanel.setBackground(UITheme.SIDEBAR_COLOR);
        userPanel.setBorder(new EmptyBorder(20, 10, 10, 10));

        // Separator before user profile
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(226, 232, 240));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        userPanel.add(separator);
        userPanel.add(Box.createVerticalStrut(15));

        // User info panel
        userInfoPanel = UIHelper.createVerticalBoxPanel();
        userInfoPanel.setBackground(UITheme.SIDEBAR_COLOR);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel userLabelPanel = UIHelper.createFlowPanel(FlowLayout.LEFT, 5, 0);
        userLabelPanel.setOpaque(false);
        userLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameLabel = new JLabel(controller.getUsername());
        usernameLabel.setFont(UITheme.BODY_FONT.deriveFont(Font.BOLD));
        usernameLabel.setForeground(UITheme.TEXT_COLOR);
        userLabelPanel.add(usernameLabel);
        
        userInfoPanel.add(userLabelPanel);

        userRole = new JLabel(controller.getLoaiTaiKhoan().toString());
        userRole.setFont(UITheme.SMALL_FONT);
        userRole.setForeground(UITheme.LIGHT_TEXT_COLOR);
        userRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        userInfoPanel.add(userRole);

        userPanel.add(userInfoPanel);
        userPanel.add(Box.createVerticalStrut(10));

        // Add sidebar panels
        sidebarPanel.add(menuPanel, BorderLayout.CENTER);
        sidebarPanel.add(userPanel, BorderLayout.SOUTH);

        // Chỉ hiển thị sidebar cho nhân viên, không hiển thị cho khách hàng
        if (!controller.getPermissionManager().isUser()) {
            add(sidebarPanel, BorderLayout.WEST);
        }

        // Create container that can adapt to different view sizes
        contentContainer = new JPanel();
        contentContainer.setLayout(new BorderLayout());
        contentContainer.setBackground(UITheme.BACKGROUND_COLOR);

        // Wrapper panel to center content
        centeringPanel = UIHelper.createGridBagPanel();
        centeringPanel.setBackground(UITheme.BACKGROUND_COLOR);
        centeringPanel.add(contentContainer);
        add(centeringPanel, BorderLayout.CENTER);

        // Improved header
        headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw soft border at the bottom
                g2d.setColor(new Color(226, 232, 240));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2d.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UITheme.HEADER_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, UITheme.HEADER_HEIGHT));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel titlePanel = UIHelper.createFlowPanel(FlowLayout.LEFT, 5, 0);
        titlePanel.setOpaque(false);

        titleLabel = new JLabel(controller.getPermissionManager().isAdmin() ? "Quản lý hệ thống" : "Hệ thống đặt vé");
        titleLabel.setFont(UITheme.HEADER_FONT);
        titleLabel.setForeground(UITheme.TEXT_COLOR);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel actionPanel = UIHelper.createFlowPanel(FlowLayout.RIGHT, 0, 0);
        actionPanel.setOpaque(false);
        
        // Tạo nút cài đặt với menu popup
        ImageIcon settingsIcon = IconManager.getInstance().getIcon("Cài đặt", "/images/Icon/setting.png", "⚙️", 20);
        JButton settingsButton = new JButton(settingsIcon);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        settingsButton.setToolTipText("Cài đặt");
        
        // Tạo popup menu cho nút cài đặt
        JPopupMenu settingsMenu = new JPopupMenu();
        settingsMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        settingsMenu.setPreferredSize(new Dimension(200, 100));
        
        // Thêm các mục menu
        // 1. Chuyển đổi theme với toggle button
        JPanel themePanel = new JPanel(new BorderLayout(10, 0));
        themePanel.setOpaque(false);
        
        JLabel themeLabel = new JLabel("Chế độ tối");
        themeLabel.setIcon(IconManager.getInstance().getIcon("Theme", "/images/Icon/theme.png", "🌓", 16));
        themePanel.add(themeLabel, BorderLayout.WEST);
        
        ThemeToggleButton themeToggle = new ThemeToggleButton();
        themeToggle.setThemeChangeListener(isDarkMode -> {
            // Cập nhật ThemeManager trước
            ThemeManager.getInstance().setDarkMode(isDarkMode);
            
            // Cập nhật lại FlatLaf theme
            try {
                if (isDarkMode) {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                } else {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                }
                
                // Cập nhật theme cho tất cả các thành phần
                updateTheme(ThemeManager.getInstance().getCurrentTheme());
                
                // Cập nhật lại toàn bộ UI
                SwingUtilities.updateComponentTreeUI(this);
                
                // Đảm bảo các thành phần được vẽ lại
                revalidate();
                repaint();
            } catch (Exception ex) {
                System.err.println("Failed to update FlatLaf theme: " + ex.getMessage());
            }
        });
        themePanel.add(themeToggle, BorderLayout.EAST);
        
        JMenuItem themeMenuItem = new JMenuItem();
        themeMenuItem.setLayout(new BorderLayout());
        themeMenuItem.add(themePanel);
        themeMenuItem.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        settingsMenu.add(themeMenuItem);
        
        // 3. Thêm separator
        settingsMenu.addSeparator();
        
        // 4. Đăng xuất
        JMenuItem logoutMenuItem = new JMenuItem("Đăng xuất");
        logoutMenuItem.setIcon(IconManager.getInstance().getIcon("Đăng xuất", "/images/Icon/logout.png", "🚪", 16));
        logoutMenuItem.addActionListener(e -> controller.logout());
        logoutMenuItem.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        settingsMenu.add(logoutMenuItem);
        
        // Xử lý sự kiện khi nhấn vào nút cài đặt
        settingsButton.addActionListener(e -> {
            settingsMenu.show(settingsButton, 0, settingsButton.getHeight());
        });
        
        // Thêm ảnh đại diện dạng tròn cho khách hàng
        if (controller.getPermissionManager().isUser()) {
            actionPanel.add(Box.createHorizontalStrut(10));
            
            // Tạo ảnh đại diện dạng tròn
            ImageIcon userIcon = IconManager.getInstance().getIcon("Người dùng", "/images/Icon/user.png", "👤", 30);
            JButton profileButton = new JButton(userIcon);
            profileButton.setBorderPainted(false);
            profileButton.setContentAreaFilled(false);
            profileButton.setFocusPainted(false);
            profileButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            profileButton.setToolTipText("Thông tin cá nhân");
            
            // Xử lý sự kiện khi nhấn vào ảnh đại diện
            profileButton.addActionListener(e -> {
                controller.handleMenuSelection("Thông tin cá nhân", null);
            });
            
            actionPanel.add(profileButton);
        }
        
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(settingsButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        contentContainer.add(headerPanel, BorderLayout.NORTH);

        // Main content with improved design - add ResponsiveScrollPane to only scroll vertically
        mainContentPanel = new JPanel();
        mainContentPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Create ResponsiveScrollPane to contain main content
        ResponsiveScrollPane scrollPane = new ResponsiveScrollPane();
        scrollPane.scrollToTop(); // Cuộn lên đầu
        scrollPane.scrollToBottom();

        if (controller.getPermissionManager().isAdmin() || controller.getPermissionManager().isQuanLyPhim() || 
            controller.getPermissionManager().isThuNgan() || controller.getPermissionManager().isBanVe()) {
            cardLayout = new CardLayout();
            mainContentPanel.setLayout(cardLayout);
            controller.setMainContentPanel(mainContentPanel, cardLayout);
            controller.initializeAdminPanels();
        } else {
            // Khách hàng vẫn sử dụng CardLayout để chuyển đổi giữa các màn hình
            cardLayout = new CardLayout();
            mainContentPanel.setLayout(cardLayout);
            controller.setMainContentPanel(mainContentPanel, cardLayout);
            
            // Khởi tạo các panel cho khách hàng
            controller.initializeCustomerPanels();
        }
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Lưu lại contentScrollPane để có thể thay đổi kích thước sau này
        contentScrollPane = scrollPane;
        
        // Add mainContentPanel to ResponsiveScrollPane
        scrollPane.setViewportView(mainContentPanel);
        contentContainer.add(scrollPane, BorderLayout.CENTER);

        add(contentContainer, BorderLayout.CENTER);
    }

    private void createMenuItems(JPanel menuPanel) {
        // Clear existing menu items
        menuItems.clear();
        
        if (controller.getPermissionManager().isAdmin() || controller.getPermissionManager().isQuanLyPhim() || 
            controller.getPermissionManager().isThuNgan() || controller.getPermissionManager().isBanVe()) {
            
            // Add Dashboard for all staff
            addMenuItem(menuPanel, "Dashboard", "Dashboard");
            
            // Film and screening management
            if (controller.getPermissionManager().hasPermission("Phim")) {
                addMenuItem(menuPanel, "Quản lý Phim", "Phim");
            }
            
            if (controller.getPermissionManager().hasPermission("Suất chiếu")) {
                addMenuItem(menuPanel, "Quản lý Suất chiếu", "Suất chiếu");
            }
            
            // Ticket sales
            if (controller.getPermissionManager().hasPermission("Bán vé")) {
                addMenuItem(menuPanel, "Bán vé", "Bán vé");
            }
            
            // Ticket management
            if (controller.getPermissionManager().hasPermission("Vé")) {
                addMenuItem(menuPanel, "Quản lý Vé", "Vé");
            }
            
            // Invoices
            if (controller.getPermissionManager().hasPermission("Hoá đơn")) {
                addMenuItem(menuPanel, "Quản lý Hóa đơn", "Hoá đơn");
            }
            
            // Reports & Statistics
            if (controller.getPermissionManager().hasPermission("Báo cáo")) {
                addMenuItem(menuPanel, "Báo cáo & Thống kê", "Báo cáo");
            }
            
            // Staff management
            if (controller.getPermissionManager().hasPermission("Nhân viên")) {
                addMenuItem(menuPanel, "Quản lý Nhân viên", "Nhân viên");
            }
            
            // User management (customers)
            if (controller.getPermissionManager().isAdmin()) {
                addMenuItem(menuPanel, "Quản lý Người dùng", "Người dùng");
            }
        } 
        // Khách hàng không có menu sidebar nên không cần thêm menu items
    }
    
    private void addMenuItem(JPanel menuPanel, String text, String feature) {
        // Get icon for the menu item
        ImageIcon icon = IconManager.getInstance().getIcon(feature, "/images/Icon/" + getIconFileName(feature), getIconFallback(feature), 20);
        
        // Create menu item with final reference to avoid initialization issues
        final SidebarMenuItem menuItem = new SidebarMenuItem(text, feature, icon, null);
        
        // Set action after initialization
        menuItem.setAction(e -> {
            // Deselect previous selected item
            if (selectedMenuItem != null) {
                selectedMenuItem.setSelected(false);
            }

            // Select current item
            menuItem.setSelected(true);
            selectedMenuItem = menuItem;
            
            // Update all menu items
            updateMenuItemsSelection();
            
            // Handle menu selection
            controller.handleMenuSelection(feature, menuItem);
        });
        
        // Add to list
        menuItems.add(menuItem);
        
        // Create panel for the menu item
        JPanel itemPanel = createMenuItemPanel(menuItem);
        menuPanel.add(itemPanel);
        menuPanel.add(Box.createVerticalStrut(5));
        
        // Select first item by default if none selected
        if (selectedMenuItem == null && menuItems.size() == 1) {
            menuItem.setSelected(true);
            selectedMenuItem = menuItem;
            updateMenuItemsSelection();
        }
    }
    
    private JPanel createMenuItemPanel(SidebarMenuItem menuItem) {
        return ModernUIApplier.createSidebarMenuItem(
            menuItem.getText(), 
            menuItem.getIcon(), 
            menuItem.isSelected(), 
            e -> menuItem.getAction().actionPerformed(e)
        );
    }
    
    private void updateMenuItemsSelection() {
        // Xóa tất cả các menu item hiện tại
        menuPanel.removeAll();
        
        // Thêm lại các menu item với trạng thái đã cập nhật
        for (SidebarMenuItem menuItem : menuItems) {
            JPanel itemPanel = createMenuItemPanel(menuItem);
            menuPanel.add(itemPanel);
            menuPanel.add(Box.createVerticalStrut(5));
        }
        
        // Cập nhật giao diện
        menuPanel.revalidate();
        menuPanel.repaint();
    }
    
    // Thêm biến menuPanel để lưu trữ tham chiếu
    private JPanel menuPanel;
    
    private String getIconFileName(String feature) {
        return switch (feature) {
            case "Dashboard" -> "dashboard.png";
            case "Phim" -> "movie.png";
            case "Suất chiếu" -> "schedule.png";
            case "Bán vé" -> "sell_ticket.png";
            case "Vé" -> "ticket.png";
            case "Hoá đơn" -> "invoice.png";
            case "Báo cáo" -> "report.png";
            case "Nhân viên" -> "staff.png";
            case "Người dùng" -> "user.png";
            case "Đặt vé" -> "booking.png";
            case "Thông tin cá nhân" -> "profile.png";
            case "Đăng xuất" -> "logout.png";
            default -> "default.png";
        };
    }
    
    private String getIconFallback(String feature) {
        return switch (feature) {
            case "Dashboard" -> "📊";
            case "Phim" -> "🎬";
            case "Suất chiếu" -> "⏰";
            case "Bán vé" -> "🎫";
            case "Vé" -> "🎟️";
            case "Hoá đơn" -> "📝";
            case "Báo cáo" -> "📈";
            case "Nhân viên" -> "👨‍💼";
            case "Người dùng" -> "👥";
            case "Đặt vé" -> "🎟️";
            case "Thông tin cá nhân" -> "👤";
            case "Đăng xuất" -> "🚪";
            default -> "•";
        };
    }

    // Method to get logo from utility class
    public static JLabel getAppLogo() {
        return com.cinema.utils.AppIconUtils.getAppLogo();
    }

    @Override
    public void dispose() {
        DatabaseConnection databaseConnection = controller.getDatabaseConnection();
        if (databaseConnection != null) {
            databaseConnection.closeConnection();
        }
        super.dispose();
    }

    public void openBookingViewForEmployee(int maPhim, int maKhachHang, int maNhanVien) {
        controller.openBookingView(maPhim, maKhachHang, maNhanVien);
    }
    
    /**
     * Cập nhật layout cho view được hiển thị
     * @param view View cần cập nhật layout
     */
    public void updateViewLayout(JPanel view) {
        if (view instanceof ResizableView resizableView) {
            // Reset kích thước các container
            contentContainer.setPreferredSize(null);
            centeringPanel.setPreferredSize(null);
            mainContentPanel.setPreferredSize(null);

            // Lấy kích thước mong muốn và tối thiểu từ view
            Dimension preferredSize = resizableView.getPreferredViewSize();
            Dimension minimumSize = resizableView.getMinimumViewSize();
            
            // Kiểm tra xem view có cần responsive không
            if (resizableView.isResponsive()) {
                // Nếu responsive, sử dụng kích thước của container cha
                Dimension parentSize = contentContainer.getParent().getSize();
                if (parentSize.width > 0 && parentSize.height > 0) {
                    preferredSize = new Dimension(
                        Math.max(minimumSize.width, parentSize.width - 40),
                        Math.max(minimumSize.height, parentSize.height - 40)
                    );
                }
            }
            
            // Cập nhật kích thước cho view
            view.setPreferredSize(preferredSize);
            view.setMinimumSize(minimumSize);
            
            // Cập nhật kích thước cho contentContainer
            contentContainer.setPreferredSize(preferredSize);
            contentContainer.setMinimumSize(minimumSize);

            // Cấu hình scroll pane
            if (contentScrollPane != null) {
                contentScrollPane.setVerticalScrollBarPolicy(
                    resizableView.needsScrolling() ? 
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED : 
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER
                );
                
                contentScrollPane.revalidate();
                contentScrollPane.repaint();
            }

            // Thông báo cho view rằng nó đã được hiển thị
            resizableView.onViewShown();

            // Revalidate và repaint các container
            view.revalidate();
            contentContainer.revalidate();
            centeringPanel.revalidate();
            mainContentPanel.revalidate();
            
            contentContainer.repaint();
            centeringPanel.repaint();
            mainContentPanel.repaint();
        }
    }

    /**
     * Cập nhật giao diện khi theme thay đổi
     */
    @Override
    public void updateTheme(Theme newTheme) {
        // Cập nhật màu nền
        setBackground(UITheme.BACKGROUND_COLOR);
        
        // Cập nhật sidebar
        sidebarPanel.setBackground(UITheme.SIDEBAR_COLOR);
        menuPanel.setBackground(UITheme.SIDEBAR_COLOR);
        userPanel.setBackground(UITheme.SIDEBAR_COLOR);
        userInfoPanel.setBackground(UITheme.SIDEBAR_COLOR);
        
        // Cập nhật content
        contentContainer.setBackground(UITheme.BACKGROUND_COLOR);
        centeringPanel.setBackground(UITheme.BACKGROUND_COLOR);
        
        // Cập nhật header
        headerPanel.setBackground(UITheme.HEADER_COLOR);
        
        // Cập nhật các label
        appTitle.setFont(UITheme.TITLE_FONT);
        appTitle.setForeground(UITheme.SELECTED_COLOR);
        titleLabel.setFont(UITheme.HEADER_FONT);
        titleLabel.setForeground(UITheme.TEXT_COLOR);
        usernameLabel.setFont(UITheme.BODY_FONT.deriveFont(Font.BOLD));
        usernameLabel.setForeground(UITheme.TEXT_COLOR);
        userRole.setFont(UITheme.SMALL_FONT);
        userRole.setForeground(UITheme.LIGHT_TEXT_COLOR);
        
        // Cập nhật menu items
        updateMenuItemsSelection();
        
        // Cập nhật màu cho các separator
        for (Component comp : logoPanel.getComponents()) {
            if (comp instanceof JSeparator) {
                ((JSeparator) comp).setForeground(new Color(226, 232, 240));
            }
        }
        
        for (Component comp : userPanel.getComponents()) {
            if (comp instanceof JSeparator) {
                ((JSeparator) comp).setForeground(new Color(226, 232, 240));
            }
        }
        
        // Repaint toàn bộ UI
        SwingUtilities.updateComponentTreeUI(this);
    }
}