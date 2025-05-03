package com.cinema.views;

import com.cinema.controllers.DatVeController;
import com.cinema.controllers.PaymentController;
import com.cinema.controllers.PhimController;
import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.models.Ghe;
import com.cinema.models.SuatChieu;
import com.cinema.models.repositories.VeRepository;
import com.cinema.services.GheService;
import com.cinema.services.SuatChieuService;
import com.cinema.services.VeService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.PermissionManager;
import com.cinema.views.admin.*;
import com.cinema.views.login.LoginView;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

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
                _ -> {
                    dispose();
                    SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
                }
        );
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
        add(mainContentPanel, BorderLayout.CENTER);

        // Footer
        if (permissionManager.isUser() || permissionManager.isBanVe()) {
            JPanel footerPanel = new JPanel();
            footerPanel.setPreferredSize(new Dimension(1280, 50));
            footerPanel.setBackground(new Color(0, 48, 135));
            JLabel footerLabel = new JLabel("© 2025 Cinema App - Liên hệ: contact@cinema.com", SwingConstants.CENTER);
            footerLabel.setForeground(Color.WHITE);
            footerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            footerPanel.add(footerLabel);
            add(footerPanel, BorderLayout.SOUTH);
        }
    }

    private void handleMenuSelection(String feature) {
        if (permissionManager.isAdmin() || permissionManager.isQuanLyPhim() || permissionManager.isThuNgan()) {
            cardLayout.show(mainContentPanel, feature);
        } else if (permissionManager.isUser() || permissionManager.isBanVe()) {
            if (feature.equals("Phim đang chiếu") || feature.equals("Đặt vé") || feature.equals("Suất chiếu")) {
                for (Component comp : mainContentPanel.getComponents()) {
                    if (comp instanceof PhimListView) {
                        ((PhimListView) comp).loadPhimList("");
                        break;
                    }
                }
            } else if (feature.equals("Thông tin cá nhân")) {
                UserInfoView userInfoView = new UserInfoView(this, username);
                userInfoView.setVisible(true);
            }
        }
    }

    private void openBookingView(int maPhim, int maKhachHang) {
        if (!permissionManager.hasPermission("Đặt vé")) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền đặt vé!", "Lỗi quyền truy cập", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DatVeController datVeController = new DatVeController(
                new SuatChieuService(databaseConnection),
                new GheService(databaseConnection),
                new VeService(databaseConnection)
        );
        BookingView bookingView = new BookingView(this, datVeController, maPhim, maKhachHang, bookingResult -> {
            PaymentView paymentView = new PaymentView(
                    this, paymentController, bookingResult.suatChieu(), bookingResult.ghe(), bookingResult.giaVe(),
                    paymentResult -> saveDatVe(
                            paymentResult.suatChieu,
                            paymentResult.ghe,
                            paymentResult.giaVe,
                            paymentResult.transactionId
                    )
            );
            paymentView.setVisible(true);
        });
        bookingView.setVisible(true);
    }

    private void saveDatVe(SuatChieu suatChieu, Ghe ghe, BigDecimal giaVe, String transactionId) {
        try {
            System.out.println("Xử lý thanh toán: Suất chiếu - " + suatChieu.getMaSuatChieu() +
                    ", Ghế - " + ghe.getSoGhe() +
                    ", Số tiền - " + giaVe +
                    ", Transaction ID - " + transactionId);
            JOptionPane.showMessageDialog(this, "Thanh toán thành công! Transaction ID: " + transactionId,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý thanh toán: " + e.getMessage(),
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