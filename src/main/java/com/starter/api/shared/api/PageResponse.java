package com.starter.api.shared.api;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalItems) {
        int totalPages = size > 0 ? (int) ((totalItems + size - 1) / size) : 0;
        return new PageResponse<>(List.copyOf(items), page, size, totalItems, totalPages);
    }
}
