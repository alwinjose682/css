package io.alw.datagen.template;

import java.util.function.UnaryOperator;

record RelatedTemplateBuilder<T>(UnaryOperator<T> builderFunc, T operand) {
}
