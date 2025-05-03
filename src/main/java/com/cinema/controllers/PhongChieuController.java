package com.cinema.controllers;

import com.cinema.models.PhongChieu;
import com.cinema.services.PhongChieuService;
import com.cinema.views.admin.SuatChieuView;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.util.List;

public class PhongChieuController {
    private final SuatChieuView view;
    private final PhongChieuService service;

    public PhongChieuController(SuatChieuView view) {
        this.view = view;
        this.service = new PhongChieuService(view.getDatabaseConnection());
        initView();
    }

    private void initView() {
        try {
            loadPhongChieuList(service.getAllPhongChieu());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Lỗi khi tải danh sách phòng chiếu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadPhongChieuList(List<PhongChieu> phongChieus) {
        DefaultTableModel model = view.getPhongChieuTableModel();
        model.setRowCount(0);
        for (PhongChieu pc : phongChieus) {
            model.addRow(new Object[]{
                    pc.getMaPhong(),
                    pc.getTenPhong(),
                    pc.getSoLuongGhe(),
                    pc.getLoaiPhong()
            });
        }
    }
}