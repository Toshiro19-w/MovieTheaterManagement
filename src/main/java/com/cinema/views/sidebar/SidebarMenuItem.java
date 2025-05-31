package com.cinema.views.sidebar;

import javax.swing.ImageIcon;
import java.awt.event.ActionListener;

/**
 * Class representing a menu item in the sidebar
 */
public class SidebarMenuItem {
    private final String text;
    private final String feature;
    private final ImageIcon icon;
    private ActionListener action;
    private boolean selected;

    public SidebarMenuItem(String text, String feature, ImageIcon icon, ActionListener action) {
        this.text = text;
        this.feature = feature;
        this.icon = icon;
        this.action = action;
        this.selected = false;
    }

    public String getText() {
        return text;
    }

    public String getFeature() {
        return feature;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public ActionListener getAction() {
        return action;
    }
    
    public void setAction(ActionListener action) {
        this.action = action;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}