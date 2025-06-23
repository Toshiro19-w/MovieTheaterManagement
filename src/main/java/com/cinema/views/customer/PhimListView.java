package com.cinema.views.customer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.cinema.components.DanhGiaPanel;
import com.cinema.components.PhimCarouselPanel;
import com.cinema.controllers.KhachHangController;
import com.cinema.controllers.PhimController;
import com.cinema.models.Phim;
import com.cinema.models.SuatChieu;
import com.cinema.models.repositories.SuatChieuRepository;
import com.cinema.services.KhachHangService;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.TimeFormatter;

public class PhimListView extends JPanel {
    private final PhimService phimService;
    private final SuatChieuRepository suatChieuRepository;
    private final KhachHangController khachHangController;
    private final BiConsumer<Integer, Integer> bookTicketCallback;
    private final String username;
    private PhimCarouselPanel dangChieuPanel;
    private PhimCarouselPanel sapChieuPanel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private JPanel searchPanel;
    private JTextField searchTextField;
    private JPopupMenu suggestionPopup;
    private JComboBox<String> dateComboBox;
    private JButton backButton;
    private JPanel mainContentPanel;
    private JPanel listPanel;
    private JPanel contentPanel;
    private JLabel noResultsLabel;
    private JScrollPane mainScrollPane;

    public PhimListView(PhimController phimController, BiConsumer<Integer, Integer> bookTicketCallback, String username) throws IOException, SQLException {
        this.phimService = new PhimService(new DatabaseConnection());
        this.suatChieuRepository = new SuatChieuRepository(new DatabaseConnection());
        this.khachHangController = new KhachHangController(new KhachHangService(new DatabaseConnection()));
        this.bookTicketCallback = bookTicketCallback;
        this.username = username;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeComponents();
        loadPhimList("", null);
    }

