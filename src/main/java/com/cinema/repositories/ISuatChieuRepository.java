package com.cinema.repositories;

import com.cinema.models.SuatChieu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public interface ISuatChieuRepository {
    public List<SuatChieu> getAllSuatChieu();
    public boolean themSuatChieu(SuatChieu suatChieu);
}
