package com.cinema.models.repositories.Interface;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IDashboardRepository {
    /**
     * Lấy dữ liệu doanh thu và số vé bán theo ngày trong khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Map chứa dữ liệu theo ngày, mỗi ngày có số vé bán và doanh thu
     * @throws SQLException Nếu có lỗi truy vấn cơ sở dữ liệu
     */
    Map<LocalDate, DailySalesData> getSalesDataByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException;
    
    /**
     * Lấy tổng số vé bán, doanh thu, đánh giá trung bình trong khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return SummaryData chứa thông tin tổng hợp
     * @throws SQLException Nếu có lỗi truy vấn cơ sở dữ liệu
     */
    SummaryData getSummaryData(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;
    
    /**
     * Lớp chứa dữ liệu tổng hợp về số vé bán, doanh thu, đánh giá
     */
    class SummaryData {
        private int ticketsSold;
        private double revenue;
        private double averageRating;
        
        public SummaryData(int ticketsSold, double revenue, double averageRating) {
            this.ticketsSold = ticketsSold;
            this.revenue = revenue;
            this.averageRating = averageRating;
        }
        
        public int getTicketsSold() {
            return ticketsSold;
        }
        
        public double getRevenue() {
            return revenue;
        }
        
        public double getAverageRating() {
            return averageRating;
        }
    }
    
    /**
     * Đếm tổng số khách hàng đã mua vé
     * @return Số lượng khách hàng
     * @throws SQLException Nếu có lỗi truy vấn cơ sở dữ liệu
     */
    int countTotalCustomers() throws SQLException;
    
    /**
     * Lớp chứa dữ liệu doanh thu và số vé bán trong một ngày
     */
    class DailySalesData {
        private int ticketsSold;
        private double revenue;
        
        public DailySalesData(int ticketsSold, double revenue) {
            this.ticketsSold = ticketsSold;
            this.revenue = revenue;
        }
        
        public int getTicketsSold() {
            return ticketsSold;
        }
        
        public double getRevenue() {
            return revenue;
        }
    }
}