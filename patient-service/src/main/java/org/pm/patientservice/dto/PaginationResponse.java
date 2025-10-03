package org.pm.patientservice.dto;

import java.util.List;

public class PaginationResponse<T> {
        private List<T> data;
        private int page;
        private int size;
        private long totalElements;

        public PaginationResponse(List<T> data, int page, int size, long totalElements) {
            this.data = data;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
        }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}
