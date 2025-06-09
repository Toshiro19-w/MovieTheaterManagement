package com.cinema.models.dto;

/**
 * Item có thể chọn trong combobox
 */
public class CheckableItem {
    private Object id;
    private String text;
    private boolean selected;
    
    public CheckableItem(Object id, String text, boolean selected) {
        this.id = id;
        this.text = text;
        this.selected = selected;
    }
    
    public Object getId() {
        return id;
    }
    
    public String getText() {
        return text;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public String toString() {
        return text;
    }
}