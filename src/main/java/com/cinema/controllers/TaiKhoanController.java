package com.cinema.controllers;

import java.awt.Component;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.cinema.services.TaiKhoanService;

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

    public boolean authenticateLogin(String username, String password) {
        try {
            return taiKhoanService.authenticateUser(username, password);
        } catch (SQLException e) {
            LOGGER.severe("Error during authentication: " + e.getMessage());
            return false;
        }
    }

    public int handleUserRegistration(String username, String fullName, String phone, String email, String password) {
        try {
            return taiKhoanService.handleRegistration(username, fullName, phone, email, password);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid registration data: " + e.getMessage());
            return -1;
        } catch (SQLException e) {
            LOGGER.severe("Database error during registration: " + e.getMessage());
            return -2;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(TaiKhoanController.class.getName());
}
