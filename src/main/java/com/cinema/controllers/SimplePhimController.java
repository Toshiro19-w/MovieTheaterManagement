package com.cinema.controllers;

import com.cinema.models.Phim;
import com.cinema.services.PhimService;
import java.sql.SQLException;
import java.util.List;

public class SimplePhimController {
    private final PhimService service;

    public SimplePhimController(PhimService service) {
        this.service = service;
    }

    public List<Phim> getAllPhim() throws SQLException {
        return service.getAllPhim();
    }
} 