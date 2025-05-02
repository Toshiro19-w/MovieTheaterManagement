package com.cinema.views;

import com.cinema.controllers.PaymentController;
import com.cinema.dto.PaymentRequest;
import com.cinema.dto.PaymentResponse;
import com.cinema.enums.PaymentStatus;
import com.cinema.enums.PaymentMethod;
import com.cinema.models.SuatChieu;
import com.cinema.models.Ghe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PaymentView extends JDialog {
    private final PaymentController paymentController;
    private final SuatChieu suatChieu;
    private final Ghe ghe;
    private final BigDecimal giaVe;
    private final String transactionId;
    private final Consumer<PaymentResult> paymentCallback;

    public PaymentView(JFrame parent, PaymentController paymentController, SuatChieu suatChieu, Ghe ghe, BigDecimal giaVe, Consumer<PaymentResult> paymentCallback) {
        super(parent, "Thanh toán bằng MoMo", true);
        this.paymentController = paymentController;
        this.suatChieu = suatChieu;
        this.ghe = ghe;
        this.giaVe = giaVe;
        this.transactionId = UUID.randomUUID().toString();
        this.paymentCallback = paymentCallback;

        setSize(350, 500);
        setLayout(new BorderLayout(0, 10));
        setLocationRelativeTo(parent);

        initializeComponents();
    }

    private void initializeComponents() {
        try {
            String orderInfo = "Thanh toán vé xem phim - " + suatChieu.getMaSuatChieu();
            String accountId = "0565321247"; // Thay bằng accountId của bạn từ MoMo
            PaymentRequest request = new PaymentRequest(accountId, giaVe, orderInfo, PaymentMethod.MOMO);
            PaymentResponse response = paymentController.createPayment(request, transactionId);

            // QR Panel
            JPanel qrPanel = new JPanel(new BorderLayout());
            JLabel qrLabel = new JLabel(new ImageIcon(response.getQrImage()));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrPanel.add(qrLabel, BorderLayout.CENTER);
            add(qrPanel, BorderLayout.CENTER);

            // Info Panel
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel instructionLabel = new JLabel("<html><div style='text-align: center;'>Quét mã QR để thanh toán<br>Số tiền: "
                    + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(giaVe) + "</div></html>", SwingConstants.CENTER);
            instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(instructionLabel);

            infoPanel.add(Box.createVerticalStrut(10));

            JLabel timerLabel = new JLabel("Thời gian còn lại: 05:00", SwingConstants.CENTER);
            timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            timerLabel.setFont(new Font(timerLabel.getFont().getName(), Font.BOLD, 14));
            infoPanel.add(timerLabel);

            infoPanel.add(Box.createVerticalStrut(10));

            JLabel statusLabel = new JLabel("Đang chờ thanh toán...", SwingConstants.CENTER);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(statusLabel);

            add(infoPanel, BorderLayout.NORTH);

            // Button Panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

            JButton refreshButton = new JButton("Kiểm tra thanh toán");
            refreshButton.addActionListener(_ -> checkPaymentStatus(statusLabel));
            buttonPanel.add(refreshButton);

            JButton cancelButton = new JButton("Hủy");
            cancelButton.addActionListener(e -> {
                int option = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc muốn hủy thanh toán này?",
                        "Xác nhận hủy",
                        JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    dispose();
                }
            });
            buttonPanel.add(cancelButton);

            add(buttonPanel, BorderLayout.SOUTH);

            // Timer
            final int[] timeRemaining = {300}; // 5 phút
            Timer timer = new Timer(1000, e -> {
                timeRemaining[0]--;
                int minutes = timeRemaining[0] / 60;
                int seconds = timeRemaining[0] % 60;
                timerLabel.setText(String.format("Thời gian còn lại: %02d:%02d", minutes, seconds));

                if (timeRemaining[0] <= 0) {
                    ((Timer) e.getSource()).stop();
                    statusLabel.setText("Hết thời gian thanh toán!");
                    JOptionPane.showMessageDialog(PaymentView.this,
                            "Đã hết thời gian thanh toán. Vui lòng thử lại.",
                            "Hết hạn",
                            JOptionPane.WARNING_MESSAGE);
                    dispose();
                }
            });
            timer.start();

            // Periodic payment status check
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            final ScheduledFuture<?> paymentChecker = scheduler.scheduleAtFixedRate(() -> {
                try {
                    PaymentStatus status = paymentController.checkPaymentStatus(transactionId);
                    if (status == PaymentStatus.COMPLETED) {
                        timer.stop();
                        scheduler.shutdown();
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Thanh toán thành công!");
                            paymentCallback.accept(new PaymentResult(suatChieu, ghe, giaVe, transactionId));
                            JOptionPane.showMessageDialog(PaymentView.this,
                                    "Thanh toán thành công! Vé đã được xác nhận.",
                                    "Thành công",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, 5, 5, TimeUnit.SECONDS);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    timer.stop();
                    scheduler.shutdown();
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo thanh toán: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void checkPaymentStatus(JLabel statusLabel) {
        try {
            PaymentStatus status = paymentController.checkPaymentStatus(transactionId);

            if (status == PaymentStatus.COMPLETED) {
                statusLabel.setText("Thanh toán thành công!");
                paymentCallback.accept(new PaymentResult(suatChieu, ghe, giaVe, transactionId));
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thành công! Vé đã được xác nhận.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
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
                dispose();
            } else {
                statusLabel.setText("Đang chờ thanh toán...");
                JOptionPane.showMessageDialog(this,
                        "Chưa nhận được thanh toán. Vui lòng quét mã QR và hoàn tất thanh toán.",
                        "Đang chờ",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            statusLabel.setText("Lỗi kiểm tra thanh toán!");
            JOptionPane.showMessageDialog(this,
                    "Lỗi kiểm tra thanh toán: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
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