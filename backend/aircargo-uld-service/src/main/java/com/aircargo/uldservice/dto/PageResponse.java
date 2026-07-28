package com.aircargo.uldservice.dto;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PageResponse() {}

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

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }
    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
}
