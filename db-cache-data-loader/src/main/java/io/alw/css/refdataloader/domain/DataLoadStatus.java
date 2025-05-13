package io.alw.css.refdataloader.domain;

import io.alw.css.refdataloader.model.DataLoadType;

import java.time.LocalDateTime;

public record DataLoadStatus(
        Integer id,
        DataLoadType loadType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status
) {
}
