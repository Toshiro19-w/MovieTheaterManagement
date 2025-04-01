package com.cinema.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ScannerUtils {
    private static Scanner sc = new Scanner(System.in);

    // Đọc số nguyên (int) với kiểm tra ngoại lệ
    public static int getInt(String message) {
        int value;
        while (true) {
            try {
                System.out.print(message);
                value = Integer.parseInt(sc.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi! Vui lòng nhập số nguyên");
            }
        }
    }

    //Kiểm tra số thực
    public static double getDouble(String message) {
        double value;
        while (true) {
            try {
                System.out.print(message);
                value = Double.parseDouble(sc.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi! Vui lòng nhập số thực");
            }
        }
    }

    // Đọc chuỗi với kiểm tra ngoại lệ (không được nhập rỗng)
    public static String getString(String message) {
        String value;
        while (true) {
            System.out.print(message);
            value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Lỗi! Vui lòng không để trống");
        }
    }

    // Kiểm tra số điện thoại hợp lệ
    public static String getPhoneNumber(String message) {
        while (true) {
            System.out.print(message);
            String phone = sc.nextLine().trim();

            if (phone.isEmpty()) {
                System.out.println("Lỗi! Vui lòng không để trống.");
                continue;
            }

            if (!phone.matches("^0\\d{9,10}$")) {
                System.out.println("Lỗi! Số điện thoại phải bắt đầu bằng '0' và có 10-11 chữ số.");
                continue;
            }

            return phone; // Trả về số hợp lệ
        }
    }

    //Kiểm tra ngày, tháng, năm
    public static LocalDate getDate(String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Lỗi! Vui lòng không để trống.");
                continue; // Nhập lại ngay lập tức
            }

            try {
                LocalDate date = LocalDate.parse(input, formatter);

                if (date.isAfter(LocalDate.now())) {
                    System.out.println("Lỗi! Ngày không thể ở tương lai.");
                    continue; // Nhập lại
                }

                if (date.getYear() < 1900) {
                    System.out.println("Lỗi! Năm không thể nhỏ hơn 1900.");
                    continue; // Nhập lại
                }

                return date; // Hợp lệ, trả về kết quả
            } catch (DateTimeParseException e) {
                System.out.println("Lỗi! Ngày tháng không hợp lệ. Hãy nhập đúng định dạng dd/MM/yyyy.");
                continue; // Nhập lại ngay
            }
        }
    }

    //Kiểm tra giới tính (Nam/ Nữ)
    public static String getGender(String message) {
        while (true) {
            System.out.print(message);
            String gender = sc.nextLine().trim().toLowerCase();

            if (gender.isEmpty()) {
                System.out.println("Lỗi! Vui lòng không để trống.");
            } else if (gender.equals("nam") || gender.equals("nữ")) {
                return gender.substring(0, 1).toUpperCase() + gender.substring(1); // Viết hoa chữ cái đầu
            } else {
                System.out.println("Lỗi! Chỉ được nhập 'Nam' hoặc 'Nữ'.");
            }
        }
    }
}


