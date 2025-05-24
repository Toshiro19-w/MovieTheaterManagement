package com.cinema.views.admin;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Một component đơn giản để chọn ngày.
 * Lưu ý: Đây là một class giả định để thay thế cho JDateChooser từ thư viện JCalendar.
 * Trong ứng dụng thực tế, bạn nên sử dụng thư viện JCalendar hoặc một thư viện chọn ngày khác.
 */
public class JDateChooser extends JPanel {
    private JSpinner dateSpinner;
    private Date date;

    public JDateChooser() {
        setLayout(new BorderLayout());
        
        // Tạo model cho spinner với ngày hiện tại
        SpinnerDateModel model = new SpinnerDateModel();
        model.setValue(new Date());
        
        // Tạo spinner
        dateSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(editor);
        
        // Thêm spinner vào panel
        add(dateSpinner, BorderLayout.CENTER);
        
        // Lưu ngày được chọn
        date = (Date) dateSpinner.getValue();
        
        // Thêm listener để cập nhật ngày khi thay đổi
        dateSpinner.addChangeListener(e -> date = (Date) dateSpinner.getValue());
    }

    /**
     * Đặt ngày cho component
     * @param date Ngày cần đặt
     */
    public void setDate(Date date) {
        this.date = date;
        dateSpinner.setValue(date);
    }

    /**
     * Lấy ngày đã chọn
     * @return Ngày đã chọn
     */
    public Date getDate() {
        return date;
    }
}