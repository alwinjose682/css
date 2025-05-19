package io.alw.datagen.definition;

public interface CountAware {
    long counter();

    /// The sub class must invoke this method at the appropriate place to increment the counter
    void incrementCounter();

    /// TO check, for example, the definition is 10th definition or the definition is the next 10th definition etc.
    default boolean isAnNthDefinition(int nth) {
        return counter() % nth == 0;
    }
}
