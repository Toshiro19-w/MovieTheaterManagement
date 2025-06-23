package com.cinema.utils;

import java.util.Random;

/**
 * Lớp tiện ích để tạo mã xác nhận
 */
public class VerificationCodeGenerator {
    
    /**
     * Tạo mã xác nhận ngẫu nhiên gồm 6 chữ số
     * 
     * @return Chuỗi 6 chữ số
     */
    public static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Tạo số ngẫu nhiên từ 100000 đến 999999
        return String.valueOf(code);
    }
    
    /**
     * Tạo mã xác nhận với độ dài tùy chỉnh
     * 
     * @param length Độ dài của mã xác nhận
     * @return Chuỗi chữ số với độ dài chỉ định
     */
    public static String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Độ dài mã xác nhận phải lớn hơn 0");
        }
        
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();
        
        // Đảm bảo chữ số đầu tiên không phải là 0
        codeBuilder.append(1 + random.nextInt(9));
        
        // Tạo các chữ số còn lại
        for (int i = 1; i < length; i++) {
            codeBuilder.append(random.nextInt(10));
        }
        
        return codeBuilder.toString();
    }
}