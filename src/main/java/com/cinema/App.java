package com.cinema;

import com.cinema.views.*;
import javax.swing.*;

public class App {
    public static void main(String[] args){
        LoginView loginView = new LoginView(); // Khởi tạo đối tượng
        loginView.hashAllPasswordsInDatabase(); // Hash mật khẩu
    SwingUtilities.invokeLater(() ->  new LoginView().setVisible(true));

    }
}