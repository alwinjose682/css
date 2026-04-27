package io.alw.datagen.template;

public interface CountAware {
    long counter();

    /// The sub class must invoke this method at the appropriate place to increment the counter
    void incrementCounter();

    /// TO check, for example, the item occurs at a 10th position in a sequence
    default boolean isAnNthItem(int nth) {
        return counter() % nth == 0;
    }
}
