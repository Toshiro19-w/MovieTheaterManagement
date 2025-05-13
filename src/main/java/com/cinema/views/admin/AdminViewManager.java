package com.cinema.views.admin;

import java.awt.CardLayout;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JPanel;

import com.cinema.enums.LoaiTaiKhoan;
import com.cinema.utils.PermissionManager;

public class AdminViewManager {
    private final PermissionManager permissionManager;
    private final JPanel mainContentPanel;
    private final CardLayout cardLayout;
    private final String username;

    public AdminViewManager(LoaiTaiKhoan loaiTaiKhoan, JPanel mainContentPanel, CardLayout cardLayout, String username) {
        this.permissionManager = new PermissionManager(loaiTaiKhoan);
        this.mainContentPanel = mainContentPanel;
        this.cardLayout = cardLayout;
        this.username = username;
    }

    public void initializeAdminPanels() throws IOException, SQLException {
        if (permissionManager.hasPermission("Phim")) {
            mainContentPanel.add(new PhimView(), "Phim");
        }
        if (permissionManager.hasPermission("Suất chiếu")) {
            mainContentPanel.add(new SuatChieuView(), "Suất chiếu");
        }
        if (permissionManager.hasPermission("Vé")) {
            mainContentPanel.add(new VeView(), "Vé");
        }
        if (permissionManager.hasPermission("Nhân viên")) {
            mainContentPanel.add(new NhanVienView(), "Nhân viên");
        }
        if (permissionManager.hasPermission("Hoá đơn")) {
            mainContentPanel.add(new HoaDonView(username), "Hoá đơn");
        }
        if (permissionManager.hasPermission("Báo cáo")) {
            mainContentPanel.add(new BaoCaoView(), "Báo cáo");
        }
        if (permissionManager.hasPermission("Bán vé")) {
            mainContentPanel.add(new SellTicketView(), "Bán vé");
        }
    }
}