    private void initializeComponents() {
        // Search panel
        searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search by name
        JLabel searchLabel = new JLabel("Tìm theo tên: ");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(searchLabel);
        searchPanel.add(Box.createHorizontalStrut(5));

        searchTextField = new JTextField(20);
        searchTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        suggestionPopup = new JPopupMenu();
        
        // Add search suggestions
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateSuggestions(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateSuggestions(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateSuggestions(); }
            
            private void updateSuggestions() {
                String text = searchTextField.getText().trim().toLowerCase();
                suggestionPopup.removeAll();
                
                if (text.length() >= 2) {
                    try {
                        List<Phim> allPhims = phimService.getAllPhim();
                        List<String> suggestions = allPhims.stream()
                            .flatMap(phim -> Stream.of(phim.getTenPhim(), phim.getTenTheLoai()).filter(Objects::nonNull))
                            .filter(s -> s.toLowerCase().contains(text))
                            .distinct()
                            .limit(5) // Limit to 5 suggestions
                            .collect(Collectors.toList());
                        
                        System.out.println("Suggestions for '" + text + "': " + suggestions); // Debug log
                        
                        for (String suggestion : suggestions) {
                            JMenuItem item = new JMenuItem(suggestion);
                            item.addActionListener(e -> {
                                searchTextField.setText(suggestion);
                                suggestionPopup.setVisible(false);
                                loadPhimList(suggestion, null);
                            });
                            suggestionPopup.add(item);
                        }
                        
                        if (!suggestions.isEmpty()) {
                            suggestionPopup.pack();
                            suggestionPopup.show(searchTextField, 0, searchTextField.getHeight());
                            System.out.println("Showing suggestion popup with " + suggestions.size() + " items"); // Debug log
                        } else {
                            suggestionPopup.setVisible(false);
                            System.out.println("No suggestions found, hiding popup"); // Debug log
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(PhimListView.this, "Lỗi khi tải gợi ý: " + ex.getMessage(), 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                        System.err.println("SQL Error: " + ex.getMessage()); // Debug log
                    }
                } else {
                    suggestionPopup.setVisible(false);
                    System.out.println("Text too short, hiding popup"); // Debug log
                }
            }
        });
        
        // Hide suggestions when losing focus
        searchTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                suggestionPopup.setVisible(false);
                System.out.println("Focus lost, hiding suggestion popup"); // Debug log
            }
            
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchTextField.getText().trim().length() >= 2) {
//                    updateSuggestions();
                }
            }
        });
        
        searchPanel.add(searchTextField);
        searchPanel.add(Box.createHorizontalStrut(10));

        // Search by date
        JLabel dateLabel = new JLabel("Ngày chiếu: ");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(dateLabel);
        searchPanel.add(Box.createHorizontalStrut(5));

        dateComboBox = new JComboBox<>();
        dateComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        dateComboBox.setEditable(true);
        dateComboBox.setPreferredSize(new Dimension(120, 25));
        dateComboBox.addItem(""); // Default empty item
        updateDateComboBoxOptions();
        
        searchPanel.add(dateComboBox);
        searchPanel.add(Box.createHorizontalStrut(10));

        // Search button
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(new Color(0, 48, 135));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.addActionListener(e -> {
            String searchText = searchTextField.getText().trim();
            LocalDate selectedDate = null;
            String selectedDateStr = (String) dateComboBox.getSelectedItem();
            if (selectedDateStr != null && !selectedDateStr.isEmpty()) {
                try {
                    selectedDate = LocalDate.parse(selectedDateStr, formatter);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ: " + selectedDateStr, 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            loadPhimList(searchText, selectedDate);
            // Show back button
            if (backButton == null) {
                backButton = new JButton("Quay lại");
                backButton.setBackground(new Color(150, 150, 150));
                backButton.setForeground(Color.WHITE);
                backButton.setFont(new Font("Arial", Font.BOLD, 14));
                backButton.addActionListener(ev -> {
                    searchTextField.setText("");
                    dateComboBox.setSelectedIndex(0);
                    loadPhimList("", null);
                    searchPanel.remove(backButton);
                    backButton = null;
                    searchPanel.revalidate();
                    searchPanel.repaint();
                });
                searchPanel.add(Box.createHorizontalStrut(10));
                searchPanel.add(backButton);
                searchPanel.revalidate();
                searchPanel.repaint();
            }
        });
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        // Main content panel
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        
        // Movie list panel
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        // No results label
        noResultsLabel = new JLabel("Không có phim phù hợp");
        noResultsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        noResultsLabel.setForeground(Color.RED);
        noResultsLabel.setAlignmentX(CENTER_ALIGNMENT);
        noResultsLabel.setVisible(false);

        // Currently showing movies panel
        dangChieuPanel = new PhimCarouselPanel("Phim đang chiếu", bookTicketCallback, this::showPhimDetail, username);
        listPanel.add(dangChieuPanel);

        // Upcoming movies panel
        sapChieuPanel = new PhimCarouselPanel("Phim sắp chiếu", bookTicketCallback, this::showPhimDetail, username);
        listPanel.add(sapChieuPanel);

        // Content panel for list and no results message
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(noResultsLabel);

        // Scroll pane for movie list
        mainScrollPane = new JScrollPane(listPanel);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainContentPanel.add(mainScrollPane, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private void updateDateComboBoxOptions() {
        List<SuatChieu> allSuatChieu = suatChieuRepository.findAll();
        Set<LocalDate> showDates = allSuatChieu.stream()
            .map(suatChieu -> suatChieu.getNgayGioChieu().toLocalDate())
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(HashSet::new));
        
        dateComboBox.removeAllItems();
        dateComboBox.addItem(""); // Default empty item
        showDates.stream()
            .sorted()
            .map(date -> date.format(formatter))
            .forEach(dateComboBox::addItem);
    }

    public void loadPhimList(String searchText, LocalDate selectedDate) {
        try {
            List<Phim> allPhims = phimService.getAllPhim();
            LocalDate today = LocalDate.now();
            
            // Get movie IDs with showtimes if filtering by date
            final Set<Integer> phimIdsWithShowtime = new HashSet<>();
            if (selectedDate != null) {
                List<SuatChieu> suatChieu = suatChieuRepository.findAll();
                suatChieu.stream()
                    .filter(s -> s.getNgayGioChieu() != null && s.getNgayGioChieu().toLocalDate().equals(selectedDate))
                    .map(SuatChieu::getMaPhim)
                    .forEach(phimIdsWithShowtime::add);
            }
            
            // Filter currently showing movies (active status)
            List<Phim> phimDangChieu = allPhims.stream()
                .filter(p -> p.getTrangThai() != null && "active".equals(p.getTrangThai()))
                .filter(p -> selectedDate == null || phimIdsWithShowtime.contains(p.getMaPhim()))
                .collect(Collectors.toList());
            
            // Filter upcoming movies (upcoming status or future release date)
            List<Phim> phimSapChieu = allPhims.stream()
                .filter(p -> p.getTrangThai() != null && 
                       ("upcoming".equals(p.getTrangThai()) || 
                        (p.getNgayKhoiChieu() != null && p.getNgayKhoiChieu().isAfter(today))))
                .filter(p -> selectedDate == null || phimIdsWithShowtime.contains(p.getMaPhim()))
                .collect(Collectors.toList());
            
            // Filter by movie name or genre
            if (!searchText.isEmpty()) {
                String searchLower = searchText.toLowerCase();
                phimDangChieu = phimDangChieu.stream()
                    .filter(phim -> (phim.getTenPhim() != null && phim.getTenPhim().toLowerCase().contains(searchLower)) ||
                                    (phim.getTenTheLoai() != null && phim.getTenTheLoai().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
                
                phimSapChieu = phimSapChieu.stream()
                    .filter(phim -> (phim.getTenPhim() != null && phim.getTenPhim().toLowerCase().contains(searchLower)) ||
                                    (phim.getTenTheLoai() != null && phim.getTenTheLoai().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
            }
            
            // Always show "Phim đang chiếu" panel, show "Phim sắp chiếu" only if not empty
            dangChieuPanel.setVisible(true);
            sapChieuPanel.setVisible(!phimSapChieu.isEmpty());
            
            // Show no results message if both lists are empty for search results
            noResultsLabel.setVisible(!searchText.isEmpty() && phimDangChieu.isEmpty() && phimSapChieu.isEmpty());
            
            // Update carousels
            dangChieuPanel.setPhimList(phimDangChieu);
            sapChieuPanel.setPhimList(phimSapChieu);
            
            // Refresh UI
            contentPanel.revalidate();
            contentPanel.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phim: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showPhimDetail(Phim phim) {
        // Main panel for details and reviews
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBackground(Color.WHITE);
        
        // Header panel with title and back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        
        // Movie title
        JLabel titleLabel = new JLabel(phim.getTenPhim());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Back button
        JButton backButton = new JButton("←");
        backButton.setBackground(new Color(0, 48, 135));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> showPhimList());
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setBackground(new Color(0, 72, 202));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setBackground(new Color(0, 48, 135));
            }
        });
        headerPanel.add(backButton, BorderLayout.WEST);
        
        // Add header to main panel
        detailPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content panel for details and reviews
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Main panel for movie details
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Left panel with large poster
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(300, 400));

        JLabel posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
            try {
                ImageIcon posterIcon = null;
                File file = new File(phim.getDuongDanPoster());
                if (file.exists()) {
                    posterIcon = new ImageIcon(phim.getDuongDanPoster());
                } else {
                    String fileName = phim.getDuongDanPoster();
                    if (fileName.contains("\\") || fileName.contains("/")) {
                        fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1);
                    }
                    URL resourceUrl = getClass().getClassLoader().getResource("images/posters/" + fileName);
                    if (resourceUrl != null) {
                        posterIcon = new ImageIcon(resourceUrl);
                    } else {
                        String relativePath = "src/main/resources/images/posters/" + fileName;
                        File relativeFile = new File(relativePath);
                        if (relativeFile.exists()) {
                            posterIcon = new ImageIcon(relativePath);
                        }
                    }
                }
                
                if (posterIcon == null || posterIcon.getIconWidth() <= 0) {
                    posterLabel.setText("Không tìm thấy ảnh");
                } else {
                    Image scaledImage = posterIcon.getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH);
                    posterLabel.setIcon(new ImageIcon(scaledImage));
                }
            } catch (Exception e) {
                e.printStackTrace();
                posterLabel.setText("Lỗi tải ảnh");
            }
        } else {
            posterLabel.setText("Không có ảnh");
        }
        leftPanel.add(posterLabel, BorderLayout.CENTER);

        // Right panel with movie details
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(LEFT_ALIGNMENT);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Genre
        JLabel genreLabel = new JLabel("\uD83C\uDFAC Thể loại: " + (phim.getTenTheLoai() != null ? phim.getTenTheLoai() : "N/A"));
        genreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        genreLabel.setAlignmentX(LEFT_ALIGNMENT);
        genreLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Duration
        JLabel durationLabel = new JLabel("\u23F1 Thời lượng: " + TimeFormatter.formatMinutesToHoursAndMinutes(phim.getThoiLuong()));
        durationLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        durationLabel.setAlignmentX(LEFT_ALIGNMENT);
        durationLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Country
        JLabel countryLabel = new JLabel("\uD83C\uDF0E Quốc gia: " + (phim.getNuocSanXuat() != null ? phim.getNuocSanXuat() : "N/A"));
        countryLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        countryLabel.setAlignmentX(LEFT_ALIGNMENT);
        countryLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Director
        JLabel directorLabel = new JLabel("\uD83C\uDFA5 Đạo diễn: " + (phim.getDaoDien() != null ? phim.getDaoDien() : "N/A"));
        directorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        directorLabel.setAlignmentX(LEFT_ALIGNMENT);
        directorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Release date
        JLabel releaseDateLabel = new JLabel("\uD83D\uDCC5 Ngày khởi chiếu: " + 
                (phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(formatter) : "N/A"));
        releaseDateLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        releaseDateLabel.setAlignmentX(LEFT_ALIGNMENT);
        releaseDateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Description
        JLabel descriptionTitle = new JLabel("Mô tả:");
        descriptionTitle.setFont(new Font("Arial", Font.BOLD, 16));
        descriptionTitle.setAlignmentX(LEFT_ALIGNMENT);
        descriptionTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JLabel descriptionLabel = new JLabel("<html><div style='width: 400px;'>" + 
                (phim.getMoTa() != null ? phim.getMoTa() : "Không có mô tả") + "</div></html>");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Add components to info panel
        infoPanel.add(genreLabel);
        infoPanel.add(durationLabel);
        infoPanel.add(countryLabel);
        infoPanel.add(directorLabel);
        infoPanel.add(releaseDateLabel);
        infoPanel.add(descriptionTitle);
        infoPanel.add(descriptionLabel);

        // Book ticket button
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        if (phim.getTrangThai() != null && "active".equals(phim.getTrangThai())) {
            JButton bookButton = new JButton("Đặt vé");
            bookButton.setBackground(new Color(0, 48, 135));
            bookButton.setForeground(Color.WHITE);
            bookButton.setFont(new Font("Arial", Font.BOLD, 16));
            bookButton.setPreferredSize(new Dimension(150, 40));
            bookButton.addActionListener(e -> {
                try {
                    int maKhachHang = khachHangController.getMaKhachHangFromSession(username);
                    bookTicketCallback.accept(phim.getMaPhim(), maKhachHang);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            bookButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    bookButton.setBackground(new Color(0, 72, 202));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    bookButton.setBackground(new Color(0, 48, 135));
                }
            });
            buttonPanel.add(bookButton, BorderLayout.WEST);
        }

        // Add components to right panel
        rightPanel.add(infoPanel);
        rightPanel.add(buttonPanel);

        // Add panels to main panel
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        // Add movie details to content panel
        contentPanel.add(mainPanel);
        
        // Add review panel
        try {
            DanhGiaPanel danhGiaPanel = new DanhGiaPanel(phim.getMaPhim(), username);
            danhGiaPanel.setAlignmentX(LEFT_ALIGNMENT);
            contentPanel.add(danhGiaPanel);
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Không thể tải đánh giá: " + e.getMessage());
            errorLabel.setAlignmentX(LEFT_ALIGNMENT);
            contentPanel.add(errorLabel);
        }
        
        // Create scroll pane for details
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Add content to detail panel
        detailPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Display detail panel
        mainContentPanel.removeAll();
        mainContentPanel.add(detailPanel, BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void showPhimList() {
        mainContentPanel.removeAll();
        mainContentPanel.add(mainScrollPane, BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
}