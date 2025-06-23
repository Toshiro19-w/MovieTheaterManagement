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

public class MultiSelectComboBox extends JComboBox<CheckableItem> {
    private static final long serialVersionUID = 1L;
    
    private Map<Object, CheckableItem> items = new HashMap<>();
    private boolean keepOpen = false;
    
    public MultiSelectComboBox() {
        init();
    }
    
    private void init() {
        setRenderer(new CheckBoxRenderer());
        
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
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            
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
                
                setSelectedIndex(-1);
                repaint();
                
                fireItemStateChanged();
            }
        });
    }
    
    public void addItem(Object id, String text, boolean selected) {
        if (id == null || text == null) {
            return; // Bỏ qua nếu id hoặc text null
        }
        CheckableItem item = new CheckableItem(id, text, selected);
        items.put(id, item);
        super.addItem(item);
    }
    
    public List<Integer> getSelectedIds() {
        List<Integer> selectedIds = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            if (item != null && item.isSelected()) {
                selectedIds.add(item.getId());
            }
        }
        return selectedIds;
    }
    
    public List<String> getSelectedTexts() {
        List<String> selectedTexts = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            if (item != null && item.isSelected()) {
                selectedTexts.add(item.getText());
            }
        }
        return selectedTexts;
    }
    
    public String getSelectedItemsText() {
        List<String> texts = getSelectedTexts();
        return texts.isEmpty() ? "" : String.join(", ", texts);
    }
    
    public boolean hasSelectedItems() {
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            if (item != null && item.isSelected()) {
                return true;
            }
        }
        return false;
    }
    
    public void setSelectedIds(List<?> ids) {
        if (ids == null || getItemCount() == 0) {
            return; // Bỏ qua nếu ids null hoặc không có mục nào
        }
        
        Set<Object> idSet = new HashSet<>(ids);
        boolean changed = false;
        
        for (int i = 0; i < getItemCount(); i++) {
            CheckableItem item = getItemAt(i);
            if (item != null) {
                boolean shouldBeSelected = idSet.contains(item.getId());
                if (item.isSelected() != shouldBeSelected) {
                    item.setSelected(shouldBeSelected);
                    changed = true;
                }
            }
        }
        
        if (changed) {
            repaint();
            fireItemStateChanged();
        }
    }
    
    @Override
    public void removeAllItems() {
        super.removeAllItems();
        items.clear();
        repaint();
    }
    
    protected void fireItemStateChanged() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "selectionChanged");
        for (ActionListener listener : getActionListeners()) {
            listener.actionPerformed(event);
        }
    }
    
    private class CheckBoxRenderer implements ListCellRenderer<CheckableItem> {
        private final JCheckBox checkbox = new JCheckBox();
        
        @Override
        public Component getListCellRendererComponent(JList<? extends CheckableItem> list, CheckableItem value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (getItemCount() == 0) {
                checkbox.setText("Không có thể loại");
                checkbox.setSelected(false);
                checkbox.setEnabled(false);
                return checkbox;
            }
            
            if (index == -1 && value == null) {
                String text = getSelectedItemsText();
                checkbox.setText(text.isEmpty() ? "Chọn thể loại" : text);
                checkbox.setSelected(false);
            } else if (value != null) {
                checkbox.setText(value.getText());
                checkbox.setSelected(value.isSelected());
            } else {
                checkbox.setText("");
                checkbox.setSelected(false);
            }
            
            checkbox.setOpaque(true);
            checkbox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            checkbox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            checkbox.setEnabled(true);
            return checkbox;
        }
    }

	public void setItems(Map<Integer, String> genreMap) {
		// TODO Auto-generated method stub
		
	}
}