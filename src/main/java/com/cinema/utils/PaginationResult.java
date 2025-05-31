package com.cinema.utils;

import java.util.List;
/*  
 * Template phân trang dữ liệu
 */
public class PaginationResult<T> {
    private List<T> data;
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private int totalItems;

    public PaginationResult(List<T> data, int currentPage, int totalPages, int pageSize, int totalItems) {
        this.data = data;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public List<T> getData() {
        return data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }


    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }


    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}