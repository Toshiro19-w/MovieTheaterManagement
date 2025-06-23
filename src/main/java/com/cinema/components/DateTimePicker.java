package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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

public class DateTimePicker extends JDialog {
    private JTextField textField;
    private JButton[] dayButtons = new JButton[42];
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;
    private JComboBox<String> hourComboBox;
    private JComboBox<String> minuteComboBox;
    private JComboBox<String> secondComboBox;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    private final String[] MONTHS = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                                    "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
    private final String[] DAYS = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
    
    public DateTimePicker(JTextField textField) {
        this.textField = textField;
        setTitle("Chọn ngày giờ");
        setModal(true);
        setSize(350, 400);
        setLocationRelativeTo(textField);
        
        // Nếu textField có giá trị, parse để lấy ngày giờ
        if (textField.getText() != null && !textField.getText().trim().isEmpty()) {
            try {
                Date date = dateTimeFormat.parse(textField.getText().trim());
                calendar.setTime(date);
            } catch (Exception e) {
                // Nếu parse lỗi, sử dụng ngày giờ hiện tại
                calendar = Calendar.getInstance();
            }
        }
        
        // Tạo giao diện
        initUI();
        
        // Hiển thị tháng hiện tại
        displayMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        
        // Đánh dấu ngày đã chọn
        highlightSelectedDay();
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
        monthComboBox.addActionListener(_ -> updateCalendar());
        
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
            dayButtons[i].addActionListener(e -> selectDate(index));
            daysPanel.add(dayButtons[i]);
        }
        
        // Panel chọn giờ
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timePanel.setBackground(Color.WHITE);
        timePanel.setBorder(BorderFactory.createTitledBorder("Thời gian"));
        
        hourComboBox = new JComboBox<>();
        minuteComboBox = new JComboBox<>();
        secondComboBox = new JComboBox<>();
        
        // Thêm các giá trị giờ, phút, giây
        for (int i = 0; i < 24; i++) {
            hourComboBox.addItem(String.format("%02d", i));
        }
        for (int i = 0; i < 60; i++) {
            minuteComboBox.addItem(String.format("%02d", i));
            secondComboBox.addItem(String.format("%02d", i));
        }
        
        // Thiết lập giờ, phút, giây từ calendar
        hourComboBox.setSelectedItem(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
        minuteComboBox.setSelectedItem(String.format("%02d", calendar.get(Calendar.MINUTE)));
        secondComboBox.setSelectedItem(String.format("%02d", calendar.get(Calendar.SECOND)));
        
        hourComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        minuteComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        secondComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        timePanel.add(hourComboBox);
        timePanel.add(new JLabel(":"));
        timePanel.add(minuteComboBox);
        timePanel.add(new JLabel(":"));
        timePanel.add(secondComboBox);
        
        // Panel chứa các nút điều khiển
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlPanel.setBackground(Color.WHITE);
        
        JButton todayButton = new JButton("Hôm nay");
        todayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        todayButton.setBackground(UIConstants.SECONDARY_COLOR);
        todayButton.setForeground(Color.WHITE);
        todayButton.setFocusPainted(false);
        todayButton.addActionListener(e -> {
            // Lấy ngày hiện tại
            Calendar today = Calendar.getInstance();
            
            // Cập nhật calendar với ngày hiện tại nhưng giữ nguyên giờ đã chọn
            int hour = Integer.parseInt((String) hourComboBox.getSelectedItem());
            int minute = Integer.parseInt((String) minuteComboBox.getSelectedItem());
            int second = Integer.parseInt((String) secondComboBox.getSelectedItem());
            
            calendar.setTime(today.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);
            
            // Cập nhật UI
            monthComboBox.setSelectedIndex(calendar.get(Calendar.MONTH));
            yearComboBox.setSelectedItem(String.valueOf(calendar.get(Calendar.YEAR)));
            
            // Hiển thị tháng mới
            displayMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
            
            // Đánh dấu ngày hiện tại
            highlightSelectedDay();
        });
        
        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        confirmButton.setBackground(UIConstants.PRIMARY_COLOR);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.addActionListener(e -> confirmSelection());
        
        JButton closeButton = new JButton("Đóng");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.setBackground(UIConstants.LIGHT_TEXT_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        
        controlPanel.add(todayButton);
        controlPanel.add(confirmButton);
        controlPanel.add(closeButton);
        
        // Thêm các panel vào panel chính
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(daysHeaderPanel, BorderLayout.NORTH);
        centerPanel.add(daysPanel, BorderLayout.CENTER);
        centerPanel.add(timePanel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Thêm panel chính vào dialog
        setContentPane(mainPanel);
    }
    
    private void updateCalendar() {
        int month = monthComboBox.getSelectedIndex();
        int year = Integer.parseInt((String) yearComboBox.getSelectedItem());
        
        // Lưu lại ngày hiện tại
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        
        // Kiểm tra xem ngày hiện tại có hợp lệ trong tháng mới không
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.MONTH, month);
        tempCalendar.set(Calendar.YEAR, year);
        int maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int newDay = Math.min(currentDay, maxDay);
        
        // Cập nhật calendar với ngày, tháng, năm mới
        calendar.set(Calendar.DAY_OF_MONTH, newDay);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        
        // Hiển thị tháng mới
        displayMonth(month, year);
        
        // Đánh dấu ngày đã chọn
        highlightSelectedDay();
    }
    
    private void displayMonth(int month, int year) {
        // Tạo calendar tạm để tính toán ngày đầu tiên của tháng
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        tempCalendar.set(Calendar.MONTH, month);
        tempCalendar.set(Calendar.YEAR, year);
        
        int firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
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
        }
    }
    
