package io.alw.css.refdataloader.dao.model;

import java.util.List;

public record Page<T>(List<T> content, PageRequest pageRequest) {
}
