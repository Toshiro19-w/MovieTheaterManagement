package com.cinema.views;

import com.cinema.controllers.DatVeController;
import com.cinema.controllers.PaymentController;
import com.cinema.models.SuatChieu;
import com.cinema.models.Ghe;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.math.BigDecimal;

public class BookingView extends JDialog {
    private final DatVeController datVeController;
    private final PaymentController paymentController;
    private final int maPhim;
    private final int maKhachHang;
    private final Consumer<BookingResult> confirmCallback;
    private SuatChieu selectedSuatChieu;
    private Ghe selectedGhe;
    private BigDecimal ticketPrice;
    private Integer maVe;

    public BookingView(JFrame parent, DatVeController datVeController, PaymentController paymentController, int maPhim, int maKhachHang, Consumer<BookingResult> confirmCallback) {
        super(parent, "Đặt vé", true);
        this.datVeController = datVeController;
        this.paymentController = paymentController;
        this.maPhim = maPhim;
        this.maKhachHang = maKhachHang;
        this.confirmCallback = confirmCallback;

        setSize(800, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        initializeComponents();
    }

    private void initializeComponents() {
        //Hiện suất chiếu
        JPanel suatChieuPanel = new JPanel(new FlowLayout());
        JLabel suatChieuLabel = new JLabel("Chọn suất chiếu:");
        JComboBox<SuatChieu> suatChieuCombo = new JComboBox<>();
        try {
            List<SuatChieu> suatChieuList = datVeController.getSuatChieuByPhim(maPhim);
            for (SuatChieu sc : suatChieuList) {
                if (sc.getSoSuatChieu() > 0) {
                    suatChieuCombo.addItem(sc);
                }
            }
            if (suatChieuCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không còn suất chiếu khả dụng cho phim này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                dispose();
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải suất chiếu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        //Sơ đồ ghế
        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(0, 10, 5, 5));
        seatPanel.setBorder(BorderFactory.createTitledBorder("Sơ đồ ghế"));
        JLabel selectedSeatLabel = new JLabel("Ghế đã chọn: None");
        Ghe[] selectedGheArray = {null};

        suatChieuCombo.addActionListener(_ -> {
            seatPanel.removeAll();
            selectedGheArray[0] = null;
            selectedSeatLabel.setText("Ghế đã chọn: None");
            selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
            ticketPrice = null;
            maVe = null;

            if (selectedSuatChieu != null) {
                try {
                    if (selectedSuatChieu.getSoSuatChieu() <= 0) {
                        JOptionPane.showMessageDialog(this, "Suất chiếu này đã hết vé!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                        seatPanel.removeAll();
                        seatPanel.revalidate();
                        seatPanel.repaint();
                        return;
                    }

                    List<Ghe> allSeats = datVeController.getAllGheByPhong(selectedSuatChieu.getMaPhong());
                    List<Ghe> availableSeats = datVeController.getGheTrongByPhongAndSuatChieu(
                            selectedSuatChieu.getMaPhong(), selectedSuatChieu.getMaSuatChieu());
                    ticketPrice = datVeController.getTicketPriceBySuatChieu(selectedSuatChieu.getMaSuatChieu());
                    if (ticketPrice == null) {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy giá vé!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    for (Ghe seat : allSeats) {
                        JButton seatButton = new JButton(seat.getSoGhe());
                        seatButton.setPreferredSize(new Dimension(50, 50));
                        boolean isAvailable = availableSeats.stream().anyMatch(ghe -> ghe.getSoGhe().equals(seat.getSoGhe()));
                        if (isAvailable) {
                            seatButton.setBackground(Color.GREEN);
                            seatButton.addActionListener(_ -> {
                                if (selectedGheArray[0] != null) {
                                    for (Component comp : seatPanel.getComponents()) {
                                        if (comp instanceof JButton && ((JButton) comp).getText().equals(selectedGheArray[0].getSoGhe())) {
                                            comp.setBackground(Color.GREEN);
                                            break;
                                        }
                                    }
                                }
                                selectedGheArray[0] = allSeats.stream()
                                        .filter(ghe -> ghe.getSoGhe().equals(seat.getSoGhe()))
                                        .findFirst()
                                        .orElse(null);
                                seatButton.setBackground(Color.YELLOW);
                                selectedSeatLabel.setText("Ghế đã chọn: " + seat.getSoGhe() + " (Giá: " + ticketPrice + ")");
                                selectedGhe = selectedGheArray[0];
                            });
                        } else {
                            seatButton.setBackground(Color.RED);
                            seatButton.setEnabled(false);
                        }
                        seatPanel.add(seatButton);
                    }
                    seatPanel.revalidate();
                    seatPanel.repaint();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Lỗi khi tải danh sách ghế: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        suatChieuPanel.add(suatChieuLabel);
        suatChieuPanel.add(suatChieuCombo);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        bottomPanel.add(selectedSeatLabel, BorderLayout.NORTH);

        //Nút xác nhận
        JButton bookButton = ConfirmButton(suatChieuCombo, selectedGheArray);
        buttonPanel.add(bookButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(suatChieuPanel, BorderLayout.NORTH);
        add(new JScrollPane(seatPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        if (suatChieuCombo.getItemCount() > 0) {
            suatChieuCombo.setSelectedIndex(0);
        }
    }

    //Nút xác nhận
    private JButton ConfirmButton(JComboBox<SuatChieu> suatChieuCombo, Ghe[] selectedGheArray) {
        JButton bookButton = new JButton("Xác nhận");
        bookButton.addActionListener(_ -> {
            selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
            selectedGhe = selectedGheArray[0];
            if (selectedSuatChieu == null || selectedGhe == null || ticketPrice == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu và ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                datVeController.datVe(
                        selectedSuatChieu.getMaSuatChieu(),
                        selectedGhe.getMaPhong(),
                        selectedGhe.getSoGhe(),
                        ticketPrice,
                        maKhachHang
                );
                maVe = datVeController.getMaVeFromBooking(
                        selectedSuatChieu.getMaSuatChieu(),
                        selectedGhe.getSoGhe(),
                        maKhachHang
                );
                JOptionPane.showMessageDialog(this, "Đặt vé thành công! Vui lòng tiến hành thanh toán.", "Thành công", JOptionPane.INFORMATION_MESSAGE);

                // Mở PaymentView ngay sau khi đặt vé thành công
                PaymentView paymentView = new PaymentView((JFrame) getParent(), paymentController, datVeController, selectedSuatChieu, selectedGhe, ticketPrice, maVe, maKhachHang, paymentResult -> {
                    if (paymentResult != null) {
                        confirmCallback.accept(new BookingResult(selectedSuatChieu,
                                selectedGhe,
                                ticketPrice,
                                paymentResult.transactionId));
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Thanh toán đã bị hủy.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                });
                paymentView.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đặt vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        return bookButton;
    }

    public record BookingResult(SuatChieu suatChieu, Ghe ghe, BigDecimal giaVe, String transactionId) {}
}