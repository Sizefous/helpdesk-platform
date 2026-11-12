package com.rashid.helpdesk.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.TreeSet;

public final class PageRequests {

    public static final int MAX_SIZE = 100;

    private PageRequests() {
    }

    public static Pageable of(int page, int size, String sortBy, String direction, Set<String> allowedSortFields) {
        if (!allowedSortFields.contains(sortBy)) {
            throw new BadRequestException("sortBy must be one of: " + String.join(", ", new TreeSet<>(allowedSortFields)));
        }
        Sort.Direction dir = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new BadRequestException("direction must be asc or desc"));

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        // The id tie-breaker keeps page boundaries stable when many rows share the same createdAt.
        return PageRequest.of(safePage, safeSize, Sort.by(dir, sortBy).and(Sort.by(Sort.Direction.ASC, "id")));
    }
}