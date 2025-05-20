package com.cinema.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Utility class for formatting values
 */
public class FormatUtils {
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,### VND");
    
    /**
     * Format a BigDecimal as currency
     * @param amount Amount to format
     * @return Formatted string
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VND";
        return CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Parse a currency string to BigDecimal
     * @param currencyStr Currency string (e.g. "10,000 VND")
     * @return BigDecimal value
     */
    public static BigDecimal parseCurrency(String currencyStr) {
        if (currencyStr == null || currencyStr.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Remove non-numeric characters except decimal point
        String numericStr = currencyStr.replaceAll("[^0-9.]", "");
        if (numericStr.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            return new BigDecimal(numericStr);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}