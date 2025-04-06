package com.cinema.repositories;

import com.cinema.models.Ghe;
import com.cinema.utils.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GheRepository extends BaseRepository<Ghe> {
    public GheRepository(DatabaseConnection dbConnection) {
        super(dbConnection);
    }

    public List<Ghe> findGheTrongByPhongAndSuatChieu(int maPhong, int maSuatChieu) throws SQLException {
        List<Ghe> list = new ArrayList<>();
        String sql = "SELECT g.maPhong, g.soGhe " +
                "FROM Ghe g " +
                "WHERE g.maPhong = ? " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM Ve v " +
                "WHERE v.maSuatChieu = ? AND v.maPhong = g.maPhong AND v.soGhe = g.soGhe" +
                ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            stmt.setInt(2, maSuatChieu);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Ghe(
                        rs.getInt("maPhong"),
                        rs.getString("soGhe")
                ));
            }
        }
        return list;
    }

    @Override
    public List<Ghe> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public Ghe findById(int id) throws SQLException {
        return null;
    }

    @Override
    public Ghe save(Ghe entity) throws SQLException {
        return null;
    }

    @Override
    public Ghe update(Ghe entity) throws SQLException {
        return null;
    }

    @Override
    public void delete(int id) throws SQLException {

    }
}