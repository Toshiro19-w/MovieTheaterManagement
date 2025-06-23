
package com.cinema.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Lớp HoaDon biểu diễn một hóa đơn trong hệ thống quản lý rạp chiếu phim.
 * Bao gồm thông tin về nhân viên, khách hàng, ngày lập, tổng tiền, phương thức thanh toán,
 * và danh sách vé liên kết với hóa đơn.
 */
public class HoaDon {
    private int maHoaDon; // Mã hóa đơn, ánh xạ với maHoaDon trong bảng HoaDon
    private int maNhanVien; // Mã nhân viên, ánh xạ với maNhanVien
    private Integer maKhachHang; // Mã khách hàng, ánh xạ với maKhachHang
    private LocalDateTime ngayLap; // Ngày lập hóa đơn, ánh xạ với ngayLap
    private String tenNhanVien; // Tên nhân viên, lấy từ bảng NguoiDung
    private String tenKhachHang; // Tên khách hàng, lấy từ bảng NguoiDung
    private BigDecimal tongTien; // Tổng tiền hóa đơn, tính từ danh sách vé
    private BigDecimal tienKhachDua; // Số tiền khách đưa
    private BigDecimal tienThoiLai; // Số tiền thối lại
    private String phuongThucThanhToan; // Phương thức thanh toán: "Tiền mặt" hoặc "Chuyển khoản"
    private List<Ve> danhSachVe; // Danh sách vé liên kết qua bảng ChiTietHoaDon

    /**
     * Constructor mặc định.
     */
    public HoaDon() {
    }

    /**
     * Constructor với các thông tin cơ bản của hóa đơn.
     *
     * @param maHoaDon Mã hóa đơn
     * @param maNhanVien Mã nhân viên lập hóa đơn
     * @param maKhachHang Mã khách hàng mua vé
     * @param ngayLap Ngày lập hóa đơn
     */
    public HoaDon(int maHoaDon, int maNhanVien, int maKhachHang, LocalDateTime ngayLap) {
        this.maHoaDon = maHoaDon;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.ngayLap = ngayLap;
    }

    // Getter và Setter

    /**
     * Lấy mã hóa đơn.
     * @return Mã hóa đơn
     */
    public int getMaHoaDon() {
        return maHoaDon;
    }

    /**
     * Đặt mã hóa đơn.
     * @param maHoaDon Mã hóa đơn, phải lớn hơn 0
     * @throws IllegalArgumentException nếu maHoaDon không hợp lệ
     */
    public void setMaHoaDon(int maHoaDon) {
        if (maHoaDon <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn phải lớn hơn 0");
        }
        this.maHoaDon = maHoaDon;
    }

    /**
     * Lấy mã nhân viên.
     * @return Mã nhân viên
     */
    public int getMaNhanVien() {
        return maNhanVien;
    }

