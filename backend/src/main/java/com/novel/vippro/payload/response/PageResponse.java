package com.novel.vippro.payload.response;

import java.util.List;
import org.springframework.data.domain.Page;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageResponse<T> {
    private int totalPages;
    private long totalElements;
    private int pageNumber;
    private int pageSize;
    private List<T> content;

    public PageResponse() {
    }

    public PageResponse(Page<T> page) {
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.content = page.getContent();
    }
}