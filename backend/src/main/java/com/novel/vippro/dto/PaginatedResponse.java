package com.novel.vippro.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaginatedResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}