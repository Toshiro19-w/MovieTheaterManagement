package com.cinema;

import com.cinema.views.*;
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhap().setVisible(true));
    }
}
