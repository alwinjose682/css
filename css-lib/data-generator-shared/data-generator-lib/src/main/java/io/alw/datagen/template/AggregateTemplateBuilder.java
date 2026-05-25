package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.*;
import java.util.function.Supplier;

public abstract class AggregateTemplateBuilder<T extends TestDataGeneratable, B, R> extends TemplateBuilder<T> {
    private final Deque<Supplier<B>> relObjectBuilderSuppliers;

    protected AggregateTemplateBuilder(T parent) {
        super(parent);
        this.relObjectBuilderSuppliers = new LinkedList<>();
    }

    protected abstract R buildRelatedObject(B relatedObjectBuilder);

    /// This method can be called recursively
    public TemplateBuilder<T> withRelatedObjectBuilder(Supplier<B> firstBuildStep) {
        relObjectBuilderSuppliers.push(firstBuildStep);
        return this;
    }

    /// Builds the parent template and then its related objects, if any.
    /// The related objects can access the build output, T, during their builds.
    @Override
    public List<T> build() {
        // 1. Build the template
        final T parent = finalBuildInstruction();
        final List<T> result = new ArrayList<>();
        result.add(parent);

        // 4. Build all related objects using functions that do not require any operand
        while (!relObjectBuilderSuppliers.isEmpty()) {
            Supplier<B> firstBuildStep = relObjectBuilderSuppliers.pop();
            B resultBuilder = firstBuildStep.get();
            R related = buildRelatedObject(resultBuilder);
            result.add(related);
        }

        return Collections.unmodifiableList(result);
    }
}
