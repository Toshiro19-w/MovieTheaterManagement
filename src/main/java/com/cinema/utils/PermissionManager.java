package com.cinema.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cinema.enums.LoaiTaiKhoan;

public class PermissionManager {
    private final LoaiTaiKhoan loaiTaiKhoan;
    private static final Map<LoaiTaiKhoan, List<String>> rolePermissions = new HashMap<>();

    static {
        rolePermissions.put(LoaiTaiKhoan.ADMIN, Arrays.asList(
                "Dashboard", "Phim", "Suất chiếu", "Vé", "Hoá đơn", "Báo cáo", "Bán vé", "Người dùng"
        ));
        rolePermissions.put(LoaiTaiKhoan.QUANLYPHIM, Arrays.asList(
                "Phim", "Suất chiếu", "Nhân viên"
        ));
        rolePermissions.put(LoaiTaiKhoan.USER, Arrays.asList(
                "Phim đang chiếu"
        ));
        rolePermissions.put(LoaiTaiKhoan.THUNGAN, Arrays.asList(
                "Hoá đơn", "Vé", "Bán vé"
        ));
        rolePermissions.put(LoaiTaiKhoan.BANVE, Arrays.asList(
                "Phim", "Suất chiếu", "Bán vé", "Khách hàng"
        ));
    }

    public PermissionManager(LoaiTaiKhoan loaiTaiKhoan) {
        this.loaiTaiKhoan = loaiTaiKhoan;
    }

    public boolean hasPermission(String feature) {
        return rolePermissions.getOrDefault(loaiTaiKhoan, Collections.emptyList()).contains(feature);
    }

    public List<String> getPermissions() {
        return rolePermissions.getOrDefault(loaiTaiKhoan, Collections.emptyList());
    }

    public boolean isAdmin() {
        return loaiTaiKhoan == LoaiTaiKhoan.ADMIN;
    }

    public boolean isQuanLyPhim() {
        return loaiTaiKhoan == LoaiTaiKhoan.QUANLYPHIM;
    }

    public boolean isUser() {
        return loaiTaiKhoan == LoaiTaiKhoan.USER;
    }

    public boolean isThuNgan() {
        return loaiTaiKhoan == LoaiTaiKhoan.THUNGAN;
    }

    public boolean isBanVe() {
        return loaiTaiKhoan == LoaiTaiKhoan.BANVE;
    }
}