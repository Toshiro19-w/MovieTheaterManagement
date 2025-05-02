package com.cinema.views;

import com.cinema.controllers.DatVeController;
import com.cinema.models.SuatChieu;
import com.cinema.models.Ghe;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class BookingView extends JDialog {
    private final DatVeController datVeController;
    private final int maPhim;
    private final Consumer<BookingResult> confirmCallback;
    private SuatChieu selectedSuatChieu;
    private Ghe selectedGhe;

    public BookingView(JFrame parent, DatVeController datVeController, int maPhim, Consumer<BookingResult> confirmCallback) {
        super(parent, "Đặt vé", true);
        this.datVeController = datVeController;
        this.maPhim = maPhim;
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

        JPanel seatPanel = new JPanel(new GridLayout(5, 10, 5, 5));
        seatPanel.setBorder(BorderFactory.createTitledBorder("Sơ đồ ghế"));
        JLabel selectedSeatLabel = new JLabel("Ghế đã chọn: None");
        Ghe[] selectedGheArray = {null};

        suatChieuCombo.addActionListener(_ -> {
            seatPanel.removeAll();
            selectedGheArray[0] = null;
            selectedSeatLabel.setText("Ghế đã chọn: None");
            selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
            if (selectedSuatChieu != null) {
                try {
                    List<Ghe> gheList = datVeController.getGheTrongByPhongAndSuatChieu(
                            selectedSuatChieu.getMaPhong(), selectedSuatChieu.getMaSuatChieu());
                    String[] allSeats = new String[50];
                    for (int i = 0; i < 5; i++) {
                        for (int j = 1; j <= 10; j++) {
                            allSeats[i * 10 + j - 1] = (char) ('A' + i) + String.valueOf(j);
                        }
                    }

                    for (String seat : allSeats) {
                        JButton seatButton = new JButton(seat);
                        seatButton.setPreferredSize(new Dimension(50, 50));
                        boolean isAvailable = gheList.stream().anyMatch(ghe -> ghe.getSoGhe().equals(seat));
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
                                selectedGheArray[0] = gheList.stream()
                                        .filter(ghe -> ghe.getSoGhe().equals(seat))
                                        .findFirst()
                                        .orElse(null);
                                seatButton.setBackground(Color.YELLOW);
                                selectedSeatLabel.setText("Ghế đã chọn: " + seat);
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
        bottomPanel.add(selectedSeatLabel, BorderLayout.NORTH);

        JButton confirmButton = new JButton("Xác nhận đặt vé");
        confirmButton.addActionListener(_ -> {
            selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
            selectedGhe = selectedGheArray[0];
            if (selectedSuatChieu == null || selectedGhe == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu và ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                datVeController.datVe(
                        selectedSuatChieu.getMaSuatChieu(),
                        selectedGhe.getMaPhong(),
                        selectedGhe.getSoGhe(),
                        new java.math.BigDecimal("50000")
                );
                confirmCallback.accept(new BookingResult(selectedSuatChieu, selectedGhe, new java.math.BigDecimal("50000")));
                dispose();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đặt vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomPanel.add(confirmButton, BorderLayout.SOUTH);

        add(suatChieuPanel, BorderLayout.NORTH);
        add(new JScrollPane(seatPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        if (suatChieuCombo.getItemCount() > 0) {
            suatChieuCombo.setSelectedIndex(0);
        }
    }

    public static class BookingResult {
        public final SuatChieu suatChieu;
        public final Ghe ghe;
        public final java.math.BigDecimal giaVe;

        public BookingResult(SuatChieu suatChieu, Ghe ghe, java.math.BigDecimal giaVe) {
            this.suatChieu = suatChieu;
            this.ghe = ghe;
            this.giaVe = giaVe;
        }
    }
}