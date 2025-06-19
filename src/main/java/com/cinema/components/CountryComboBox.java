package com.cinema.components;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.cinema.utils.ValidationUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

public class CountryComboBox extends JComboBox<String> {
    private final EventList<String> allCountries;
    private final FilterList<String> filteredCountries;
    private final SortedList<String> sortedCountries;
    private boolean hasError = false;
    private AutoCompleteSupport<String> autoCompleteSupport;

    public CountryComboBox() {
        super();
        
        // Khởi tạo danh sách quốc gia với GlazedLists
        allCountries = new BasicEventList<>();
        allCountries.addAll(ValidationUtils.getValidCountries());
        
        // Sắp xếp danh sách
        sortedCountries = new SortedList<>(allCountries);
        
        // Tạo danh sách đã lọc
        filteredCountries = new FilterList<>(sortedCountries);
        
        // Thiết lập model cho combobox
        setModel(new DefaultComboBoxModel<>(filteredCountries.toArray(String[]::new)));
        setEditable(true);
        
        // Thiết lập AutoComplete
        autoCompleteSupport = AutoCompleteSupport.install(this, filteredCountries, List::add);
        
        // Lấy text field từ combobox
        JTextField editor = (JTextField) getEditor().getEditorComponent();
        
        // Thêm listener cho việc tìm kiếm
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter(editor.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter(editor.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter(editor.getText());
            }
        });

        // Thêm key listener để xử lý các phím đặc biệt
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String selected = editor.getText();
                    if (ValidationUtils.isValidCountry(selected)) {
                        setSelectedItem(selected);
                    }
                }
            }
        });
    }

    private void updateFilter(String searchText) {
        SwingUtilities.invokeLater(() -> {
            String text = searchText.toLowerCase();
            filteredCountries.setMatcher(country -> 
                country.toLowerCase().contains(text)
            );
        });
    }

    @Override
    public void setSelectedItem(Object item) {
        if (item instanceof String country) {
            if (ValidationUtils.isValidCountry(country)) {
                super.setSelectedItem(country);
            }
        }
    }

    public Document getDocument() {
        return ((JTextField) getEditor().getEditorComponent()).getDocument();
    }

    public String getText() {
        JTextField editor = (JTextField) getEditor().getEditorComponent();
        return editor.getText();
    }

    public void setError(boolean error) {
        this.hasError = error;
        repaint();
    }

    public boolean hasError() {
        return hasError;
    }

    @Override
    public void removeNotify() {
        // Cleanup GlazedLists resources
        if (autoCompleteSupport != null) {
            try {
                autoCompleteSupport.uninstall();
            } catch (IllegalStateException ex) {
                // Đã uninstall rồi thì bỏ qua, không crash
            }
            autoCompleteSupport = null;
        }
        if (filteredCountries != null) {
            filteredCountries.dispose();
        }
        if (sortedCountries != null) {
            sortedCountries.dispose();
        }
        if (allCountries != null) {
            allCountries.dispose();
        }
        super.removeNotify();
    }
} 