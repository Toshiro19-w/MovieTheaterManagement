package com.cinema.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * ComboBox cho phép chọn nhiều giá trị
 */
public class MultiSelectComboBox extends JComboBox<CheckableItem> {
    private static final long serialVersionUID = 1L;
    
    private Map<Object, CheckableItem> items = new HashMap<>();
    private boolean keepOpen = false;
    private List<ActionListener> actionListeners = new ArrayList<>();
    
    public MultiSelectComboBox() {
        init();
    }
    
    private void init() {
        setRenderer(new CheckBoxRenderer());
        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                keepOpen = true;
            }
            
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (keepOpen) {
                    showPopup();
                }
                keepOpen = false;
            }
            
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                keepOpen = false;
            }
        });
        
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
                    int index = getSelectedIndex();
                    if (index >= 0) {
                        CheckableItem item = getItemAt(index);
                        item.setSelected(!item.isSelected());
                        keepOpen = true;
                        setSelectedIndex(-1); // Reset selection
                        setSelectedIndex(index);
                        repaint(); // Cập nhật giao diện
                        
                        // Thông báo cho các listener
                        for (ActionListener listener : actionListeners) {
                            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "selectionChanged"));
                        }
                    }
                }
            }
        });
    }
    
    @Override
    public void addActionListener(ActionListener l) {
        actionListeners.add(l);
    }
    
    @Override
    public void removeActionListener(ActionListener l) {
        actionListeners.remove(l);
    }
    
    /**
     * Thêm item vào combobox
     * 
     * @param id ID của item
     * @param text Text hiển thị
     * @param selected Trạng thái chọn
     */
    public void addItem(Object id, String text, boolean selected) {
        CheckableItem item = new CheckableItem(id, text, selected);
        items.put(id, item);
        addItem(item);
    }
    
    /**
     * Lấy danh sách ID của các item đã chọn
     * 
     * @return Danh sách ID
     */
    public List<Object> getSelectedIds() {
        List<Object> selectedIds = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            if (item.isSelected()) {
                selectedIds.add(item.getId());
            }
        }
        return selectedIds;
    }
    
    /**
     * Lấy danh sách text của các item đã chọn
     * 
     * @return Danh sách text
     */
    public List<String> getSelectedTexts() {
        List<String> selectedTexts = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            if (item.isSelected()) {
                selectedTexts.add(item.getText());
            }
        }
        return selectedTexts;
    }
    
    /**
     * Lấy text hiển thị của các item đã chọn, phân cách bởi dấu phẩy
     * 
     * @return Text hiển thị
     */
    public String getSelectedItemsText() {
        List<String> texts = getSelectedTexts();
        if (texts.isEmpty()) {
            return "";
        }
        return String.join(", ", texts);
    }
    
    /**
     * Kiểm tra xem có item nào được chọn không
     * 
     * @return true nếu có ít nhất một item được chọn
     */
    public boolean hasSelectedItems() {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItemAt(i).isSelected()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Thiết lập trạng thái chọn cho các item
     * 
     * @param ids Danh sách ID cần chọn
     */
    public void setSelectedIds(List<?> ids) {
        // Đảm bảo ids không null
        if (ids == null) {
            ids = new ArrayList<>();
        }
        
        // Reset tất cả các item về trạng thái không chọn
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            item.setSelected(false);
        }
        
        // Chọn các item có ID trong danh sách
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            for (Object id : ids) {
                if (id != null && id.equals(item.getId())) {
                    item.setSelected(true);
                    break;
                }
            }
        }
        
        // Cập nhật giao diện
        repaint();
        
        // Thông báo cho các listener
        for (ActionListener listener : actionListeners) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "selectionChanged"));
        }
    }
    
    /**
     * Xóa tất cả item
     */
    @Override
    public void removeAllItems() {
        super.removeAllItems();
        items.clear();
    }
    
    /**
     * Renderer cho checkbox trong combobox
     */
    private class CheckBoxRenderer implements ListCellRenderer<CheckableItem> {
        @Override
        public Component getListCellRendererComponent(JList<? extends CheckableItem> list, CheckableItem value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            if (index == -1) {
                // Hiển thị text khi không có item nào được chọn hoặc có item được chọn
                String text = getSelectedItemsText();
                if (text.isEmpty()) {
                    text = "Chọn thể loại";
                }
                JCheckBox checkbox = new JCheckBox(text);
                checkbox.setBackground(list.getBackground());
                checkbox.setForeground(list.getForeground());
                return checkbox;
            }
            
            // Hiển thị item trong dropdown
            JCheckBox checkbox = new JCheckBox(value.getText());
            checkbox.setSelected(value.isSelected());
            checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            checkbox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return checkbox;
        }
    }
}

/**
 * Item có thể chọn trong combobox
 */
class CheckableItem {
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