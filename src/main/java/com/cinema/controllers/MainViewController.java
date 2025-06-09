package com.cinema.controllers;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.models.Ghe;
import com.cinema.models.SuatChieu;
import com.cinema.services.GheService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.PermissionManager;
import com.cinema.views.BookingView;
import com.cinema.views.MainView;
import com.cinema.views.PhimListView;
import com.cinema.views.UserInfoView;
import com.cinema.views.admin.AdminViewManager;
import com.cinema.views.admin.DashboardView;
import com.cinema.views.admin.UserManagementView;
import com.cinema.views.login.LoginView;
import com.cinema.views.sidebar.SidebarMenuItem;

/**
 * Controller class for MainView to handle navigation and business logic
 */
public class MainViewController {
    private final MainView view;
    private final String username;
    private final LoaiTaiKhoan loaiTaiKhoan;
    private final PermissionManager permissionManager;
    private final PaymentController paymentController;
    private final DatabaseConnection databaseConnection;
    private PhimController phimController;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    public MainViewController(MainView view, String username, LoaiTaiKhoan loaiTaiKhoan) throws IOException, SQLException {
        this.view = view;
        this.username = username;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.permissionManager = new PermissionManager(loaiTaiKhoan);
        this.paymentController = new PaymentController();
        
        try {
            this.databaseConnection = new DatabaseConnection();
            this.phimController = new PhimController(new com.cinema.views.admin.PhimView());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Không thể đọc file cấu hình cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
    
    public void setMainContentPanel(JPanel mainContentPanel, CardLayout cardLayout) {
        this.mainContentPanel = mainContentPanel;
        this.cardLayout = cardLayout;
    }
    
    public void initializeAdminPanels() throws IOException, SQLException {
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || 
            permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            
            AdminViewManager adminViewManager = new AdminViewManager(loaiTaiKhoan, mainContentPanel, cardLayout, username);
            adminViewManager.initializeAdminPanels();

            DashboardView dashboardView = new DashboardView(databaseConnection);
            mainContentPanel.add(dashboardView, "Dashboard");

            cardLayout.show(mainContentPanel, "Dashboard");
            UserManagementView userManagementView = new UserManagementView();
            mainContentPanel.add(userManagementView, "Người dùng");
        } else {
            PhimListView phimListView = new PhimListView(phimController, this::openBookingView, username);
            mainContentPanel.add(phimListView, BorderLayout.CENTER);
        }
    }
    
    /**
     * Khởi tạo các panel cho khách hàng
     */
    public void initializeCustomerPanels() throws IOException, SQLException {
        // Khởi tạo màn hình danh sách phim
        PhimListView phimListView = new PhimListView(phimController, this::openBookingView, username);
        mainContentPanel.add(phimListView, "Phim");
        
        // Hiển thị màn hình phim mặc định
        cardLayout.show(mainContentPanel, "Phim");
    }
    
    public void handleMenuSelection(String feature, SidebarMenuItem menuItem) {
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || 
            permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            try {
                // Check if user has permission to access this feature
                if (permissionManager.hasPermission(feature) || feature.equals("Dashboard")) {
                    cardLayout.show(mainContentPanel, feature);
                } else {
                    JOptionPane.showMessageDialog(view, 
                        "Bạn không có quyền truy cập tính năng này!", 
                        "Cảnh báo", 
                        JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, 
                    "Lỗi khi chuyển đổi view: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else if (permissionManager.isUser()) {
            try {
                if (feature.equals("Phim")) {
                    cardLayout.show(mainContentPanel, "Phim");
                } else if (feature.equals("Thông tin cá nhân")) {
                    // Hiển thị thông tin cá nhân trong dialog
                    UserInfoView userInfoView = new UserInfoView(view, username);
                    userInfoView.setVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, 
                    "Lỗi khi chuyển đổi view: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void logout() {
        view.dispose();
        try {
            new LoginView().setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void openBookingView(int maPhim, int maKhachHang) {
        DatVeController datVeController = new DatVeController(
                new SuatChieuService(databaseConnection),
                new GheService(databaseConnection),
                new VeService(databaseConnection)
        );
        BookingView bookingView = new BookingView(view, datVeController, paymentController, maPhim, maKhachHang, bookingResult -> {
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
            JOptionPane.showMessageDialog(view, "Đặt vé và thanh toán thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi lưu thông tin đặt vé: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    public String getUsername() {
        return username;
    }
    
    public LoaiTaiKhoan getLoaiTaiKhoan() {
        return loaiTaiKhoan;
    }
}