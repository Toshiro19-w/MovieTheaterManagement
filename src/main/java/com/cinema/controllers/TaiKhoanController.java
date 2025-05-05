package com.cinema.controllers;

import com.cinema.services.TaiKhoanService;

import java.awt.*;
import java.sql.SQLException;

public class TaiKhoanController extends Component {
    private final TaiKhoanService taiKhoanService;

    public TaiKhoanController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    public boolean verifyUserForPasswordReset(String username, String email, String phone) throws SQLException {
        return taiKhoanService.verifyUserForPasswordReset(username, email, phone);
    }

    public boolean updatePassword(String username, String hashedPassword) throws SQLException {
        return taiKhoanService.updatePassword(username, hashedPassword);
    }
}
