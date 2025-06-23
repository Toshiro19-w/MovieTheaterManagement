package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class DatePicker extends JDialog {
    private JTextField textField;
    private JButton[] dayButtons = new JButton[42];
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    private final String[] MONTHS = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                                    "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
    private final String[] DAYS = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
    
    public DatePicker(JTextField textField) {
        this.textField = textField;
        setTitle("Chọn ngày");
        setModal(true);
        setSize(350, 350);
        setLocationRelativeTo(textField);
        
        // Tạo giao diện
        initUI();
        
        // Hiển thị tháng hiện tại
        displayMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }
    
    private void initUI() {
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setBackground(Color.WHITE);
        
        // Panel chọn tháng và năm
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(UIConstants.PRIMARY_COLOR);
        
        // Tạo combobox chọn tháng
        monthComboBox = new JComboBox<>(MONTHS);
        monthComboBox.setSelectedIndex(calendar.get(Calendar.MONTH));
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        monthComboBox.addActionListener(e -> updateCalendar());
        
        // Tạo combobox chọn năm
        yearComboBox = new JComboBox<>();
        int currentYear = calendar.get(Calendar.YEAR);
        for (int year = currentYear - 10; year <= currentYear + 10; year++) {
            yearComboBox.addItem(String.valueOf(year));
        }
        yearComboBox.setSelectedItem(String.valueOf(currentYear));
        yearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        yearComboBox.addActionListener(e -> updateCalendar());
        
        headerPanel.add(monthComboBox);
        headerPanel.add(yearComboBox);
        
        // Panel hiển thị tên các ngày trong tuần
        JPanel daysHeaderPanel = new JPanel(new GridLayout(1, 7));
        daysHeaderPanel.setBackground(Color.WHITE);
        
        for (String day : DAYS) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (day.equals("CN")) {
                label.setForeground(Color.RED);
            }
            daysHeaderPanel.add(label);
        }
        
        // Panel hiển thị các ngày trong tháng
        JPanel daysPanel = new JPanel(new GridLayout(6, 7));
        daysPanel.setBackground(Color.WHITE);
        
        // Tạo các nút cho các ngày
        for (int i = 0; i < dayButtons.length; i++) {
            final int index = i;
            dayButtons[i] = new JButton();
            dayButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayButtons[i].setFocusPainted(false);
            dayButtons[i].setBackground(Color.WHITE);
            dayButtons[i].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            dayButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectDate(index);
                }
            });
            daysPanel.add(dayButtons[i]);
        }
        
        // Panel chứa các nút điều khiển
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlPanel.setBackground(Color.WHITE);
        
        JButton todayButton = new JButton("Hôm nay");
        todayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        todayButton.setBackground(UIConstants.SECONDARY_COLOR);
        todayButton.setForeground(Color.WHITE);
        todayButton.setFocusPainted(false);
        todayButton.addActionListener(e -> {
            calendar = Calendar.getInstance();
            monthComboBox.setSelectedIndex(calendar.get(Calendar.MONTH));
            yearComboBox.setSelectedItem(String.valueOf(calendar.get(Calendar.YEAR)));
            updateCalendar();
            selectToday();
        });
        
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.setBackground(UIConstants.LIGHT_TEXT_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        
        controlPanel.add(todayButton);
        controlPanel.add(closeButton);
        
        // Thêm các panel vào panel chính
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(daysHeaderPanel, BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(daysHeaderPanel, BorderLayout.NORTH);
        centerPanel.add(daysPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Thêm panel chính vào dialog
        setContentPane(mainPanel);
    }
    
    private void updateCalendar() {
        int month = monthComboBox.getSelectedIndex();
        int year = Integer.parseInt((String) yearComboBox.getSelectedItem());
        displayMonth(month, year);
    }
    
    private void displayMonth(int month, int year) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        
        int firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Xóa tất cả các nút
        for (JButton button : dayButtons) {
            button.setText("");
            button.setEnabled(false);
            button.setBackground(Color.WHITE);
        }
        
        // Hiển thị các ngày trong tháng
        for (int i = 0; i < daysInMonth; i++) {
            dayButtons[firstDayOfMonth + i].setText(String.valueOf(i + 1));
            dayButtons[firstDayOfMonth + i].setEnabled(true);
            
            // Đánh dấu ngày hiện tại
            Calendar today = Calendar.getInstance();
            if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month && 
                today.get(Calendar.DAY_OF_MONTH) == i + 1) {
                dayButtons[firstDayOfMonth + i].setBackground(UIConstants.HOVER_COLOR);
            }
        }
    }
    
    private void selectDate(int buttonIndex) {
        if (!dayButtons[buttonIndex].isEnabled()) {
            return;
        }
        
        int day = Integer.parseInt(dayButtons[buttonIndex].getText());
        calendar.set(Calendar.DAY_OF_MONTH, day);
        
        // Cập nhật text field với ngày đã chọn
        textField.setText(dateFormat.format(calendar.getTime()));
        
        // Đóng dialog
        dispose();
    }
    
    private void selectToday() {
        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        
        for (int i = 0; i < dayButtons.length; i++) {
            if (dayButtons[i].isEnabled() && dayButtons[i].getText().equals(String.valueOf(todayDay))) {
                selectDate(i);
                break;
            }
        }
    }
}