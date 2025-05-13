package io.alw.css.refdataloader.dao.model;

public record PageRequest(int[] pageNum, int pageSize) {
    public static PageRequest first(int pageSize) {
        return new PageRequest(new int[]{0}, pageSize);
    }

    public void next() {
        ++pageNum[0];
    }
}
