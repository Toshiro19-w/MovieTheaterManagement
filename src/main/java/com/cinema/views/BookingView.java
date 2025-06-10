package com.cinema.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.cinema.controllers.DatVeController;
import com.cinema.controllers.PaymentController;
import com.cinema.models.Ghe;
import com.cinema.models.SuatChieu;
import com.cinema.models.dto.BookingResultDTO;

public class BookingView extends JDialog {
    // Màu sắc và font chữ giống với MainView
    private static final Color CINESTAR_BLUE = new Color(0, 51, 102);
    private static final Color CINESTAR_YELLOW = new Color(255, 204, 0);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color AVAILABLE_SEAT_COLOR = new Color(102, 187, 106);
    private static final Color SELECTED_SEAT_COLOR = CINESTAR_YELLOW;
    private static final Color BOOKED_SEAT_COLOR = new Color(239, 83, 80);
    private static final Font HEADER_FONT = new Font("Roboto", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Roboto", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Roboto", Font.BOLD, 14);
    
    private final DatVeController datVeController;
    private final PaymentController paymentController;
    private final int maPhim;
    private final int maKhachHang, maNhanVien;
    private final Consumer<BookingResultDTO> confirmCallback;
    private SuatChieu selectedSuatChieu;
    private List<Ghe> selectedGheList = new ArrayList<>();
    private BigDecimal ticketPrice;
    private Integer maVe;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public BookingView(JFrame parent, DatVeController datVeController, PaymentController paymentController, int maPhim, int maKhachHang, int maNhanVien, Consumer<BookingResultDTO> confirmCallback) {
        super(parent, "Đặt vé", true);
        this.datVeController = datVeController;
        this.paymentController = paymentController;
        this.maPhim = maPhim;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.confirmCallback = confirmCallback;

        setSize(900, 700);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BACKGROUND_COLOR);

