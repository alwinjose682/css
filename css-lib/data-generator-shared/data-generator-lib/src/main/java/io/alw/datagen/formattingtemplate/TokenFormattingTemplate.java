package io.alw.datagen.formattingtemplate;

import io.alw.datagen.model.AffixPosition;

import java.util.List;

public sealed interface TokenFormattingTemplate<T, R> permits SimpleConcatenatingTemplate, WidthAwareConcatenatingTemplate {
    default R apply(String delimiter, List<T> values, String affixStr) {
        return apply(delimiter, values, affixStr, AffixPosition.SUFFIX, 1);
    }

    R apply(String delimiter, List<T> values, String affixStr, AffixPosition affixPosition, int numOfAffixers);
}
