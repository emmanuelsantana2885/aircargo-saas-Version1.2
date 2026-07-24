package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        PageResponse<T> p = new PageResponse<>();
        p.setContent(content);
        p.setPage(page);
        p.setSize(size);
        p.setTotalElements(totalElements);
        p.setTotalPages(size > 0 ? (int) Math.ceil((double) totalElements / size) : 0);
        p.setFirst(page == 0);
        p.setLast((long) (page + 1) * size >= totalElements);
        return p;
    }
}
