package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AggregateTemplateBuilder<T extends TestDataGeneratable, U extends TestDataGeneratable, TB, UB> extends TemplateBuilder<T, AggregateTemplateBuilderResult<T, U>> {
    private final Deque<ChildBuildItem<U, UB>> groupedItemBuilders;
    private final Deque<ParentBuildItem<T, U, TB>> relatedItemBuilders;

    protected AggregateTemplateBuilder(T parent) {
        super(parent);
        this.groupedItemBuilders = new LinkedList<>();
        this.relatedItemBuilders = new LinkedList<>();
    }

    protected abstract T buildGroupedItem(TB relatedObjectBuilder);

    protected abstract T buildRelatedItem(TB relatedObjectBuilder);

    /// This method can be called recursively
    ///
    /// NOTE: The items inserted in the queue will be removed in the same order as they are inserted
    public AggregateTemplateBuilder<T, U, TB, UB> withGroupedItem(Consumer<U> callback, Supplier<UB> buildStep) {
        groupedItemBuilders.addFirst(new ChildBuildItem<>(callback, buildStep));
        return this;
    }

    /// Builds the parent template and then its related objects, if any.
    /// The related objects can access the build output, T, during their builds.
    ///
    /// NOTE: The grouped and related items will be removed(retrieved for build) in the same order as they were inserted
    ///
    /// see also {@link AggregateTemplateBuilder#buildItem(ChildBuildItem, List)}
    @Override
    public AggregateTemplateBuilderResult<T, U> build() {
        // 1. Build the template
        final T root = buildRootTemplate();

        // 2. Build items that need to be grouped together with parent/root item
        final List<U> groupedItems = new ArrayList<>();
        while (!groupedItemBuilders.isEmpty()) {
            ChildBuildItem<UB, U> childBuildItem = groupedItemBuilders.removeLast();
            buildItem(childBuildItem, groupedItems);
        }

        final List<I> relatedItems = new ArrayList<>();
        // 3. Build items that do NOT need to be grouped together with parent/root item
        while (!relatedItemBuilders.isEmpty()) {
            ChildBuildItem<B1, I> childBuildItem = relatedItemBuilders.removeLast();
            buildItem(childBuildItem, relatedItems);
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
    private void buildItem(ChildBuildItem<B1, I> childBuildItem, final List<I> resultList) {
        Supplier<B1> buildStep = childBuildItem.buildStep();

        // If a buildable item:
        if (buildStep != null) {
            B1 resultBuilder = buildStep.get();
            I result = buildGroupedItem(resultBuilder);

            // callback
            Consumer<I> callback = childBuildItem.callback();
            if (callback != null) {
                callback.accept(result);
            }

            // runnableAfterCallback
            Runnable runnableAfterCallback = childBuildItem.runnableAfterCallback();
            if (runnableAfterCallback != null) {
                runnableAfterCallback.run();
            }

            // add the result to the resultList
            resultList.add(result);
        }
        // If note a buildable item:
        else if (childBuildItem.runnable() != null) {
            childBuildItem.runnable().run();
        }
    }
}
