package com.oryzem.backend.modules.projects.repository;

import java.util.List;

public record PagedResult<T>(
        List<T> items,
        String nextPageToken
) {
}
