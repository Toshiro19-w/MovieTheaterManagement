package com.cinema.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.cinema.models.dto.CheckableItem;

/**
 * ComboBox cho phép chọn nhiều giá trị
 */
public class MultiSelectComboBox extends JComboBox<CheckableItem> {
    private static final long serialVersionUID = 1L;
    
    private Map<Object, CheckableItem> items = new HashMap<>();
    private boolean keepOpen = false;
    
    public MultiSelectComboBox() {
        init();
    }
    
    private void init() {
        setRenderer(new CheckBoxRenderer());
        
        // Ngăn không cho dropdown tự đóng khi chọn item
        setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected javax.swing.plaf.basic.ComboPopup createPopup() {
                return new javax.swing.plaf.basic.BasicComboPopup(comboBox) {
                    @Override
                    protected void firePopupMenuWillBecomeInvisible() {
                        if (!keepOpen) {
                            super.firePopupMenuWillBecomeInvisible();
                        }
                    }
                };
            }
        });
        
        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                keepOpen = true;
            }
            
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // Không làm gì khi popup đóng
            }
            
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                keepOpen = false;
            }
        });
        
        super.addActionListener(e -> {
            int index = getSelectedIndex();
            if (index >= 0) {
                CheckableItem item = getItemAt(index);
                item.setSelected(!item.isSelected());
                
                // Update display
                setSelectedIndex(-1);
                repaint();
                
                // Notify item change
                fireItemStateChanged();
            }
        });
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
        super.addItem(item);
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
        if (ids == null) {
            ids = new ArrayList<>();
        }
        
        // Tạo set để tìm kiếm nhanh hơn
        Set<Object> idSet = new HashSet<>(ids);
        
        // Cập nhật trạng thái cho tất cả các item
        boolean changed = false;
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            boolean shouldBeSelected = idSet.contains(item.getId());
            if (item.isSelected() != shouldBeSelected) {
                item.setSelected(shouldBeSelected);
                changed = true;
            }
        }
        
        if (changed) {
            // Cập nhật giao diện
            repaint();
            
            // Thông báo cho các listener
            fireItemStateChanged();
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
     * Thông báo cho các listener khi có sự thay đổi
     */
    protected void fireItemStateChanged() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "selectionChanged");
        for (ActionListener listener : getActionListeners()) {
            listener.actionPerformed(event);
        }
    }

    /**
     * Renderer cho checkbox trong combobox
     */
    private class CheckBoxRenderer implements ListCellRenderer<CheckableItem> {
        private final JCheckBox checkbox = new JCheckBox();
        
        @Override
        public Component getListCellRendererComponent(JList<? extends CheckableItem> list, CheckableItem value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            if (index == -1 && value == null) {
                // Hiển thị text khi không có item nào được chọn hoặc có item được chọn
                String text = getSelectedItemsText();
                if (text.isEmpty()) {
                    text = "Chọn thể loại";
                }
                checkbox.setText(text);
            } else if (value != null) {
                // Hiển thị item trong dropdown
                checkbox.setText(value.getText());
                checkbox.setSelected(value.isSelected());
            }
            
            checkbox.setOpaque(true);
            checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            checkbox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return checkbox;
        }
    }
}

