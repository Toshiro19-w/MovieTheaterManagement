package com.cinema.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Lớp tiện ích để áp dụng các thành phần UI hiện đại từ ModernUIComponents
 * vào các view khác trong ứng dụng
 */
public class ModernUIApplier {
    
    /**
     * Áp dụng style hiện đại cho JTable
     */
    public static void applyModernTableStyle(JTable table) {
        table.setFont(UIConstants.BODY_FONT);
        table.setRowHeight(UIConstants.ROW_HEIGHT);
        table.setGridColor(UIConstants.TABLE_GRID_COLOR);
        table.setSelectionBackground(UIConstants.TABLE_SELECTION_BG);
        table.setSelectionForeground(UIConstants.TEXT_COLOR);
        
        // Thiết lập header
        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.TITLE_FONT);
        header.setBackground(UIConstants.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        
        // Căn giữa header
        ((DefaultTableCellRenderer)header.getDefaultRenderer())
            .setHorizontalAlignment(JLabel.CENTER);
    }
    
    /**
     * Áp dụng style hiện đại cho JButton
     */
    public static JButton createModernButton(String text, Color bgColor, Color textColor) {
        return ModernUIComponents.createButton(text, bgColor, textColor);
    }
    
    /**
     * Tạo nút với màu mặc định
     */
    public static JButton createPrimaryButton(String text) {
        return createModernButton(text, UIConstants.PRIMARY_COLOR, Color.WHITE);
    }
    
    /**
     * Tạo nút thứ cấp
     */
    public static JButton createSecondaryButton(String text) {
        return createModernButton(text, UIConstants.SECONDARY_COLOR, UIConstants.TEXT_COLOR);
    }
    
    /**
     * Áp dụng style hiện đại cho JTextField
     */
    public static JTextField createModernTextField(String placeholder) {
        return ModernUIComponents.createTextField(placeholder);
    }
    
    /**
     * Áp dụng style hiện đại cho JPanel với hiệu ứng đổ bóng và góc bo tròn
     */
    public static JPanel createModernPanel() {
        return ModernUIComponents.createRoundedPanel();
    }
    
    /**
     * Áp dụng style hiện đại cho JComboBox
     */
    public static void applyModernComboBoxStyle(JComboBox<?> comboBox) {
        comboBox.setFont(UIConstants.BODY_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM, 
                                           UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM)
        ));
    }
    
    /**
     * Áp dụng style hiện đại cho JLabel
     */
    public static JLabel createModernHeaderLabel(String text) {
        return ModernUIComponents.createHeaderLabel(text);
    }
    
    /**
     * Áp dụng style hiện đại cho JLabel thông tin
     */
    public static JLabel createModernInfoLabel(String text) {
        return ModernUIComponents.createInfoLabel(text);
    }
    
    /**
     * Tạo panel với border và tiêu đề hiện đại
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = createModernPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR),
            title,
            0, 0,
            UIConstants.TITLE_FONT,
            UIConstants.PRIMARY_COLOR
        ));
        return panel;
    }
    
    /**
     * Tạo border đổ bóng
     */
    public static AbstractBorder createShadowBorder() {
        return new ModernUIComponents.ShadowBorder();
    }
    
    /**
     * Áp dụng hiệu ứng hover cho component
     */
    public static void applyHoverEffect(JComponent component, Color normalColor, Color hoverColor) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                component.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                component.setBackground(normalColor);
            }
        });
    }
    
    /**
     * Tạo menu item cho sidebar
     */
    public static JPanel createSidebarMenuItem(String text, Icon icon, boolean isSelected, ActionListener actionListener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(isSelected ? UIConstants.SELECTED_COLOR : UIConstants.SIDEBAR_COLOR);
        panel.setBorder(new EmptyBorder(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_LARGE, 
                                       UIConstants.PADDING_MEDIUM, UIConstants.PADDING_LARGE));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Create panel for icon and text
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UIConstants.PADDING_MEDIUM, 0));
        contentPanel.setOpaque(false);
        
        // Add icon if provided
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            contentPanel.add(iconLabel);
        }
        
        // Add text
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(UIConstants.BUTTON_FONT);
        textLabel.setForeground(isSelected ? Color.WHITE : UIConstants.TEXT_COLOR);
        contentPanel.add(textLabel);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Add action listener if provided
        if (actionListener != null) {
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    actionListener.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, text));
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) {
                        panel.setBackground(UIConstants.HOVER_COLOR);
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) {
                        panel.setBackground(UIConstants.SIDEBAR_COLOR);
                    }
                }
            });
            panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        return panel;
    }
}