        initializeComponents();
    }

    private void initializeComponents() {
        // Header panel với gradient
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, CINESTAR_BLUE, getWidth(), 0, new Color(0, 102, 204));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Đặt vé xem phim");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel suất chiếu
        JPanel suatChieuPanel = new JPanel();
        suatChieuPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        suatChieuPanel.setBackground(BACKGROUND_COLOR);
        suatChieuPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CINESTAR_BLUE), "Thông tin suất chiếu"));

        JLabel suatChieuLabel = new JLabel("Chọn suất chiếu:");
        suatChieuLabel.setFont(LABEL_FONT);
        JComboBox<SuatChieu> suatChieuCombo = new JComboBox<>();
        suatChieuCombo.setFont(LABEL_FONT);
        suatChieuCombo.setPreferredSize(new Dimension(300, 30));
        
        try {
            List<SuatChieu> suatChieuList = datVeController.getSuatChieuByPhim(maPhim);

            if (suatChieuList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không còn suất chiếu khả dụng cho phim này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                dispose();
                return;
            }

            for (SuatChieu suatChieu : suatChieuList) {
                suatChieuCombo.addItem(suatChieu);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải suất chiếu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        suatChieuPanel.add(suatChieuLabel);
        suatChieuPanel.add(Box.createHorizontalStrut(10));
        suatChieuPanel.add(suatChieuCombo);

        // Panel chú thích
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        legendPanel.setBackground(BACKGROUND_COLOR);
        
        addLegendItem(legendPanel, "Ghế trống", AVAILABLE_SEAT_COLOR);
        addLegendItem(legendPanel, "Ghế đã chọn", SELECTED_SEAT_COLOR);
        addLegendItem(legendPanel, "Ghế đã đặt", BOOKED_SEAT_COLOR);
        
        // Panel sơ đồ ghế
        JPanel seatContainerPanel = new JPanel(new BorderLayout());
        seatContainerPanel.setBackground(BACKGROUND_COLOR);
        seatContainerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CINESTAR_BLUE), "Sơ đồ ghế"));
        
        // Thêm màn hình phía trên
        JPanel screenPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CINESTAR_BLUE);
                g2d.fillRect(50, 0, getWidth() - 100, 10);
                g2d.setFont(new Font("Roboto", Font.BOLD, 12));
                g2d.drawString("MÀN HÌNH", getWidth()/2 - 30, 25);
            }
        };
        screenPanel.setPreferredSize(new Dimension(0, 40));
        screenPanel.setBackground(BACKGROUND_COLOR);
        seatContainerPanel.add(screenPanel, BorderLayout.NORTH);
        
        // Panel ghế
        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(0, 10, 10, 10));
        seatPanel.setBackground(BACKGROUND_COLOR);
        seatPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Label hiển thị ghế đã chọn
        JLabel selectedSeatLabel = new JLabel("Ghế đã chọn: Chưa chọn");
        selectedSeatLabel.setFont(LABEL_FONT);
        selectedSeatLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        suatChieuCombo.addActionListener(_ -> {
            seatPanel.removeAll();
            selectedGheList.clear();
            updateSelectedSeatsInfo(selectedSeatLabel);
            selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
            ticketPrice = null;
            maVe = null;

            if (selectedSuatChieu != null) {
                try {
                    List<Ghe> allSeats = datVeController.getAllGheByPhong(selectedSuatChieu.getMaPhong());
                    List<Ghe> availableSeats = datVeController.getGheTrongByPhongAndSuatChieu(
                            selectedSuatChieu.getMaPhong(), selectedSuatChieu.getMaSuatChieu());
                    ticketPrice = datVeController.getTicketPriceBySuatChieu(selectedSuatChieu.getMaSuatChieu());
                    if (ticketPrice == null) {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy giá vé!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    for (Ghe seat : allSeats) {
                        SeatButton seatButton = new SeatButton(seat.getSoGhe());
                        boolean isAvailable = availableSeats.stream().anyMatch(ghe -> ghe.getSoGhe().equals(seat.getSoGhe()));
                        
                        if (isAvailable) {
                            seatButton.setAvailable(true);
                            seatButton.addActionListener(_ -> {
                                // Kiểm tra xem ghế đã được chọn chưa
                                boolean isSelected = selectedGheList.stream()
                                        .anyMatch(ghe -> ghe.getSoGhe().equals(seat.getSoGhe()));
                                
                                if (isSelected) {
                                    // Nếu đã chọn thì bỏ chọn
                                    selectedGheList.removeIf(ghe -> ghe.getSoGhe().equals(seat.getSoGhe()));
                                    seatButton.setSelected(false);
                                } else {
                                    // Nếu chưa chọn thì thêm vào danh sách
                                    Ghe selectedGhe = allSeats.stream()
                                            .filter(ghe -> ghe.getSoGhe().equals(seat.getSoGhe()))
                                            .findFirst()
                                            .orElse(null);
                                    if (selectedGhe != null) {
                                        selectedGheList.add(selectedGhe);
                                        seatButton.setSelected(true);
                                    }
                                }
                                
                                updateSelectedSeatsInfo(selectedSeatLabel);
                            });
                        } else {
                            seatButton.setAvailable(false);
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

        JScrollPane seatScrollPane = new JScrollPane(seatPanel);
        seatScrollPane.setBorder(null);
        seatScrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        seatContainerPanel.add(seatScrollPane, BorderLayout.CENTER);

        // Panel thông tin và nút xác nhận
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JPanel selectedInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectedInfoPanel.setBackground(BACKGROUND_COLOR);
        selectedInfoPanel.add(selectedSeatLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton bookButton = createStyledButton("Xác nhận đặt vé");
        bookButton.addActionListener(_ -> {
            selectedSuatChieu = (SuatChieu) suatChieuCombo.getSelectedItem();
            
            if (selectedSuatChieu == null || selectedGheList.isEmpty() || ticketPrice == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn suất chiếu và ghế!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Tạo thông báo xác nhận với thông tin chi tiết
            String selectedSeats = selectedGheList.stream()
                    .map(Ghe::getSoGhe)
                    .collect(Collectors.joining(", "));
            BigDecimal totalPrice = ticketPrice.multiply(BigDecimal.valueOf(selectedGheList.size()));
            String formattedPrice = currencyFormat.format(totalPrice);
            
            String confirmMessage = "Xác nhận thông tin đặt vé:\n\n" +
                    "- Suất chiếu: " + selectedSuatChieu + "\n" +
                    "- Phòng: " + selectedSuatChieu.getMaPhong() + "\n" +
                    "- Ghế: " + selectedSeats + "\n" +
                    "- Số lượng vé: " + selectedGheList.size() + "\n" +
                    "- Tổng tiền: " + formattedPrice + "\n\n" +
                    "Bạn có chắc chắn muốn đặt vé không?";
            
            int option = JOptionPane.showConfirmDialog(
                    this,
                    confirmMessage,
                    "Xác nhận đặt vé",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
            
            try {
                // Tạo vé chờ thanh toán cho từng ghế đã chọn
                List<Integer> maVeList = new ArrayList<>();
                for (Ghe ghe : selectedGheList) {
                    datVeController.createPendingVe(
                            selectedSuatChieu.getMaSuatChieu(),
                            ghe.getMaPhong(),
                            ghe.getSoGhe(),
                            ticketPrice,
                            maKhachHang
                    );
                    
                    Integer maVe = datVeController.getPendingVeFromBooking(
                            selectedSuatChieu.getMaSuatChieu(),
                            ghe.getSoGhe(),
                            maKhachHang
                    );
                    
                    if (maVe != null) {
                        maVeList.add(maVe);
                    }
                }
                
                if (maVeList.isEmpty()) {
                    throw new SQLException("Không thể tạo vé");
                }
                
                // Lưu mã vé đầu tiên để sử dụng trong PaymentView
                maVe = maVeList.get(0);
                
                JOptionPane.showMessageDialog(this, "Đã tạo vé chờ thanh toán! Vui lòng tiến hành thanh toán.", "Thành công", JOptionPane.INFORMATION_MESSAGE);

                // Mở PaymentView ngay sau khi tạo vé chờ thanh toán thành công
                PaymentView paymentView = new PaymentView((JFrame) getParent(), paymentController, datVeController, selectedSuatChieu, selectedGheList.get(0), totalPrice, maVe, maKhachHang, maNhanVien, paymentResult -> {
                    if (paymentResult != null) {
                        confirmCallback.accept(new BookingResultDTO(selectedSuatChieu,
                                selectedGheList.get(0),
                                totalPrice,
                                paymentResult.transactionId));
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Thanh toán đã bị hủy.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                });
                paymentView.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đặt vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
        
        buttonPanel.add(bookButton);
        
        infoPanel.add(selectedInfoPanel, BorderLayout.WEST);
        infoPanel.add(buttonPanel, BorderLayout.EAST);

        // Thêm các panel vào panel chính
        mainPanel.add(suatChieuPanel, BorderLayout.NORTH);
        mainPanel.add(legendPanel, BorderLayout.CENTER);
        mainPanel.add(seatContainerPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);

        // Chọn suất chiếu đầu tiên mặc định
        if (suatChieuCombo.getItemCount() > 0) {
            suatChieuCombo.setSelectedIndex(0);
        }
    }
    
    private void addLegendItem(JPanel panel, String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setBackground(BACKGROUND_COLOR);
        
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Roboto", Font.PLAIN, 12));
        
        item.add(colorBox);
        item.add(label);
        panel.add(item);
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(CINESTAR_YELLOW);
        button.setForeground(CINESTAR_BLUE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 215, 64));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(CINESTAR_YELLOW);
            }
        });
        
        return button;
    }
    
    private void updateSelectedSeatsInfo(JLabel selectedSeatLabel) {
        if (selectedGheList.isEmpty()) {
            selectedSeatLabel.setText("Ghế đã chọn: Chưa chọn");
            return;
        }
        
        String selectedSeats = selectedGheList.stream()
                .map(Ghe::getSoGhe)
                .collect(Collectors.joining(", "));
        
        BigDecimal totalPrice = ticketPrice.multiply(BigDecimal.valueOf(selectedGheList.size()));
        String formattedPrice = currencyFormat.format(totalPrice);
        
        selectedSeatLabel.setText("Ghế đã chọn: " + selectedSeats + " - Tổng tiền: " + formattedPrice);
    }

    // Custom button cho ghế
    private class SeatButton extends JButton {
        private boolean isAvailable = false;
        private boolean isSelected = false;
        
        public SeatButton(String text) {
            super(text);
            setPreferredSize(new Dimension(50, 50));
            setFont(new Font("Roboto", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
            updateAppearance();
        }
        
        public void setAvailable(boolean available) {
            this.isAvailable = available;
            setEnabled(available);
            updateAppearance();
        }
        
        public void setSelected(boolean selected) {
            this.isSelected = selected;
            updateAppearance();
        }
        
        private void updateAppearance() {
            if (!isAvailable) {
                setBackground(BOOKED_SEAT_COLOR);
                setForeground(Color.WHITE);
            } else if (isSelected) {
                setBackground(SELECTED_SEAT_COLOR);
                setForeground(CINESTAR_BLUE);
            } else {
                setBackground(AVAILABLE_SEAT_COLOR);
                setForeground(Color.WHITE);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 10, 10);
            g2.setColor(getBackground().darker());
            g2.fillRoundRect(0, getHeight() - 5, getWidth(), 5, 5, 5);
            
            // Vẽ text
            g2.setColor(getForeground());
            g2.setFont(getFont());
            String text = getText();
            int width = g2.getFontMetrics().stringWidth(text);
            int height = g2.getFontMetrics().getHeight();
            g2.drawString(text, (getWidth() - width) / 2, (getHeight() - height) / 2 + g2.getFontMetrics().getAscent());
            
            g2.dispose();
        }
    }
}