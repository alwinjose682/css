package io.alw.datagen.definition;

import java.util.function.UnaryOperator;

record RelatedTypeHolder<T>(UnaryOperator<T> relTypeBuilder, T t) {
}
