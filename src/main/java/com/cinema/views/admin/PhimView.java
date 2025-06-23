package com.cinema.views.admin;

import com.cinema.components.ModernUIApplier;
import com.cinema.components.UIConstants;
import com.cinema.controllers.PhimController;
import com.cinema.models.NhanVien;
import com.cinema.models.Phim;
import com.cinema.services.PhimService;
import com.cinema.utils.DatabaseConnection;
import com.cinema.models.dto.PaginationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PhimView extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> tableSorter;
    private final JButton btnAdd, btnGenre, btnReload;

    private PhimController controller;
    private final NhanVien currentNhanVien;

    public PhimView(NhanVien currentNhanVien) throws SQLException, IOException {
    	DatabaseConnection dbConnection = new DatabaseConnection();
    	PhimService service = new PhimService(dbConnection);
    	this.controller = new PhimController(currentNhanVien, service);
        this.currentNhanVien = currentNhanVien;
        // Service và controller inject đúng chuẩn mới
        this.controller = new PhimController(currentNhanVien, service);

        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);

        // Top buttons panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        topPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        btnAdd = ModernUIApplier.createModernButton("Thêm phim", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnGenre = ModernUIApplier.createModernButton("Quản lý thể loại", UIConstants.SECONDARY_COLOR, Color.WHITE);
        btnGenre.setPreferredSize(new Dimension(160, 40));
        btnReload = ModernUIApplier.createModernButton("Làm mới", UIConstants.PRIMARY_COLOR, Color.WHITE);
        btnReload.setPreferredSize(new Dimension(160, 40));

        topPanel.add(btnAdd);
        topPanel.add(btnGenre);
        topPanel.add(btnReload);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(
                new Object[]{"Poster", "Tên phim", "Thể loại", "Thời lượng", "Ngày khởi chiếu", "Trạng thái"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return ImageIcon.class;
                return super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(tableModel);
        ModernUIApplier.applyModernTableStyle(table);
        table.setRowHeight(120);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(tableSorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 12, 12, 12));
        add(scrollPane, BorderLayout.CENTER);

        // Load dữ liệu lần đầu
        reloadTable();

        // Button Actions
        btnAdd.addActionListener(e -> openMovieDialog(null));
        btnReload.addActionListener(e -> reloadTable());
        btnGenre.addActionListener(e -> {
            GenreDialog dialog = new GenreDialog(SwingUtilities.getWindowAncestor(this), controller);
            dialog.setVisible(true);
        });

        // Double click vào row để xem/sửa phim
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int viewRow = table.getSelectedRow();
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    Phim phim = getPhimByTableRow(modelRow);
                    openMovieDialog(phim);
                }
            }
        });
    }

    /** Load lại dữ liệu bảng phim */
    private void reloadTable() {
        try {
            PaginationResult<Phim> result = controller.getPhimPaginated(1, 15);
            tableModel.setRowCount(0);
            for (Phim phim : result.getData()) {
                ImageIcon icon = null;
                if (phim.getDuongDanPoster() != null && !phim.getDuongDanPoster().isEmpty()) {
                    String path = "src/main/resources/images/posters/" + phim.getDuongDanPoster();
                    java.io.File f = new java.io.File(path);
                    if (f.exists())
                        icon = new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(70, 100, Image.SCALE_SMOOTH));
                }
                tableModel.addRow(new Object[]{
                        icon,
                        phim.getTenPhim(),
                        phim.getTenTheLoai(),
                        phim.getThoiLuong(),
                        phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "",
                        phim.getTrangThai()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phim: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Lấy đối tượng Phim theo row trong bảng */
    private Phim getPhimByTableRow(int modelRow) {
        try {
            String tenPhim = tableModel.getValueAt(modelRow, 1).toString();
            int thoiLuong = Integer.parseInt(tableModel.getValueAt(modelRow, 3).toString());
            List<Phim> allPhim = controller.getAllPhim();
            for (Phim p : allPhim) {
                if (p.getTenPhim().equals(tenPhim) && p.getThoiLuong() == thoiLuong) {
                    return p;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /** Mở dialog thêm/sửa/xem phim */
    private void openMovieDialog(Phim phim) {
        MovieDialog dialog = new MovieDialog(
            SwingUtilities.getWindowAncestor(this),
            controller,
            phim,
            () -> reloadTable() // callback chỉ reloadTable đúng 1 lần
        );
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}