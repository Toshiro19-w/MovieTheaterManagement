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
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
    
    // UI components for theme updates
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
    private JPanel menuPanel;
    private JButton toggleSidebarButton;

    public MainView(String username, LoaiTaiKhoan loaiTaiKhoan) throws IOException, SQLException {
        // Register theme change listener
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
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(UITheme.BACKGROUND_COLOR);
        
        // Set app icon
        com.cinema.utils.AppIconUtils.setAppIcon(this);
        
        initUI();
    }
    
    private void initUI() throws IOException, SQLException {
        setLayout(new BorderLayout());

        // Sidebar with fixed size for admin users
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
        sidebarPanel.setBackground(UITheme.BACKGROUND_COLOR);
        sidebarPanel.setBorder(new EmptyBorder(15, 10, 10, 10));

        // Logo and app title in sidebar
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

        // Sidebar Menu
        menuPanel = UIHelper.createVerticalBoxPanel();
        menuPanel.setBackground(UITheme.SIDEBAR_COLOR);
        menuPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Create menu items based on user role
        createMenuItems(menuPanel);

        // User Profile Section
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

        // Show sidebar only for non-customer users
        if (!controller.getPermissionManager().isUser()) {
            add(sidebarPanel, BorderLayout.WEST);
        }

        // Create container for content
        contentContainer = new JPanel();
        contentContainer.setLayout(new BorderLayout());
        contentContainer.setBackground(UITheme.BACKGROUND_COLOR);

        // Wrapper panel to center content
        centeringPanel = UIHelper.createGridBagPanel();
        centeringPanel.setBackground(UITheme.BACKGROUND_COLOR);
        centeringPanel.add(contentContainer);
        add(centeringPanel, BorderLayout.CENTER);

        // Header panel
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

        // Hamburger menu button for toggling sidebar
        JPanel togglePanel = UIHelper.createFlowPanel(FlowLayout.LEFT, 5, 0);
        togglePanel.setOpaque(false);
        if(!controller.getPermissionManager().isUser()) {
        ImageIcon hamburgerIcon = IconManager.getInstance().getIcon("Menu", "/images/Icon/menu.jpg", "☰", 20);
        toggleSidebarButton = new JButton(hamburgerIcon);
        toggleSidebarButton.setBorderPainted(false);
        toggleSidebarButton.setContentAreaFilled(false);
        toggleSidebarButton.setFocusPainted(false);
        toggleSidebarButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        toggleSidebarButton.setToolTipText("Toggle Sidebar");
        toggleSidebarButton.addActionListener(e -> toggleSidebar());
        toggleSidebarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                toggleSidebarButton.setBackground(UITheme.ACCENT_COLOR);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                toggleSidebarButton.setBackground(UITheme.HEADER_COLOR);
            }
        });
        togglePanel.add(toggleSidebarButton);
        }
        headerPanel.add(togglePanel, BorderLayout.WEST);

        JPanel titlePanel = UIHelper.createFlowPanel(FlowLayout.LEFT, 5, 0);
        titlePanel.setOpaque(false);

        titleLabel = new JLabel(controller.getPermissionManager().isAdmin() ? "Quản lý hệ thống" : "Hệ thống đặt vé");
        titleLabel.setFont(UITheme.HEADER_FONT);
        titleLabel.setForeground(UITheme.TEXT_COLOR);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        JPanel actionPanel = UIHelper.createFlowPanel(FlowLayout.RIGHT, 0, 0);
        actionPanel.setOpaque(false);
        
        // Settings button with popup menu
        ImageIcon settingsIcon = IconManager.getInstance().getIcon("Cài đặt", "/images/Icon/setting.png", "⚙️", 20);
        JButton settingsButton = new JButton(settingsIcon);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        settingsButton.setToolTipText("Cài đặt");
        
        // Create popup menu for settings
        JPopupMenu settingsMenu = new JPopupMenu();
        settingsMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        settingsMenu.setPreferredSize(new Dimension(200, 100));
        
        // Theme toggle menu item
        JPanel themePanel = new JPanel(new BorderLayout(10, 0));
        themePanel.setOpaque(false);
        
        JLabel themeLabel = new JLabel("Chế độ tối");
        themeLabel.setIcon(IconManager.getInstance().getIcon("Theme", "/images/Icon/theme.png", "🌓", 16));
        themePanel.add(themeLabel, BorderLayout.WEST);
        
        ThemeToggleButton themeToggle = new ThemeToggleButton();
        themeToggle.setThemeChangeListener(isDarkMode -> {
            ThemeManager.getInstance().setDarkMode(isDarkMode);
            try {
                if (isDarkMode) {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                } else {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                }
                updateTheme(ThemeManager.getInstance().getCurrentTheme());
                SwingUtilities.updateComponentTreeUI(this);
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
        
        settingsMenu.addSeparator();
        
        // Logout menu item
        JMenuItem logoutMenuItem = new JMenuItem("Đăng xuất");
        logoutMenuItem.setIcon(IconManager.getInstance().getIcon("Đăng xuất", "/images/Icon/logout.png", "🚪", 16));
        logoutMenuItem.addActionListener(e -> controller.logout());
        logoutMenuItem.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        settingsMenu.add(logoutMenuItem);
        
        settingsButton.addActionListener(e -> {
            settingsMenu.show(settingsButton, 0, settingsButton.getHeight());
        });
        
        // Add profile and logout buttons for customers
        if (controller.getPermissionManager().isUser()) {
            actionPanel.add(Box.createHorizontalStrut(10));
            
            ImageIcon userIcon = IconManager.getInstance().getIcon("Người dùng", "/images/Icon/user.png", "👤", 30);
            JButton profileButton = new JButton(userIcon);
            profileButton.setBorderPainted(false);
            profileButton.setContentAreaFilled(false);
            profileButton.setFocusPainted(false);
            profileButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            profileButton.setToolTipText("Thông tin cá nhân");
            profileButton.addActionListener(e -> {
                controller.handleMenuSelection("Thông tin cá nhân", null);
            });
            actionPanel.add(profileButton);

//            actionPanel.add(Box.createHorizontalStrut(10));
//            ImageIcon logoutIcon = IconManager.getInstance().getIcon("Đăng xuất", "/images/Icon/logout.png", "🚪", 30);
//            JButton logoutButtonHeader = new JButton(logoutIcon);
//            logoutButtonHeader.setBorderPainted(false);
//            logoutButtonHeader.setContentAreaFilled(false);
//            logoutButtonHeader.setFocusPainted(false);
//            logoutButtonHeader.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
//            logoutButtonHeader.setToolTipText("Đăng xuất");
//            logoutButtonHeader.addActionListener(e -> {
//                int confirm = JOptionPane.showConfirmDialog(this,
//                        "Bạn có chắc muốn đăng xuất?",
//                        "Xác nhận đăng xuất",
//                        JOptionPane.YES_NO_OPTION);
//                if (confirm == JOptionPane.YES_OPTION) {
//                    controller.logout();
//                    dispose();
//                }
//            });
//            actionPanel.add(logoutButtonHeader);
        }
        
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(settingsButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        contentContainer.add(headerPanel, BorderLayout.NORTH);

        // Main content
        mainContentPanel = new JPanel();
        mainContentPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        ResponsiveScrollPane scrollPane = new ResponsiveScrollPane();
        scrollPane.scrollToTop();

        if (controller.getPermissionManager().isAdmin() || controller.getPermissionManager().isQuanLyPhim() || 
            controller.getPermissionManager().isThuNgan() || controller.getPermissionManager().isBanVe()) {
            cardLayout = new CardLayout();
            mainContentPanel.setLayout(cardLayout);
            controller.setMainContentPanel(mainContentPanel, cardLayout);
            controller.initializeAdminPanels();
        } else {
            cardLayout = new CardLayout();
            mainContentPanel.setLayout(cardLayout);
            controller.setMainContentPanel(mainContentPanel, cardLayout);
            controller.initializeCustomerPanels();
        }
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        contentScrollPane = scrollPane;
        scrollPane.setViewportView(mainContentPanel);
        contentContainer.add(scrollPane, BorderLayout.CENTER);

        add(contentContainer, BorderLayout.CENTER);
    }

    private void toggleSidebar() {
        if (!controller.getPermissionManager().isUser()) { // Only toggle for non-customer users
            sidebarPanel.setVisible(!sidebarPanel.isVisible());
            toggleSidebarButton.setToolTipText(sidebarPanel.isVisible() ? "Hide Sidebar" : "Show Sidebar");
            contentContainer.revalidate();
            contentContainer.repaint();
            centeringPanel.revalidate();
            centeringPanel.repaint();
        }
    }

    private void createMenuItems(JPanel menuPanel) {
        menuItems.clear();
        
        if (controller.getPermissionManager().isAdmin() || controller.getPermissionManager().isQuanLyPhim() || 
            controller.getPermissionManager().isThuNgan() || controller.getPermissionManager().isBanVe()) {
            
            addMenuItem(menuPanel, "Dashboard", "Dashboard");
            if (controller.getPermissionManager().hasPermission("Phim")) {
                addMenuItem(menuPanel, "Quản lý Phim", "Phim");
            }
            if (controller.getPermissionManager().hasPermission("Suất chiếu")) {
                addMenuItem(menuPanel, "Quản lý Suất chiếu", "Suất chiếu");
            }
            if (controller.getPermissionManager().hasPermission("Bán vé")) {
                addMenuItem(menuPanel, "Bán vé", "Bán vé");
            }
            if (controller.getPermissionManager().hasPermission("Vé")) {
                addMenuItem(menuPanel, "Quản lý Vé", "Vé");
            }
            if (controller.getPermissionManager().hasPermission("Hoá đơn")) {
                addMenuItem(menuPanel, "Quản lý Hóa đơn", "Hoá đơn");
            }
            if (controller.getPermissionManager().hasPermission("Báo cáo")) {
                addMenuItem(menuPanel, "Báo cáo & Thống kê", "Báo cáo");
            }
            if (controller.getPermissionManager().hasPermission("Nhân viên")) {
                addMenuItem(menuPanel, "Quản lý Nhân viên", "Nhân viên");
            }
            if (controller.getPermissionManager().isAdmin()) {
                addMenuItem(menuPanel, "Quản lý Người dùng", "Người dùng");
            }
        }
    }
    
    private void addMenuItem(JPanel menuPanel, String text, String feature) {
        ImageIcon icon = IconManager.getInstance().getIcon(feature, "/images/Icon/" + getIconFileName(feature), getIconFallback(feature), 20);
        final SidebarMenuItem menuItem = new SidebarMenuItem(text, feature, icon, null);
        menuItem.setAction(e -> {
            if (selectedMenuItem != null) {
                selectedMenuItem.setSelected(false);
            }
            menuItem.setSelected(true);
            selectedMenuItem = menuItem;
            updateMenuItemsSelection();
            controller.handleMenuSelection(feature, menuItem);
        });
        menuItems.add(menuItem);
        JPanel itemPanel = createMenuItemPanel(menuItem);
        menuPanel.add(itemPanel);
        menuPanel.add(Box.createVerticalStrut(5));
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
        menuPanel.removeAll();
        for (SidebarMenuItem menuItem : menuItems) {
            JPanel itemPanel = createMenuItemPanel(menuItem);
            menuPanel.add(itemPanel);
            menuPanel.add(Box.createVerticalStrut(5));
        }
        menuPanel.revalidate();
        menuPanel.repaint();
    }
    
    private String getIconFileName(String feature) {
        return switch (feature) {
            case "Dashboard" -> "Dashboard.png";
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

    public static JLabel getAppLogo() {
        return com.cinema.utils.AppIconUtils.getAppLogo();
    }

    @Override
    public void dispose() {
        DatabaseConnection databaseConnection = controller.getDatabaseConnection();
        if (databaseConnection != null) {
            databaseConnection.close();
        }
        super.dispose();
    }

    public void openBookingViewForEmployee(int maPhim, int maKhachHang, int maNhanVien) {
        controller.openBookingView(maPhim, maKhachHang, maNhanVien);
    }
    
    public void updateViewLayout(JPanel view) {
        if (view instanceof ResizableView resizableView) {
            contentContainer.setPreferredSize(null);
            centeringPanel.setPreferredSize(null);
            mainContentPanel.setPreferredSize(null);

            Dimension preferredSize = resizableView.getPreferredViewSize();
            Dimension minimumSize = resizableView.getMinimumViewSize();
            
            if (resizableView.isResponsive()) {
                Dimension parentSize = contentContainer.getParent().getSize();
                if (parentSize.width > 0 && parentSize.height > 0) {
                    preferredSize = new Dimension(
                        Math.max(minimumSize.width, parentSize.width - 40),
                        Math.max(minimumSize.height, parentSize.height - 40)
                    );
                }
            }
            
            view.setPreferredSize(preferredSize);
            view.setMinimumSize(minimumSize);
            contentContainer.setPreferredSize(preferredSize);
            contentContainer.setMinimumSize(minimumSize);

            if (contentScrollPane != null) {
                contentScrollPane.setVerticalScrollBarPolicy(
                    resizableView.needsScrolling() ? 
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED : 
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER
                );
                contentScrollPane.revalidate();
                contentScrollPane.repaint();
            }

            resizableView.onViewShown();

            view.revalidate();
            contentContainer.revalidate();
            centeringPanel.revalidate();
            mainContentPanel.revalidate();
            
            contentContainer.repaint();
            centeringPanel.repaint();
            mainContentPanel.repaint();
        }
    }

    @Override
    public void updateTheme(Theme newTheme) {
        setBackground(UITheme.BACKGROUND_COLOR);
        sidebarPanel.setBackground(UITheme.SIDEBAR_COLOR);
        menuPanel.setBackground(UITheme.SIDEBAR_COLOR);
        userPanel.setBackground(UITheme.SIDEBAR_COLOR);
        userInfoPanel.setBackground(UITheme.SIDEBAR_COLOR);
        contentContainer.setBackground(UITheme.BACKGROUND_COLOR);
        centeringPanel.setBackground(UITheme.BACKGROUND_COLOR);
        headerPanel.setBackground(UITheme.HEADER_COLOR);
        
        appTitle.setFont(UITheme.TITLE_FONT);
        appTitle.setForeground(UITheme.SELECTED_COLOR);
        titleLabel.setFont(UITheme.HEADER_FONT);
        titleLabel.setForeground(UITheme.TEXT_COLOR);
        usernameLabel.setFont(UITheme.BODY_FONT.deriveFont(Font.BOLD));
        usernameLabel.setForeground(UITheme.TEXT_COLOR);
        userRole.setFont(UITheme.SMALL_FONT);
        userRole.setForeground(UITheme.LIGHT_TEXT_COLOR);
        
        updateMenuItemsSelection();
        
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
        
        SwingUtilities.updateComponentTreeUI(this);
    }
}