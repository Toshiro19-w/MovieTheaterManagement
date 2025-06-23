package com.cinema.controllers;

import com.cinema.components.MultiSelectComboBox;
import com.cinema.models.NhanVien;
import com.cinema.models.Phim;
import com.cinema.models.repositories.PhimRepository;
import com.cinema.services.PhimService;
import com.cinema.utils.LogUtils;
import com.cinema.models.dto.PaginationResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller quản lý logic CRUD phim, phục vụ cho các popup và bảng.
 * Không thao tác trực tiếp với các trường form như view cũ.
 */
public class PhimController {
    private final PhimService service;
    private final NhanVien currentNhanVien;
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PhimController(NhanVien currentNhanVien, PhimService service) {
        this.currentNhanVien = currentNhanVien;
        this.service = service;
    }

    // Lấy danh sách thể loại (Map<id, tên>)
    public Map<Integer, String> getAllTheLoaiMap() throws SQLException {
        return service.getAllTheLoaiMap();
    }

    // Lấy danh sách kiểu phim
    public List<String> getAllDinhDang() throws SQLException {
        return service.getAllDinhDang();
    }

    // Lấy danh sách phim phân trang
    public PaginationResult<Phim> getPhimPaginated(int page, int pageSize) throws SQLException {
        return service.getAllPhimPaginated(page, pageSize);
    }

    // Lấy toàn bộ phim (không phân trang)
    public List<Phim> getAllPhim() throws SQLException {
        return service.getAllPhim();
    }

    // Lấy phim theo id
    public Phim getPhimById(int maPhim) throws SQLException {
        return service.getPhimById(maPhim);
    }

    // Thêm phim mới, trả về mã phim vừa thêm
    public int themPhim(Phim phim, String posterPath) throws SQLException {
        if (posterPath != null) {
            phim.setDuongDanPoster(savePoster(posterPath, phim.getTenPhim()));
        }
        
        phim.setTrangThai("upcoming");
        int maPhim = service.addPhim(phim).getMaPhim();
        LogUtils.logThemPhim(maPhim, "Thêm phim mới: " + phim.getTenPhim(), getCurrentUserId());
        return maPhim;
    }

    // Sửa phim
    public void suaPhim(Phim phim, String posterPath) throws SQLException {
        if (posterPath != null) {
            phim.setDuongDanPoster(savePoster(posterPath, phim.getTenPhim()));
        }
        service.updatePhim(phim);
        LogUtils.logSuaPhim(phim.getMaPhim(), "Sửa phim: " + phim.getTenPhim(), getCurrentUserId());
    }

    // Xóa phim
    public void xoaPhim(int maPhim) throws SQLException {
        Phim phim = service.getPhimById(maPhim);
        if (phim != null) {
            service.deletePhim(maPhim);
            LogUtils.logXoaPhim(maPhim, "Xóa phim: " + phim.getTenPhim(), getCurrentUserId());
        }
    }

    // Lưu poster mới vào thư mục resources, trả về tên file
    private String savePoster(String sourcePath, String tenPhim) {
        try {
            String posterDir = "src/main/resources/images/posters/";
            File postersDir = new File(posterDir);
            if (!postersDir.exists()) postersDir.mkdirs();

            // Đặt tên file poster theo tên phim
            String safeName = tenPhim.replaceAll("[^a-zA-Z0-9]", "_");
            String fileName = safeName + getFileExtension(sourcePath);
            Path targetPath = Paths.get(posterDir, fileName);

            Files.copy(Paths.get(sourcePath), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lưu poster: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return path.substring(lastDotIndex);
        }
        return "";
    }

    // Lấy mã user hiện tại (nếu cần log)
    private int getCurrentUserId() {
        return currentNhanVien != null ? currentNhanVien.getMaNguoiDung() : -1;
    }

    // Lấy thể loại từ MultiSelectComboBox
    public List<Integer> getSelectedTheLoaiIds(MultiSelectComboBox cb) {
        return cb.getSelectedIds().stream().map(id -> (Integer) id).collect(Collectors.toList());
    }
    public PhimService getService() {
        return service;
    }
}