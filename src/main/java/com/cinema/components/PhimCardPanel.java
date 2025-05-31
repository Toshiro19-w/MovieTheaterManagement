package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.cinema.controllers.KhachHangController;
import com.cinema.models.Phim;
import com.cinema.services.KhachHangService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.TimeFormatter;

public class PhimCardPanel extends JPanel {
    private Phim phim;
    private BiConsumer<Integer, Integer> bookTicketCallback;
    private Consumer<Phim> detailCallback;
    private String username;
    
    public PhimCardPanel(Phim phim, BiConsumer<Integer, Integer> bookTicketCallback, 
                        Consumer<Phim> detailCallback, String username) {
        this.phim = phim;
        this.bookTicketCallback = bookTicketCallback;
        this.detailCallback = detailCallback;
        this.username = username;
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setPreferredSize(new Dimension(220, 320));
        setBackground(Color.WHITE);
        
        initComponents();
    }
    
    private void initComponents() {
        // Poster
        JLabel posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(220, 220));
        
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                File posterFile = new File(phim.getDuongDanPoster());
                if (posterFile.exists()) {
                    ImageIcon icon = new ImageIcon(phim.getDuongDanPoster());
                    Image img = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
                    posterLabel.setIcon(new ImageIcon(img));
                } else {
                    posterLabel.setText("Không có ảnh");
                }
            } catch (Exception e) {
                posterLabel.setText("Không có ảnh");
            }
        } else {
            posterLabel.setText("Không có ảnh");
        }
        
        add(posterLabel, BorderLayout.NORTH);
        
        // Thông tin phim
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("<html><b>" + phim.getTenPhim() + "</b></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel detailsLabel = new JLabel("<html><center>" + 
                phim.getTenTheLoai() + " | " + 
                TimeFormatter.formatMinutesToHoursAndMinutes(phim.getThoiLuong()) + 
                "</center></html>", SwingConstants.CENTER);
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoPanel.add(detailsLabel, BorderLayout.CENTER);
        
        // Nút đặt vé
        JButton datVeButton = new JButton("Đặt vé");
        datVeButton.setBackground(new Color(0, 48, 135));
        datVeButton.setForeground(Color.WHITE);
        datVeButton.setFont(new Font("Arial", Font.BOLD, 14));
        datVeButton.setBorderPainted(false);
        datVeButton.setFocusPainted(false);
        
        datVeButton.addActionListener(e -> {
            try {
                KhachHangController khachHangController = new KhachHangController(
                        new KhachHangService(new DatabaseConnection()));
                int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
                bookTicketCallback.accept(phim.getMaPhim(), maKhachHang);
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        
        datVeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                datVeButton.setBackground(new Color(0, 72, 202));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                datVeButton.setBackground(new Color(0, 48, 135));
            }
        });
        
        infoPanel.add(datVeButton, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.CENTER);
        
        // Thêm sự kiện click vào card để xem chi tiết
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == PhimCardPanel.this && !datVeButton.getBounds().contains(e.getPoint())) {
                    detailCallback.accept(phim);
                }
            }
        });
    }
}