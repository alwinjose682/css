package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AggregateTemplateBuilder<T extends TestDataGeneratable, B, R> extends TemplateBuilder<T, AggregateTemplateBuilderResult<T, R>> {
    private final Deque<BuildItem<B, R>> groupedItemBuilders;
    private final Deque<BuildItem<B, R>> relatedItemBuilders;

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
    ///
    /// NOTE: The items inserted in the queue will be removed in the same order as they are inserted
    public AggregateTemplateBuilder<T, B, R> withGroupedItem(Consumer<R> callback, Supplier<B> buildStep) {
        groupedItemBuilders.addFirst(new BuildItem<>(callback, buildStep));
        return this;
    }

    public AggregateTemplateBuilder<T, B, R> withGroupedItem(Supplier<B> buildStep) {
        groupedItemBuilders.addFirst(new BuildItem<>(buildStep));
        return this;
    }

    /// The final build output`R` from items added to this method is NOT intended to be associated with the root result `T`.
    /// The association is made by the implementation of the abstract method [AggregateTemplateBuilder#buildGroupedOrRelatedItem(B)].
    ///
    /// This method can be called recursively
    ///
    /// NOTE: The items inserted in the queue will be removed in the same order as they are inserted
    public AggregateTemplateBuilder<T, B, R> withRelatedItem(Consumer<R> callback, Supplier<B> buildStep) {
        relatedItemBuilders.addFirst(new BuildItem<>(callback, buildStep));
        return this;
    }

    public AggregateTemplateBuilder<T, B, R> withRelatedItem(Runnable buildStep) {
        relatedItemBuilders.addFirst(new BuildItem<>(buildStep));
        return this;
    }

    /// Builds the parent template and then its related objects, if any.
    /// The related objects can access the build output, T, during their builds.
    ///
    /// NOTE: The grouped and related items will be removed(retrieved for build) in the same order as they were inserted
    @Override
    public AggregateTemplateBuilderResult<T, R> build() {
        // 1. Build the template
        final T root = buildRootTemplate();

        // 2. Build items that need to be grouped together with parent/root item
        final List<R> groupedItems = new ArrayList<>();
        while (!groupedItemBuilders.isEmpty()) {
            BuildItem<B, R> buildItem = groupedItemBuilders.removeLast();
            buildItem(buildItem, groupedItems);
        }

        final List<R> relatedItems = new ArrayList<>();
        // 3. Build items that do NOT need to be grouped together with parent/root item
        while (!relatedItemBuilders.isEmpty()) {
            BuildItem<B, R> buildItem = relatedItemBuilders.removeLast();
            buildItem(buildItem, relatedItems);
        }

        return new AggregateTemplateBuilderResult<>(root, groupedItems, relatedItems);
    }

    private void buildItem(BuildItem<B, R> buildItem, final List<R> resultList) {
        Supplier<B> buildStep = buildItem.buildStep();

        // If the item is a buildable item: build the item, execute callback if any and add to the resultList
        if (buildStep != null) {
            B resultBuilder = buildStep.get();
            R result = buildGroupedOrRelatedItem(resultBuilder);

            Consumer<R> callback = buildItem.callback();
            if (callback != null) {
                callback.accept(result);
            }

            resultList.add(result);
        }
        // If the item is just a runnable (The item cannot be both a runnable and buildable item)
        else if (buildItem.runnable() != null) {
            buildItem.runnable().run();
        }
    }
}
