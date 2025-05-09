package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() throws IOException, SQLException {
        // Header
        HeaderPanel headerPanel = new HeaderPanel(
                username, loaiTaiKhoan,
                this::handleMenuSelection,
                searchText -> {
                    for (Component comp : mainContentPanel.getComponents()) {
                        if (comp instanceof PhimListView) {
                            ((PhimListView) comp).loadPhimList(searchText);
                            break;
                        }
                    }
                },
                _ -> {
                    dispose();
                    SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
                }
        );
        headerPanel.setBackground(new Color(0, 123, 255)); // Modern blue header
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        mainContentPanel = new JPanel();
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan()) {
            cardLayout = new CardLayout();
            mainContentPanel.setLayout(cardLayout);
            AdminViewManager adminViewManager = new AdminViewManager(loaiTaiKhoan, mainContentPanel, cardLayout);
            adminViewManager.initializeAdminPanels();
        } else {
            mainContentPanel.setLayout(new BorderLayout());
            mainContentPanel.setBackground(Color.WHITE);
            PhimListView phimListView = new PhimListView(phimController, this::openBookingView, username);
            mainContentPanel.add(phimListView, BorderLayout.CENTER);
        }
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainContentPanel, BorderLayout.CENTER);

        // Footer
        if (permissionManager.isUser()) {
            JPanel footerPanel = new JPanel();
            footerPanel.setPreferredSize(new Dimension(1280, 50));
            footerPanel.setBackground(new Color(33, 37, 41)); // Dark footer
            JLabel footerLabel = new JLabel("© 2025 Cinema App - Liên hệ: contact@cinema.com", SwingConstants.CENTER);
            footerLabel.setForeground(Color.LIGHT_GRAY);
            footerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            footerPanel.add(footerLabel);
            add(footerPanel, BorderLayout.SOUTH);
        }
    }

    private void handleMenuSelection(String feature) {
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan()) {
            cardLayout.show(mainContentPanel, feature);
        } else if (permissionManager.isUser()) {
            if (feature.equals("Phim đang chiếu") || feature.equals("Đặt vé") || feature.equals("Suất chiếu")) {
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
            // Callback này được gọi khi thanh toán thành công, bao gồm transactionId
            saveDatVe(
                    bookingResult.suatChieu(),
                    bookingResult.ghe(),
                    bookingResult.giaVe(),
                    bookingResult.transactionId() // Lấy transactionId từ BookingResult đã được cập nhật
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
}