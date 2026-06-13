package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AggregateTemplateBuilder<T extends TestDataGeneratable, B1, B2, I> extends TemplateBuilder<T, AggregateTemplateBuilderResult<T, I>> {
    private final Deque<BuildItem<B1, I>> groupedItemBuilders;
    private final Deque<BuildItem<B2, I>> relatedItemBuilders;

    protected AggregateTemplateBuilder(T parent) {
        super(parent);
        this.groupedItemBuilders = new LinkedList<>();
        this.relatedItemBuilders = new LinkedList<>();
    }

    protected abstract I buildGroupedItem(B1 relatedObjectBuilder);

    protected abstract I buildRelatedItem(B2 relatedObjectBuilder);

    /// This method should be used if the final build output`R` needs to be associated with the root result `T`.
    /// The association is made by the implementation of the abstract method [AggregateTemplateBuilder#buildGroupedItem(B1)].
    ///
    /// This method can be called recursively
    ///
    /// NOTE: The items inserted in the queue will be removed in the same order as they are inserted
    public AggregateTemplateBuilder<T, B1, I> withGroupedItem(Consumer<I> callback, Supplier<B1> buildStep) {
        groupedItemBuilders.addFirst(new BuildItem<>(callback, buildStep));
        return this;
    }

    /// The final build output`R` from items added to this method is NOT intended to be associated with the root result `T`.
    /// The association is made by the implementation of the abstract method [AggregateTemplateBuilder#buildGroupedItem(B1)].
    ///
    /// This method can be called recursively
    ///
    /// NOTE: The items inserted in the queue will be removed in the same order as they are inserted
    public AggregateTemplateBuilder<T, B1, I> withRelatedItem(Consumer<I> callback, Runnable runnableAfterCallback, Supplier<B2> buildStep) {
        relatedItemBuilders.addFirst(new BuildItem<>(callback, runnableAfterCallback, buildStep));
        return this;
    }

    public AggregateTemplateBuilder<T, B1, I> withRelatedItem(Supplier<B2> buildStep) {
        relatedItemBuilders.addFirst(new BuildItem<>(buildStep));
        return this;
    }

    public AggregateTemplateBuilder<T, B1, I> withRelatedItem(Runnable buildStep) {
        relatedItemBuilders.addFirst(new BuildItem<>(buildStep));
        return this;
    }

    /// Builds the parent template and then its related objects, if any.
    /// The related objects can access the build output, T, during their builds.
    ///
    /// NOTE: The grouped and related items will be removed(retrieved for build) in the same order as they were inserted
    ///
    /// see also {@link AggregateTemplateBuilder#buildItem(BuildItem, List)}
    @Override
    public AggregateTemplateBuilderResult<T, I> build() {
        // 1. Build the template
        final T root = buildRootTemplate();

        // 2. Build items that need to be grouped together with parent/root item
        final List<I> groupedItems = new ArrayList<>();
        while (!groupedItemBuilders.isEmpty()) {
            BuildItem<B1, I> buildItem = groupedItemBuilders.removeLast();
            buildItem(buildItem, groupedItems);
        }

        final List<I> relatedItems = new ArrayList<>();
        // 3. Build items that do NOT need to be grouped together with parent/root item
        while (!relatedItemBuilders.isEmpty()) {
            BuildItem<B1, I> buildItem = relatedItemBuilders.removeLast();
            buildItem(buildItem, relatedItems);
        }

        return new AggregateTemplateBuilderResult<>(root, groupedItems, relatedItems);
    }

    /// If the item is a buildable item:
    /// 1) build the item
    /// 2) execute callback if any
    /// 3) execute the runnableAfterCallback if any and
    /// 4) add the buildResult to the resultList
    ///
    /// If the item is just a runnable, just execute the runnable. There is no build result in this case.
    ///
    /// NOTE: The buildItem cannot be both a runnable and buildable item. It is ensured so
    private void buildItem(BuildItem<B1, I> buildItem, final List<I> resultList) {
        Supplier<B1> buildStep = buildItem.buildStep();

        // If a buildable item:
        if (buildStep != null) {
            B1 resultBuilder = buildStep.get();
            I result = buildGroupedItem(resultBuilder);

            // callback
            Consumer<I> callback = buildItem.callback();
            if (callback != null) {
                callback.accept(result);
            }

            // runnableAfterCallback
            Runnable runnableAfterCallback = buildItem.runnableAfterCallback();
            if (runnableAfterCallback != null) {
                runnableAfterCallback.run();
            }

            // add the result to the resultList
            resultList.add(result);
        }
        // If note a buildable item:
        else if (buildItem.runnable() != null) {
            buildItem.runnable().run();
        }
    }
}
