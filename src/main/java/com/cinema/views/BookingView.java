package com.cinema.views;

import com.cinema.controllers.DatVeController;
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
    private final int maPhim;
    private final int maKhachHang;
    private final Consumer<BookingResult> confirmCallback;
    private SuatChieu selectedSuatChieu;
    private Ghe selectedGhe;
    private BigDecimal ticketPrice;
    private Integer maVe; // Track maVe for payment confirmation


    public BookingView(JFrame parent, DatVeController datVeController, int maPhim, int maKhachHang, Consumer<BookingResult> confirmCallback) {
        super(parent, "Đặt vé", true);
        this.datVeController = datVeController;
        this.maPhim = maPhim;
        this.maKhachHang = maKhachHang;
        this.confirmCallback = confirmCallback;

        setSize(800, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        initializeComponents();
    }

    private void initializeComponents() {
        JPanel suatChieuPanel = new JPanel(new FlowLayout());
        JLabel suatChieuLabel = new JLabel("Chọn suất chiếu:");
        JComboBox<SuatChieu> suatChieuCombo = new JComboBox<>();
        try {
            List<SuatChieu> suatChieuList = datVeController.getSuatChieuByPhim(maPhim);
            for (SuatChieu sc : suatChieuList) {
                suatChieuCombo.addItem(sc);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải suất chiếu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(0, 10, 5, 5)); // Dynamic rows, 10 columns
        seatPanel.setBorder(BorderFactory.createTitledBorder("Sơ đồ ghế"));
        JLabel selectedSeatLabel = new JLabel("Ghế đã chọn: None");
        Ghe[] selectedGheArray = {null};

        suatChieuCombo.addActionListener(_ -> {
            seatPanel.removeAll();
            selectedGheArray[0] = null;
            selectedSeatLabel.setText("Ghế đã chọn: None");
            selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
            ticketPrice = null;

            maVe = null; // Reset maVe

            if (selectedSuatChieu != null) {
                try {
                    // Get all seats for the room
                    List<Ghe> allSeats = datVeController.getAllGheByPhong(selectedSuatChieu.getMaPhong());
                    // Get available seats for the showtime
                    List<Ghe> availableSeats = datVeController.getGheTrongByPhongAndSuatChieu(
                            selectedSuatChieu.getMaPhong(), selectedSuatChieu.getMaSuatChieu());


                    // Get ticket price for this showtime

                    // Get ticket price for this showtime (assume uniform pricing for simplicity)

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

        JButton bookButton = new JButton("Xác nhận đặt vé");
        bookButton.addActionListener(_ -> {
            selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
            selectedGhe = selectedGheArray[0];
            if (selectedSuatChieu == null || selectedGhe == null || ticketPrice == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu và ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                // Book the ticket
                datVeController.datVe(
                        selectedSuatChieu.getMaSuatChieu(),
                        selectedGhe.getMaPhong(),
                        selectedGhe.getSoGhe(),
                        ticketPrice,
                        maKhachHang

                );
                // Retrieve maVe
                maVe = datVeController.getMaVeFromBooking(
                        selectedSuatChieu.getMaSuatChieu(),
                        selectedGhe.getSoGhe(),
                        maKhachHang
                );
                confirmCallback.accept(new BookingResult(selectedSuatChieu, selectedGhe, ticketPrice));
                JOptionPane.showMessageDialog(this, "Đặt vé và tạo hóa đơn thành công! Vui lòng kiểm tra thanh toán.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi đặt vé hoặc tạo hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton paymentButton = new JButton("Kiểm tra thanh toán");
        paymentButton.addActionListener(_ -> {
            if (maVe == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng đặt vé trước khi kiểm tra thanh toán!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                // Confirm payment
                int maHoaDon = datVeController.getMaHoaDonFromVe(maVe);
                datVeController.confirmPayment(maVe, maHoaDon);
                JOptionPane.showMessageDialog(this, "Thanh toán thành công! Vé đã được xác nhận.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xác nhận thanh toán: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                confirmCallback.accept(new BookingResult(selectedSuatChieu, selectedGhe, ticketPrice));
                JOptionPane.showMessageDialog(this, "Đặt vé và tạo hóa đơn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        buttonPanel.add(bookButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(suatChieuPanel, BorderLayout.NORTH);
        add(new JScrollPane(seatPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        if (suatChieuCombo.getItemCount() > 0) {
            suatChieuCombo.setSelectedIndex(0);
        }
    }

    public record BookingResult(SuatChieu suatChieu, Ghe ghe, BigDecimal giaVe) {}
}