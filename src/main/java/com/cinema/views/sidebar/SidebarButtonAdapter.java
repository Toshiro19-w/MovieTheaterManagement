package com.cinema.views.sidebar;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Mouse adapter for handling hover and selection effects on sidebar buttons
 */
public class SidebarButtonAdapter extends MouseAdapter {
    private final JComponent component;
    private final JLabel textLabel;
    private final Color hoverColor;
    private final Color textColor;
    private final Color selectedTextColor;
    private final SidebarMenuItem menuItem;

    public SidebarButtonAdapter(JComponent component, JLabel textLabel, Color hoverColor, 
                               Color textColor, Color selectedTextColor, SidebarMenuItem menuItem) {
        this.component = component;
        this.textLabel = textLabel;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
        this.selectedTextColor = selectedTextColor;
        this.menuItem = menuItem;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (!menuItem.isSelected()) {
            component.setBackground(hoverColor);
            component.repaint();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (!menuItem.isSelected()) {
            component.setBackground(component.getParent().getBackground());
            component.repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (menuItem.getAction() != null) {
            menuItem.getAction().actionPerformed(null);
        }
    }
    
    public void updateSelectionState() {
        if (menuItem.isSelected()) {
            textLabel.setForeground(selectedTextColor);
            component.setBackground(hoverColor);
        } else {
            textLabel.setForeground(textColor);
            component.setBackground(component.getParent().getBackground());
        }
        component.repaint();
    }
}