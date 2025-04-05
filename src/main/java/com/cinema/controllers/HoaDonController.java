package com.cinema.controllers;

import com.cinema.models.HoaDon;
import com.cinema.models.Ve;
import com.cinema.services.HoaDonService;
import com.cinema.services.VeService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class HoaDonController {
    private final HoaDonService hoaDonService;
    private final VeService veService;

    public HoaDonController(HoaDonService hoaDonService, VeService veService) {
        this.hoaDonService = hoaDonService;
        this.veService = veService;
    }

    public int thanhToanHoaDon(Integer maNhanVien, Integer maKhachHang, List<Integer> maVeList) throws SQLException {
        // Tính tổng tiền từ các vé
        BigDecimal tongTien = BigDecimal.ZERO;
        for (Integer maVe : maVeList) {
            List<Ve> veList = veService.findByHoaDon(null); // Lấy vé chưa có hóa đơn
            for (Ve ve : veList) {
                if (ve.getMaVe() == maVe && "booked".equals(ve.getTrangThai())) {
                    tongTien = tongTien.add(ve.getGiaVe());
                }
            }
        }

        // Tạo hóa đơn
        int maHoaDon = hoaDonService.createHoaDon(maNhanVien, maKhachHang, tongTien);

        // Cập nhật trạng thái vé và liên kết với hóa đơn
        for (Integer maVe : maVeList) {
            veService.updateVeStatus(maVe, "paid", maHoaDon);
            hoaDonService.createChiTietHoaDon(maHoaDon, maVe);
        }

        return maHoaDon;
    }

    public List<HoaDon> getLichSuHoaDon(int maKhachHang) throws SQLException {
        return hoaDonService.findByKhachHang(maKhachHang);
    }

    public List<Ve> getVeByHoaDon(int maHoaDon) throws SQLException {
        return veService.findByHoaDon(maHoaDon);
    }
}