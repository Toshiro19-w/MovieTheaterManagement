package com.cinema.utils;

import com.cinema.models.HoaDon;
import com.cinema.models.Ve;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class HoaDonPdfExporter {
    public static void exportHoaDonToPdf(HoaDon hoaDon, String path) throws Exception {
        Document doc = new Document(PageSize.A5, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(path));
        doc.open();

        // Header
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 12);

        Paragraph p = new Paragraph("CINEMA HUB", titleFont);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("HÓA ĐƠN BÁN VÉ XEM PHIM", bold));
        doc.add(new Paragraph("Mã hóa đơn: " + hoaDon.getMaHoaDon()));
        doc.add(new Paragraph("Ngày lập: " + hoaDon.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Nhân viên bán: " + hoaDon.getTenNhanVien() + " (Mã: " + hoaDon.getMaNhanVien() + ")", normal));
        doc.add(new Paragraph("Khách hàng: " + hoaDon.getTenKhachHang() + " (Mã: " + hoaDon.getMaKhachHang() + ")", normal));
        doc.add(new Paragraph(" "));

        if (hoaDon.getDanhSachVe() != null && !hoaDon.getDanhSachVe().isEmpty()) {
            Ve firstVe = hoaDon.getDanhSachVe().get(0);
            doc.add(new Paragraph("Tên phim: " + firstVe.getTenPhim(), bold));
            doc.add(new Paragraph("Ngày chiếu: " + 
                firstVe.getNgayGioChieu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                "   Giờ: " + firstVe.getNgayGioChieu().format(DateTimeFormatter.ofPattern("HH:mm")) +
                "   Phòng: " + firstVe.getTenPhong()
            ));
            // Ghép ghế
            StringBuilder gheStr = new StringBuilder();
            for (Ve ve : hoaDon.getDanhSachVe()) {
                gheStr.append(ve.getSoGhe()).append(", ");
            }
            if (gheStr.length() > 0) gheStr.setLength(gheStr.length() - 2);
            doc.add(new Paragraph("Ghế: " + gheStr.toString()));
        }

        doc.add(new Paragraph("---------------------------------------------"));
        doc.add(new Paragraph("Thành tiền:     " + hoaDon.getTongTien() + " VNĐ", normal));
        doc.add(new Paragraph("Tiền khách đưa: " + hoaDon.getTienKhachDua() + " VNĐ", normal));
        doc.add(new Paragraph("Tiền thối lại:  " + hoaDon.getTienThoiLai() + " VNĐ", normal));
        doc.add(new Paragraph("Thanh toán:     " + hoaDon.getPhuongThucThanhToan(), normal));
        doc.add(new Paragraph("---------------------------------------------"));
        doc.add(new Paragraph("Cảm ơn Quý khách đã sử dụng dịch vụ!", bold));
        doc.close();
    }
}