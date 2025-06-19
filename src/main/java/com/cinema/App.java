package com.cinema;

import javax.swing.SwingUtilities;

import com.cinema.controllers.SyncController;
import com.cinema.utils.DatabaseConnection;
import com.cinema.utils.SyncManager;
import com.cinema.views.login.LoginView;

import java.io.IOException;

public class App {
    private static SyncController syncController;
    
    public static void main(String[] args){
        try {
            // Khởi tạo kết nối cơ sở dữ liệu
            DatabaseConnection dbConnection = new DatabaseConnection();
            
            // Khởi tạo SyncController
            syncController = new SyncController(dbConnection);
            
            // Đăng ký hook để đóng kết nối khi tắt ứng dụng
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (syncController != null) {
                    syncController.shutdown();
                }
                dbConnection.close();
                System.out.println("Ứng dụng đã đóng an toàn.");
            }));
            
            SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
        } catch (IOException e) {
            System.err.println("Không thể khởi tạo kết nối cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static SyncController getSyncController() {
        return syncController;
    }
}