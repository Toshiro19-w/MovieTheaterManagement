package com.cinema.views.admin;

import com.cinema.components.ModernUIApplier;
import com.cinema.components.UIConstants;
import com.cinema.controllers.PhimController;
import com.cinema.models.NhanVien;
import com.cinema.models.Phim;
import com.cinema.models.dto.PaginationResult;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SimpleDocumentListener;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.RowFilter;

/**
 * Giao diện quản lý phim cho admin, hỗ trợ hiển thị danh sách phim,
 * tìm kiếm theo tên và ngày khởi chiếu, thêm/sửa phim và quản lý thể loại.
 */
public class PhimView extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> tableSorter;
    private final JButton btnAdd, btnGenre, btnReload;
    private final JTextField txtSearchTenPhim;
    private final DatePicker dateSearchNgayKhoiChieu;
    private PhimController controller;
    private final NhanVien currentNhanVien;

    /**
     * Khởi tạo PhimView với nhân viên hiện tại.
     *
     * @param currentNhanVien Nhân viên đang đăng nhập
     * @throws SQLException Nếu có lỗi kết nối cơ sở dữ liệu
     * @throws IOException Nếu có lỗi đọc file
     */
    public PhimView(NhanVien currentNhanVien) throws SQLException, IOException {
        this.currentNhanVien = currentNhanVien;

        // Khởi tạo service và controller
        DatabaseConnection dbConnection = new DatabaseConnection();
        PhimService service = new PhimService(dbConnection);
        this.controller = new PhimController(currentNhanVien, service);

        // Thiết lập layout chính
        setLayout(new BorderLayout(10, 10));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Header Panel (Chứa Search Panel và Top Buttons Panel) ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel lblSearchTenPhim = new JLabel("Tìm tên phim:");
        lblSearchTenPhim.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchTenPhim = new JTextField(20);
        txtSearchTenPhim.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchTenPhim.setPreferredSize(new Dimension(200, 30));

        JLabel lblNgayKhoiChieu = new JLabel("Ngày khởi chiếu:");
        lblNgayKhoiChieu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("dd/MM/yyyy");
        dateSearchNgayKhoiChieu = new DatePicker(dateSettings);
        dateSearchNgayKhoiChieu.setPreferredSize(new Dimension(150, 30));

        searchPanel.add(lblSearchTenPhim);
        searchPanel.add(txtSearchTenPhim);
        searchPanel.add(lblNgayKhoiChieu);
        searchPanel.add(dateSearchNgayKhoiChieu);

        // --- Top Buttons Panel ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        btnAdd = ModernUIApplier.createModernButton("Thêm phim", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnGenre = ModernUIApplier.createModernButton("Quản lý thể loại", UIConstants.SECONDARY_COLOR, Color.WHITE);
        btnGenre.setPreferredSize(new Dimension(160, 40));
        btnReload = ModernUIApplier.createModernButton("Làm mới", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnReload.setPreferredSize(new Dimension(160, 40));

        topPanel.add(btnAdd);
        topPanel.add(btnGenre);
        topPanel.add(btnReload);

        // Thêm searchPanel và topPanel vào headerPanel
        headerPanel.add(searchPanel);
        headerPanel.add(topPanel);

        // Thêm headerPanel vào BorderLayout.NORTH
        add(headerPanel, BorderLayout.NORTH);

        // --- Table Setup ---
        tableModel = new DefaultTableModel(
                new Object[]{"Mã Phim", "Poster", "Tên Phim", "Thể Loại", "Thời Lượng", "Ngày Khởi Chiếu", "Trạng Thái"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return ImageIcon.class;
                return super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(tableModel);
        ModernUIApplier.applyModernTableStyle(table);
        table.setRowHeight(120);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMinWidth(0); // Ẩn cột mã phim
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        tableSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(tableSorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Load dữ liệu lần đầu
        reloadTable();

        // --- Event Listeners ---
        txtSearchTenPhim.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyTableFilter));
        dateSearchNgayKhoiChieu.addDateChangeListener(e -> applyTableFilter());

        btnAdd.addActionListener(e -> openMovieDialog(null));
        btnReload.addActionListener(e -> reloadTable());
        btnGenre.addActionListener(e -> {
            GenreDialog dialog = new GenreDialog(SwingUtilities.getWindowAncestor(PhimView.this), controller);
            dialog.setVisible(true);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int viewRow = table.getSelectedRow();
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    Phim phim = getPhimByTableRow(modelRow);
                    openMovieDialog(phim);
                }
            }
        });
    }

    /**
     * Áp dụng bộ lọc cho bảng dựa trên tên phim và ngày khởi chiếu.
     */
    private void applyTableFilter() {
        String searchTen = txtSearchTenPhim.getText().trim().toLowerCase();
        LocalDate selectedDate = dateSearchNgayKhoiChieu.getDate();

        tableSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Lọc theo tên phim
                String tenPhim = entry.getStringValue(2).toLowerCase();
                boolean matchTen = searchTen.isEmpty() || tenPhim.contains(searchTen);

                // Lọc theo ngày khởi chiếu
                boolean matchNgay = true;
                if (selectedDate != null) {
                    String ngayText = entry.getStringValue(5);
                    if (ngayText != null && !ngayText.isEmpty()) {
                        try {
                            LocalDate ngay = LocalDate.parse(ngayText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                            matchNgay = ngay.equals(selectedDate);
                        } catch (Exception ex) {
                            matchNgay = false;
                        }
                    } else {
                        matchNgay = false;
                    }
                }
                return matchTen && matchNgay;
            }
        });
    }

    /**
     * Tải lại dữ liệu bảng phim từ cơ sở dữ liệu.
     */
    private void reloadTable() {
        try {
            PaginationResult<Phim> result = controller.getPhimPaginated(1, 15);
            tableModel.setRowCount(0);
            for (Phim phim : result.getData()) {
                ImageIcon icon = null;
                if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
                    String path = "src/main/resources/images/posters/" + phim.getDuongDanPoster();
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        icon = new ImageIcon(new ImageIcon(file.getAbsolutePath())
                                .getImage()
                                .getScaledInstance(70, 100, Image.SCALE_SMOOTH));
                    }
                }
                tableModel.addRow(new Object[]{
                        phim.getMaPhim(),
                        icon,
                        phim.getTenPhim(),
                        phim.getTenTheLoai(),
                        phim.getThoiLuong(),
                        phim.getNgayKhoiChieu() != null
                                ? phim.getNgayKhoiChieu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                : "",
                        phim.getTrangThai()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phim: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Lấy đối tượng Phim dựa trên hàng trong bảng.
     *
     * @param modelRow Hàng trong model của bảng
     * @return Đối tượng Phim hoặc null nếu không tìm thấy
     */
    private Phim getPhimByTableRow(int modelRow) {
        try {
            int maPhim = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
            return controller.getPhimById(maPhim);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin phim: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Mở dialog để thêm hoặc sửa thông tin phim.
     *
     * @param phim Đối tượng Phim để sửa, hoặc null nếu thêm mới
     */
    private void openMovieDialog(Phim phim) {
        MovieDialog dialog = new MovieDialog(
                SwingUtilities.getWindowAncestor(this),
                controller,
                phim,
                this::reloadTable
        );
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}