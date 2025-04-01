package com.cinema.repositories;

import com.cinema.models.Ve;
import java.util.List;

public interface IVeRepository {
    public List<Ve> getAllVeBasic();
    public List<Ve> getAllVeCuaToi(String email);
    public boolean suaVe(Ve ve);
    public boolean xoaVe(int maVe);
    public boolean themVe(Ve ve);
}
