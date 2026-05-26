package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.*;
import java.util.function.Supplier;

public abstract class AggregateTemplateBuilder<T extends TestDataGeneratable, B, R> extends TemplateBuilder<T, AggregateTemplateBuilderResult<T,R>> {
    private final Deque<Supplier<B>> groupedItemBuilders;
    private final Deque<Supplier<B>> relatedItemBuilders;

    protected AggregateTemplateBuilder(T parent) {
        super(parent);
        this.groupedItemBuilders = new LinkedList<>();
        this.relatedItemBuilders = new LinkedList<>();
    }

    protected abstract R buildGroupedOrRelatedItem(B relatedObjectBuilder);

    /// This method should be used if the final build output`R` needs to be associated with the root result `T`.
    /// The association is made by the implementation of the abstract method [AggregateTemplateBuilder#buildGroupedOrRelatedItem(B)].
    ///
    /// This method can be called recursively
    public AggregateTemplateBuilder<T,B,R> withGroupedItem(Supplier<B> firstBuildStep) {
        groupedItemBuilders.push(firstBuildStep);
        return this;
    }

    /// The final build output`R` from items added to this method is NOT intended to be associated with the root result `T`.
    /// The association is made by the implementation of the abstract method [AggregateTemplateBuilder#buildGroupedOrRelatedItem(B)].
    ///
    /// This method can be called recursively
    public AggregateTemplateBuilder<T,B,R> withRelatedItem(Supplier<B> firstBuildStep) {
        relatedItemBuilders.push(firstBuildStep);
        return this;
    }

    /// Builds the parent template and then its related objects, if any.
    /// The related objects can access the build output, T, during their builds.
    @Override
    public AggregateTemplateBuilderResult<T,R> build() {
        // 1. Build the template
        final T root = finalBuildInstruction();

        // 2. Build items that need to be grouped together with parent/root item
        final List<R> groupedItems = new ArrayList<>();
        while (!groupedItemBuilders.isEmpty()) {
            Supplier<B> firstBuildStep = groupedItemBuilders.pop();
            B resultBuilder = firstBuildStep.get();
            R related = buildGroupedOrRelatedItem(resultBuilder);
            groupedItems.add(related);
        }

        final List<R> relatedItems = new ArrayList<>();
        // 3. Build items that do NOT need to be grouped together with parent/root item
        while (!relatedItemBuilders.isEmpty()) {
            Supplier<B> firstBuildStep = relatedItemBuilders.pop();
            B resultBuilder = firstBuildStep.get();
            R related = buildGroupedOrRelatedItem(resultBuilder);
            relatedItems.add(related);
        }

        return new AggregateTemplateBuilderResult<>(root, groupedItems, relatedItems);
    }
}
