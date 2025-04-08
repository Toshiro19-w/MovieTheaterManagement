package com.cinema.controllers;

import com.cinema.models.SuatChieu;
import com.cinema.services.SuatChieuService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

public class SuatChieuController extends Component {
    private final SuatChieuService suatChieuService;

    public SuatChieuController(SuatChieuService suatChieuService) {
        this.suatChieuService = suatChieuService;
    }

    public List<SuatChieu> findAll() {
        try {
            return suatChieuService.getAllSuatChieu();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return null;
        }
    }

    public List<SuatChieu> findAllDetail() {
        try {
            return suatChieuService.getAllSuatChieuDetail();
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public SuatChieu findSuatChieuById(int maSuatChieu) {
        try {
            return suatChieuService.getSuatChieuById(maSuatChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phim: " + e.getMessage());
            return null;
        }
    }

    public SuatChieu saveSuatChieu(SuatChieu suatChieu) {
        try {
//            if (!ValidationUtils.validatePhim(phim)) {
//                System.out.println("Dữ liệu phim không hợp lệ.");
//                return null;
//            }
            return suatChieuService.addSuatChieu(suatChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu suất chiếu: " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Mã thể loại và mã phòng là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:sss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public SuatChieu updateSuatChieu(SuatChieu suatChieu) {
        try {
//            if (!ValidationUtils.validatePhongChieu(phongChieu)) {
//                System.out.println("Dữ liệu phòng chiếu không hợp lệ.");
//                return null;
//            }
            return suatChieuService.updateSuatChieu(suatChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật suất chiếu: " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Mã suất chiếu, mã phim và mã phòng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Ngày giờ chiếu không đúng định dạng (dd/MM/yyyy HH:mm:ss)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public boolean deleteSuatChieu(int maSuatChieu) {
        try {
            suatChieuService.deleteSuatChieu(maSuatChieu);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phim: " + e.getMessage());
            return false;
        }
    }

    public List<SuatChieu> searchSuatChieuByNgay(LocalDateTime ngayGioChieu) {
        try {
            return suatChieuService.searchSuatChieuByNgay(ngayGioChieu);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm suất chiếu: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