    /**
     * Đặt mã nhân viên.
     * @param maNhanVien Mã nhân viên, có thể là 0 nếu không có nhân viên
     */
    public void setMaNhanVien(int maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    /**
     * Lấy mã khách hàng.
     * @return Mã khách hàng
     */
   

    /**
     * Đặt mã khách hàng.
     * @param maKhachHang Mã khách hàng, có thể là 0 nếu không có khách hàng
     */
   
    public void setMaKhachHang(Integer maKhachHang) {
        this.maKhachHang = maKhachHang;
    }
    public Integer getMaKhachHang() {
        return maKhachHang;
    }
    /**
     * Lấy ngày lập hóa đơn.
     * @return Ngày lập hóa đơn
     */
    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    /**
     * Đặt ngày lập hóa đơn.
     * @param ngayLap Ngày lập hóa đơn, không được null
     * @throws IllegalArgumentException nếu ngayLap là null
     */
    public void setNgayLap(LocalDateTime ngayLap) {
        if (ngayLap == null) {
            throw new IllegalArgumentException("Ngày lập hóa đơn không được null");
        }
        this.ngayLap = ngayLap;
    }

    /**
     * Lấy tên nhân viên.
     * @return Tên nhân viên
     */
    public String getTenNhanVien() {
        return tenNhanVien;
    }

    /**
     * Đặt tên nhân viên.
     * @param tenNhanVien Tên nhân viên
     */
    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    /**
     * Lấy tên khách hàng.
     * @return Tên khách hàng
     */
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    /**
     * Đặt tên khách hàng.
     * @param tenKhachHang Tên khách hàng
     */
    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    /**
     * Lấy tổng tiền hóa đơn.
     * @return Tổng tiền hóa đơn
     */
    public BigDecimal getTongTien() {
        return tongTien;
    }

    /**
     * Đặt tổng tiền hóa đơn.
     * @param tongTien Tổng tiền, phải không âm
     * @throws IllegalArgumentException nếu tongTien âm hoặc null
     */
    public void setTongTien(BigDecimal tongTien) {
        if (tongTien == null || tongTien.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền phải không âm và không được null");
        }
        this.tongTien = tongTien;
    }

    /**
     * Lấy số tiền khách đưa.
     * @return Số tiền khách đưa
     */
    public BigDecimal getTienKhachDua() {
        return tienKhachDua;
    }

    /**
     * Đặt số tiền khách đưa.
     * @param tienKhachDua Số tiền khách đưa, phải không âm
     * @throws IllegalArgumentException nếu tienKhachDua âm
     */
    public void setTienKhachDua(BigDecimal tienKhachDua) {
        if (tienKhachDua != null && tienKhachDua.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền khách đưa phải không âm");
        }
        this.tienKhachDua = tienKhachDua;
    }

    /**
     * Lấy số tiền thối lại.
     * @return Số tiền thối lại
     */
    public BigDecimal getTienThoiLai() {
        return tienThoiLai;
    }

    /**
     * Đặt số tiền thối lại.
     * @param tienThoiLai Số tiền thối lại, phải không âm
     * @throws IllegalArgumentException nếu tienThoiLai âm
     */
    public void setTienThoiLai(BigDecimal tienThoiLai) {
        if (tienThoiLai != null && tienThoiLai.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền thối lại phải không âm");
        }
        this.tienThoiLai = tienThoiLai;
    }

    /**
     * Lấy phương thức thanh toán.
     * @return Phương thức thanh toán ("Tiền mặt" hoặc "Chuyển khoản")
     */
    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    /**
     * Đặt phương thức thanh toán.
     * @param phuongThucThanhToan Phương thức thanh toán
     */
    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    /**
     * Lấy danh sách vé liên kết với hóa đơn.
     * @return Danh sách vé
     */
    public List<Ve> getDanhSachVe() {
        return danhSachVe;
    }

    /**
     * Đặt danh sách vé liên kết với hóa đơn.
     * @param danhSachVe Danh sách vé
     */
    public void setDanhSachVe(List<Ve> danhSachVe) {
        this.danhSachVe = danhSachVe;
    }

    /**
     * So sánh hai đối tượng HoaDon dựa trên maHoaDon.
     * @param o Đối tượng khác
     * @return true nếu cùng maHoaDon, false nếu không
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoaDon hoaDon = (HoaDon) o;
        return maHoaDon == hoaDon.maHoaDon;
    }

    /**
     * Tính mã băm dựa trên maHoaDon.
     * @return Mã băm
     */
    @Override
    public int hashCode() {
        return Objects.hash(maHoaDon);
    }

    /**
     * Trả về biểu diễn chuỗi của đối tượng HoaDon.
     * @return Chuỗi chứa thông tin hóa đơn
     */
    @Override
    public String toString() {
        return "HoaDon{" +
                "maHoaDon=" + maHoaDon +
                ", maNhanVien=" + maNhanVien +
                ", maKhachHang=" + maKhachHang +
                ", ngayLap=" + ngayLap +
                ", tenNhanVien='" + tenNhanVien + '\'' +
                ", tenKhachHang='" + tenKhachHang + '\'' +
                ", tongTien=" + tongTien +
                ", tienKhachDua=" + tienKhachDua +
                ", tienThoiLai=" + tienThoiLai +
                ", phuongThucThanhToan='" + phuongThucThanhToan + '\'' +
                ", danhSachVe=" + danhSachVe +
                '}';
    }
}
