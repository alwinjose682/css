package io.alw.datagen.formattingtemplate;

import io.alw.datagen.model.AffixPosition;

import java.util.List;

///  SimpleConcatenatingTemplate suffixes the 'affixer'
public final class SimpleConcatenatingTemplate implements TokenFormattingTemplate<String, String> {
    private final static SimpleConcatenatingTemplate singletonInstance = new SimpleConcatenatingTemplate();

    private SimpleConcatenatingTemplate() {
    }

    public static SimpleConcatenatingTemplate singleton() {
        return singletonInstance;
    }

    @Override
    public String apply(String delimiter, List<String> values, String affixStr, AffixPosition affixPosition, int numOfAffixers) {
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < numOfAffixers; ++idx) {
            sb.append(affixStr);
        }

        String finalAffixStr = sb.toString(); // The finalAffixStr will be empty if numOfAffixers==0, which is the correct behaviour

        return switch (affixPosition) {
            case PREFIX -> finalAffixStr + String.join(delimiter, values);
            case SUFFIX -> String.join(delimiter, values) + finalAffixStr;
            case PREFIX_OF_LAST_TOKEN -> {
                StringBuilder sb2 = new StringBuilder();
                for (int i = 0; i < values.size() - 1; i++) {
                    sb2.append(values.get(i));
                }
                sb2
                        .append(finalAffixStr)
                        .append(values.getLast());
                yield sb2.toString();
            }
        };
    }
}
