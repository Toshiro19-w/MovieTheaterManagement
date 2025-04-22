package com.cinema.services;

import com.cinema.repositories.TaiKhoanRepository;
import com.cinema.utils.DatabaseConnection;

public class TaiKhoanService {
    private final TaiKhoanRepository taiKhoanRepository;

    public TaiKhoanService(DatabaseConnection databaseConnection) {
        this.taiKhoanRepository = new TaiKhoanRepository(databaseConnection);
    }

    public void saveResetTokenToDB(String email, String token) {
        taiKhoanRepository.saveResetTokenToDB(email, token);
    }

    public boolean checkEmailExists(String email) {
        return taiKhoanRepository.checkEmailExists(email);
    }
}
