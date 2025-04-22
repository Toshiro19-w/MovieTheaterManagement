package com.cinema.services;

import com.cinema.models.Ghe;
import com.cinema.models.repositories.GheRepository;
import com.cinema.utils.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class GheService {
    private final GheRepository gheRepository;

    public GheService(DatabaseConnection databaseConnection) {
        this.gheRepository = new GheRepository(databaseConnection);
    }

    public List<Ghe> findGheTrongByPhongAndSuatChieu(int maPhong, int maSuatChieu) throws SQLException {
        return gheRepository.findGheTrongByPhongAndSuatChieu(maPhong, maSuatChieu);
    }
}
