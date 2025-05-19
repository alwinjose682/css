package io.alw.datagen.formattingtemplate;

import io.alw.datagen.model.AffixPosition;

import java.util.List;

public final class WidthAwareConcatenatingTemplate implements TokenFormattingTemplate<String, String> {
    private final int maxFieldLength;
    private final TokenFormattingTemplate<String, String> simpleConcatenatingTemplate;

    public WidthAwareConcatenatingTemplate(int maxFieldLength) {
        this.maxFieldLength = maxFieldLength;
        this.simpleConcatenatingTemplate = SimpleConcatenatingTemplate.singleton();
    }

    @Override
    public String apply(String delimiter, List<String> values, String affixStr, AffixPosition affixPosition, int numOfAffixers) {
        final int maxAllowedIdLength;

        if (affixStr != null && !affixStr.isBlank()) {
            maxAllowedIdLength = maxFieldLength - affixStr.length(); // The affixStr needs to be affixed at least 1 time
        } else {
            maxAllowedIdLength = maxFieldLength;
        }

        // Create the id with delimiter. 'affixStr' will be applied after reducing the 'id' if required.
        final String id = simpleConcatenatingTemplate.apply(delimiter, values, "");

        // Reduce the id to the computed 'maxAllowedIdLength' inorder to affix the affixStr at least 1 time
        final String reducedId;
        if (id.length() > maxAllowedIdLength) {
            int difference = id.length() - maxAllowedIdLength;
            reducedId = id.substring(0, 2) + id.substring(2 + difference + 1);
        } else {
            reducedId = id;
        }

        final int numOfAffixersRequired;
        if (affixStr != null && !affixStr.isBlank()) {
            numOfAffixersRequired = (maxFieldLength - reducedId.length() - 1) / affixStr.length();
        } else {
            numOfAffixersRequired = 0;
        }


        String result = simpleConcatenatingTemplate.apply(delimiter, List.of(reducedId), affixStr, affixPosition, numOfAffixersRequired);
        if (result.length() > maxFieldLength) {
            throw new RuntimeException("The maxLength: " + maxFieldLength + " constraint is exceeded by the resultant string: " + result);
        }

        return result;
    }
}
