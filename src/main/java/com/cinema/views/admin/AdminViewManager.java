package com.cinema.views.admin;

import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.utils.PermissionManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

public class AdminViewManager {
    private final PermissionManager permissionManager;
    private final JPanel mainContentPanel;
    private final CardLayout cardLayout;

    public AdminViewManager(LoaiTaiKhoan loaiTaiKhoan, JPanel mainContentPanel, CardLayout cardLayout) {
        this.permissionManager = new PermissionManager(loaiTaiKhoan);
        this.mainContentPanel = mainContentPanel;
        this.cardLayout = cardLayout;
    }

    public void initializeAdminPanels() throws IOException, SQLException {
        if (permissionManager.hasPermission("Phim")) {
            mainContentPanel.add(new PhimView(), "Phim");
        }
        if (permissionManager.hasPermission("Suất chiếu")) {
            mainContentPanel.add(new SuatChieuView(), "Suất chiếu");
        }
        if (permissionManager.hasPermission("Phòng chiếu")) {
            mainContentPanel.add(new PhongChieuView(), "Phòng chiếu");
        }
        if (permissionManager.hasPermission("Vé")) {
            mainContentPanel.add(new VeView(), "Vé");
        }
        if (permissionManager.hasPermission("Nhân viên")) {
            mainContentPanel.add(new NhanVienView(), "Nhân viên");
        }
        if (permissionManager.hasPermission("Hoá đơn")) {
            mainContentPanel.add(new HoaDonView(), "Hoá đơn");
        }
        if (permissionManager.hasPermission("Báo cáo")) {
            mainContentPanel.add(new BaoCaoView(), "Báo cáo");
        }
    }
}