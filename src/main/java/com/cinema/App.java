package com.cinema;

import com.cinema.views.logn.LoginView;

import javax.swing.*;

public class App {
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}