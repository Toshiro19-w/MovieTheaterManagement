package com.cinema.utils;

import com.cinema.models.Ve;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class VePdfExporter {
    public static void exportVeToPdf(Ve ve, String path) throws Exception {
        Document doc = new Document(PageSize.A6, 20, 20, 20, 20);
        PdfWriter.getInstance(doc, new FileOutputStream(path));
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 11);

        Paragraph p = new Paragraph("CINEMA HUB", titleFont);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
        doc.add(new Paragraph("Vé xem phim", titleFont));

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Phim: " + ve.getTenPhim(), normal));
        doc.add(new Paragraph("Ngày chiếu: " + ve.getNgayGioChieu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                "   Giờ: " + ve.getNgayGioChieu().format(DateTimeFormatter.ofPattern("HH:mm"))));
        doc.add(new Paragraph("Phòng: " + ve.getTenPhong() + "   Ghế: " + ve.getSoGhe(), normal));
        doc.add(new Paragraph("Giá vé: " + ve.getGiaVeSauGiam() + " VNĐ", normal));
        doc.add(new Paragraph("Mã vé: " + ve.getMaVe(), normal));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Chúc bạn xem phim vui vẻ!", normal));
        doc.close();
    }
}