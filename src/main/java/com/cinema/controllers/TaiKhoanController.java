package com.cinema.controllers;

import com.cinema.services.TaiKhoanService;

import javax.swing.*;
import java.awt.*;

public class TaiKhoanController extends Component {
    private final TaiKhoanService taiKhoanService;

    public TaiKhoanController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    public void saveResetTokenToDB(String email, String token) {
        try{
            taiKhoanService.saveResetTokenToDB(email, token);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isEmailExists(String email) {
        try{
            return taiKhoanService.checkEmailExists(email);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
