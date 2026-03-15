package com.group1.catalogservice.domain.model;

import java.util.List;

public record PageResult<T>(
        List<T> data,
        int totalElements,
        int pageNumber,
        int totalPages,
        boolean isFirst,
        boolean isLast,
        boolean hasNext,
        boolean hasPrevious
) {

}
