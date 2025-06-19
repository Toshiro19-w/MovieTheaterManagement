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
import com.cinema.models.NhanVien;
import com.cinema.models.SuatChieu;
import com.cinema.services.GheService;
import com.cinema.services.NhanVienService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.PermissionManager;
import com.cinema.views.MainView;
import com.cinema.views.admin.AdminViewManager;
import com.cinema.views.admin.DashboardView;
import com.cinema.views.admin.UserManagementView;
import com.cinema.views.booking.BookingView;
import com.cinema.views.customer.PhimListView;
import com.cinema.views.customer.UserInfoView;
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
    private NhanVien currentNhanVien;

    public MainViewController(MainView view, String username, LoaiTaiKhoan loaiTaiKhoan) throws IOException, SQLException {
        this.view = view;
        this.username = username;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.permissionManager = new PermissionManager(loaiTaiKhoan);
        this.paymentController = new PaymentController();
        
        try {
            this.databaseConnection = new DatabaseConnection();
            this.phimController = new PhimController(new com.cinema.views.admin.PhimView(currentNhanVien), currentNhanVien);
            
            if (!permissionManager.isUser()) {
                NhanVienService nhanVienService = new NhanVienService(databaseConnection);
                this.currentNhanVien = nhanVienService.findByUsername(username);
                
                if (this.currentNhanVien == null) {
                    throw new IllegalStateException("Không tìm thấy thông tin nhân viên cho tài khoản: " + username);
                }
            }
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
            
            AdminViewManager adminViewManager = new AdminViewManager(loaiTaiKhoan, mainContentPanel, cardLayout, username, currentNhanVien);
            adminViewManager.initializeAdminPanels();

            DashboardView dashboardView = new DashboardView(databaseConnection);
            mainContentPanel.add(dashboardView, "Dashboard");

            cardLayout.show(mainContentPanel, "Dashboard");
            
            // Cập nhật kích thước view sau khi hiển thị Dashboard
            view.updateViewLayout(dashboardView);
            UserManagementView userManagementView = new UserManagementView();
            mainContentPanel.add(userManagementView, "Người dùng");
        } else {
            PhimListView phimListView = new PhimListView(phimController, this::openBookingViewAdapter, username);
            mainContentPanel.add(phimListView, BorderLayout.CENTER);
        }
    }
    
    /**
     * Khởi tạo các panel cho khách hàng
     */
    public void initializeCustomerPanels() throws IOException, SQLException {
        // Khởi tạo màn hình danh sách phim
        PhimListView phimListView = new PhimListView(phimController, this::openBookingViewAdapter, username);
        mainContentPanel.add(phimListView, "Phim");
        
        // Hiển thị màn hình phim mặc định
        cardLayout.show(mainContentPanel, "Phim");
        
        // Cập nhật kích thước view sau khi hiển thị màn hình phim
        view.updateViewLayout(phimListView);
    }      public void handleMenuSelection(String feature, SidebarMenuItem menuItem) {
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || 
            permissionManager.isThuNgan() || permissionManager.isBanVe()) {
            try {
                // Check if user has permission to access this feature
                if (permissionManager.hasPermission(feature) || feature.equals("Dashboard")) {
                    cardLayout.show(mainContentPanel, feature);
                    
                    // Tìm view hiện tại và cập nhật kích thước
                    Component[] components = mainContentPanel.getComponents();
                    for (Component component : components) {
                        if (component.isVisible() && component instanceof JPanel) {
                            view.updateViewLayout((JPanel) component);
                            break;
                        }
                    }
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
                    
                    // Cập nhật kích thước view sau khi chuyển đổi
                    Component[] components = mainContentPanel.getComponents();
                    for (Component component : components) {
                        if (component.isVisible() && component instanceof JPanel) {
                            view.updateViewLayout((JPanel) component);
                            break;
                        }
                    }
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

    // Dành cho khách hàng
    private void openBookingViewAdapter(int maPhim, int maKhachHang) {
        int maNhanVien = 0;
        openBookingView(maPhim, maKhachHang, maNhanVien);
    }
    
    public void openBookingView(int maPhim, int maKhachHang, int maNhanVien) {
        DatVeController datVeController = new DatVeController(
                new SuatChieuService(databaseConnection),
                new GheService(databaseConnection),
                new VeService(databaseConnection)
        );
        BookingView bookingView = new BookingView(view, datVeController, paymentController, maPhim, maKhachHang, maNhanVien, bookingResult -> {
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
            // Ghi log hoặc thực hiện các thao tác khác sau khi đặt vé thành công
            System.out.println("Đặt vé thành công: " + 
                            "Suất chiếu: " + suatChieu.getMaSuatChieu() + 
                            ", Ghế: " + ghe.getSoGhe() + 
                            ", Giá vé: " + giaVe + 
                            ", Mã giao dịch: " + transactionId);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, 
                "Lỗi khi lưu thông tin đặt vé: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
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