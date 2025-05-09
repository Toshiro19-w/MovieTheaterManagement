package com.cinema;

import javax.swing.SwingUtilities;

import com.cinema.views.login.LoginView;

public class App {
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}