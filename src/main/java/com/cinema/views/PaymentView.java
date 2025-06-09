package com.cinema.views;

import com.cinema.controllers.PaymentController;
import com.cinema.controllers.DatVeController;
import com.cinema.models.dto.PaymentRequest;
import com.cinema.models.dto.PaymentResponse;
import com.cinema.enums.PaymentStatus;
import com.cinema.enums.PaymentMethod;
import com.cinema.models.SuatChieu;
import com.cinema.models.Ghe;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.sql.SQLException;

public class PaymentView extends JDialog {
    private final PaymentController paymentController;
    private final DatVeController datVeController;
    private final SuatChieu suatChieu;
    private final Ghe ghe;
    private final BigDecimal giaVe;
    private final String transactionId;
    private final Consumer<PaymentResult> paymentCallback;
    private final int maVe;
    private final int maKhachHang;

    public PaymentView(JFrame parent, PaymentController paymentController, DatVeController datVeController, SuatChieu suatChieu, Ghe ghe, BigDecimal giaVe, int maVe, int maKhachHang, Consumer<PaymentResult> paymentCallback) {
        super(parent, "Thanh toán bằng MoMo", true);
        this.paymentController = paymentController;
        this.datVeController = datVeController;
        this.suatChieu = suatChieu;
        this.ghe = ghe;
        this.giaVe = giaVe;
        this.transactionId = UUID.randomUUID().toString();
        this.maVe = maVe;
        this.maKhachHang = maKhachHang;
        this.paymentCallback = paymentCallback;

        setSize(350, 500);
        setLayout(new BorderLayout(0, 10));
        setLocationRelativeTo(parent);

        initializeComponents();
    }

    private void initializeComponents() {
        try {
            String orderInfo = "Thanh toán vé xem phim - " + suatChieu.getMaSuatChieu();
            String accountId = "0565321247";
            PaymentRequest request = new PaymentRequest(accountId, giaVe, orderInfo, PaymentMethod.MOMO);
            PaymentResponse response = paymentController.createPayment(request, transactionId);

            JPanel qrPanel = new JPanel(new BorderLayout());
            JLabel qrLabel = new JLabel(new ImageIcon(response.getQrImage()));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrPanel.add(qrLabel, BorderLayout.CENTER);
            add(qrPanel, BorderLayout.CENTER);

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel instructionLabel = new JLabel("<html><div style='text-align: center;'>Quét mã QR để thanh toán<br>Số tiền: "
                    + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(giaVe) + "</div></html>", SwingConstants.CENTER);
            instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(instructionLabel);

            infoPanel.add(Box.createVerticalStrut(10));

            JLabel statusLabel = new JLabel("Đang chờ thanh toán...", SwingConstants.CENTER);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(statusLabel);

            add(infoPanel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

            JButton confirmButton = new JButton("Xác nhận thanh toán");
            confirmButton.addActionListener(_ -> confirmPayment(statusLabel));
            buttonPanel.add(confirmButton);

            JButton cancelButton = new JButton("Hủy");
            cancelButton.addActionListener(e -> {
                int option = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc muốn hủy thanh toán này?",
                        "Xác nhận hủy",
                        JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    cancelBooking();
                    dispose();
                }
            });
            buttonPanel.add(cancelButton);

            add(buttonPanel, BorderLayout.SOUTH);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo thanh toán: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            cancelBooking();
            dispose();
        }
    }

    private void confirmPayment(JLabel statusLabel) {
        try {
            PaymentStatus status = paymentController.checkPaymentStatus(transactionId);

            if (status == PaymentStatus.COMPLETED) {
                statusLabel.setText("Thanh toán thành công!");
                int maHoaDon = datVeController.confirmPayment(maVe, maKhachHang);
                paymentCallback.accept(new PaymentResult(suatChieu, ghe, giaVe, transactionId));
                dispose();
            } else if (status == PaymentStatus.FAILED) {
                statusLabel.setText("Thanh toán thất bại!");
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thất bại! Vui lòng thử lại.",
                        "Thất bại",
                        JOptionPane.ERROR_MESSAGE);
            } else if (status == PaymentStatus.EXPIRED) {
                statusLabel.setText("Thanh toán đã hết hạn!");
                JOptionPane.showMessageDialog(this,
                        "Thanh toán đã hết hạn! Vui lòng tạo giao dịch mới.",
                        "Hết hạn",
                        JOptionPane.WARNING_MESSAGE);
                cancelBooking();
                dispose();
            } else {
                statusLabel.setText("Đang chờ thanh toán...");
                JOptionPane.showMessageDialog(this,
                        "Chưa nhận được thanh toán. Vui lòng quét mã QR và hoàn tất thanh toán.",
                        "Đang chờ",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            statusLabel.setText("Lỗi xác nhận thanh toán!");
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xác nhận thanh toán: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            statusLabel.setText("Lỗi xác nhận thanh toán!");
            JOptionPane.showMessageDialog(this,
                    "Lỗi kiểm tra thanh toán: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelBooking() {
        try {
            datVeController.cancelVe(maVe);
            JOptionPane.showMessageDialog(this, "Đã hủy đặt vé thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            paymentCallback.accept(null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi hủy vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static class PaymentResult {
        public final SuatChieu suatChieu;
        public final Ghe ghe;
        public final BigDecimal giaVe;
        public final String transactionId;

        public PaymentResult(SuatChieu suatChieu, Ghe ghe, BigDecimal giaVe, String transactionId) {
            this.suatChieu = suatChieu;
            this.ghe = ghe;
            this.giaVe = giaVe;
            this.transactionId = transactionId;
        }
    }
}