    private void highlightSelectedDay() {
        // Bỏ đánh dấu tất cả các nút
        for (JButton button : dayButtons) {
            if (button.isEnabled()) {
                button.setBackground(Color.WHITE);
            }
        }
        
        // Đánh dấu ngày hiện tại trong lịch
        Calendar today = Calendar.getInstance();
        
        // Tạo calendar tạm để tính toán ngày đầu tiên của tháng
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        
        // Đánh dấu ngày hiện tại (màu nhạt)
        if (today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && 
            today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
            int todayIndex = firstDayOfMonth + today.get(Calendar.DAY_OF_MONTH) - 1;
            if (todayIndex >= 0 && todayIndex < dayButtons.length && dayButtons[todayIndex].isEnabled()) {
                dayButtons[todayIndex].setBackground(new Color(230, 230, 250));
            }
        }
        
        // Đánh dấu ngày đã chọn (màu đậm)
        int selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        int selectedIndex = firstDayOfMonth + selectedDay - 1;
        if (selectedIndex >= 0 && selectedIndex < dayButtons.length && dayButtons[selectedIndex].isEnabled()) {
            dayButtons[selectedIndex].setBackground(UIConstants.HOVER_COLOR);
        }
    }
    
    private void selectDate(int buttonIndex) {
        if (!dayButtons[buttonIndex].isEnabled()) {
            return;
        }
        
        // Lấy ngày được chọn
        int day = Integer.parseInt(dayButtons[buttonIndex].getText());
        
        // Cập nhật calendar với ngày mới
        calendar.set(Calendar.DAY_OF_MONTH, day);
        
        // Đánh dấu ngày đã chọn
        highlightSelectedDay();
    }
    
    private void confirmSelection() {
        // Thiết lập giờ, phút, giây từ các combobox
        int hour = Integer.parseInt((String) hourComboBox.getSelectedItem());
        int minute = Integer.parseInt((String) minuteComboBox.getSelectedItem());
        int second = Integer.parseInt((String) secondComboBox.getSelectedItem());
        
        // Cập nhật calendar với giờ, phút, giây mới
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        
        // Cập nhật text field với ngày giờ đã chọn
        textField.setText(dateTimeFormat.format(calendar.getTime()));
        
        // Đóng dialog
        dispose();
    }
}