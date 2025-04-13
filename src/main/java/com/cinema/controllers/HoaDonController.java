package com.cinema.controllers;

import com.cinema.models.HoaDon;
import com.cinema.models.Ve;
import com.cinema.services.HoaDonService;
import com.cinema.services.VeService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class HoaDonController extends Component {
    private final HoaDonService hoaDonService;
    private final VeService veService;

    public HoaDonController(HoaDonService hoaDonService, VeService veService) {
        this.hoaDonService = hoaDonService;
        this.veService = veService;
    }

    public List<HoaDon> getLichSuHoaDon(int maKhachHang) {
        try{
            return hoaDonService.findByKhachHang(maKhachHang);
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã khách hàng hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public List<HoaDon> getLichSuHoaDonByTenKhachHang(String tenKhachHang) {
        try{
            return hoaDonService.getLichSuHoaDonByTenKhachHang(tenKhachHang);
        } catch (IllegalArgumentException | SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public List<Ve> getVeByHoaDon(int maHoaDon) {
        try {
            return veService.findByHoaDon(maHoaDon);
        } catch (IllegalArgumentException | SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public List<String> getAllTenKhachHang() {
        try {
            return hoaDonService.getAllTenKhachHang();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách tên khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}