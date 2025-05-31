package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.cinema.models.Phim;

public class PhimCarouselPanel extends JPanel {
    private JPanel cardsPanel;
    private HorizontalPaginationPanel paginationPanel;
    private List<Phim> phimList;
    private int currentPage = 1;
    private int itemsPerPage = 4;
    private BiConsumer<Integer, Integer> bookTicketCallback;
    private Consumer<Phim> detailCallback;
    private String username;
    private String title;
    
    public PhimCarouselPanel(String title, BiConsumer<Integer, Integer> bookTicketCallback, 
                            Consumer<Phim> detailCallback, String username) {
        this.title = title;
        this.bookTicketCallback = bookTicketCallback;
        this.detailCallback = detailCallback;
        this.username = username;
        this.phimList = new ArrayList<>();
        
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        
        // Tiêu đề
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 48, 135));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Panel chứa các card phim
        cardsPanel = new JPanel(new GridLayout(1, itemsPerPage, 15, 0));
        cardsPanel.setBackground(Color.WHITE);
        add(cardsPanel, BorderLayout.CENTER);
        
        // Panel phân trang
        paginationPanel = new HorizontalPaginationPanel();
        paginationPanel.setPageChangeListener(this::loadPage);
        add(paginationPanel, BorderLayout.SOUTH);
    }
    
    public void setPhimList(List<Phim> phimList) {
        this.phimList = phimList;
        this.currentPage = 1;
        int totalPages = (int) Math.ceil((double) phimList.size() / itemsPerPage);
        paginationPanel.updatePagination(currentPage, totalPages);
        loadPage(currentPage);
    }
    
    private void loadPage(int page) {
        cardsPanel.removeAll();
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, phimList.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Phim phim = phimList.get(i);
            PhimCardPanel card = new PhimCardPanel(phim, bookTicketCallback, detailCallback, username);
            cardsPanel.add(card);
        }
        
        // Nếu không đủ số lượng card, thêm panel trống
        for (int i = endIndex - startIndex; i < itemsPerPage; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Color.WHITE);
            cardsPanel.add(emptyPanel);
        }
        
        currentPage = page;
        int totalPages = (int) Math.ceil((double) phimList.size() / itemsPerPage);
        paginationPanel.updatePagination(currentPage, totalPages);
        
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
}