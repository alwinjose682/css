package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AggregateTemplateBuilder<T extends DataGeneratable, U extends DataGeneratable, TB, UB> extends TemplateBuilder<T, AggregateTemplateBuilderResult<T>> {
    private final Deque<ChildBuildDirective<U, UB>> childTemplateDirectives;
    private final Deque<ParentBuildDirective<T, U, TB, UB>> relatedDirectives;

    protected AggregateTemplateBuilder(T parent) {
        super(parent);
        this.childTemplateDirectives = new LinkedList<>();
        this.relatedDirectives = new LinkedList<>();
    }

    protected abstract T buildParentTemplate();

    protected abstract T buildRelatedParentTemplate(TB parentBuilder);

    protected abstract U buildChildTemplate(UB builder);

    protected abstract U buildRelatedChildTemplate(UB builder);

    /// Returns result and child association function for the template, not for related template
    protected abstract BiConsumer<T, Set<U>> parentAndChildAssociationFunc();

    @Override
    protected T buildRootTemplate() {
        return buildParentTemplate();
    }

    /// This method can be called recursively
    ///
    /// NOTE: The items inserted in the queue will be removed in the same order as they are inserted
    public AggregateTemplateBuilder<T, U, TB, UB> withChildTemplateDirective(Consumer<U> callback, Supplier<UB> buildStep) {
        childTemplateDirectives.addFirst(new ChildBuildDirective<>(callback, buildStep));
        return this;
    }

    public AggregateTemplateBuilder<T, U, TB, UB> withRelatedTemplateDirective(ParentBuildDirective<T, U, TB, UB> parentBuildDirective) {
        relatedDirectives.add(parentBuildDirective);
        return this;
    }

    /// Builds the template(result+child) and then its related objects(result+child), if any.
    ///
    /// NOTE: Each 'T' is expected to be unique. Hence, they are added in a [Set]
    ///
    /// NOTE: The grouped and related items will be removed(retrieved for build) in the same order as they were inserted
    ///
    /// see also {@link AggregateTemplateBuilder#buildChildTemplate(ChildBuildDirective, boolean)}}
    @Override
    public AggregateTemplateBuilderResult<T> build() {
        // 1. Build the result template
        final T parentTemplate = buildRootTemplate();

        // 2. Build the child templates that need to be grouped together with result/result template
        final Set<U> childItems = new HashSet<>();
        while (!childTemplateDirectives.isEmpty()) {
            ChildBuildDirective<U, UB> childDirective = childTemplateDirectives.removeLast();
            childItems.add(buildChildTemplate(childDirective, false));
        }

        // 3. Associate the result and child templates
        parentAndChildAssociationFunc().accept(parentTemplate, childItems);

        // 4. Build related templates that do NOT need to be grouped together with result/result template
        final Set<T> relatedTemplates = new HashSet<>();
        while (!relatedDirectives.isEmpty()) {
            ParentBuildDirective<T, U, TB, UB> parentBuildDirective = relatedDirectives.removeLast();
            relatedTemplates.add(buildRelatedTemplate(parentBuildDirective));
        }

        return new AggregateTemplateBuilderResult<>(parentTemplate, relatedTemplates);
    }

    /// Builds the related result and its multiple child items
    private T buildRelatedTemplate(ParentBuildDirective<T, U, TB, UB> parentBuildDirective) {
        Supplier<TB> parentBuilderFunc = parentBuildDirective.parentBuilderFunc();
        T relatedParentItem = buildRelatedParentTemplate(parentBuilderFunc.get());
        Runnable finalExecutableAction = parentBuildDirective.finalExecutableAction();

        // The related result and child association function
        BiFunction<T, Set<U>, T> associationFunc = parentBuildDirective.parentAndChildAssociationFunc();

        // Execute the specific build directive to obtain the final result(the fully built related result item)
        var buildResult = switch (parentBuildDirective) {
            case ParentBuildDirective.ParentBuildDirectiveType1<T, U, TB, UB> dir -> {
                Set<U> childItems = dir
                        .childDirectives()
                        .stream().map(childDir -> buildChildTemplate(childDir, true))
                        .collect(Collectors.toSet());

                yield associationFunc.apply(relatedParentItem, childItems);
            }
            case ParentBuildDirective.ParentBuildDirectiveType2<T, U, TB, UB> dir -> {
                Set<U> childItems = dir
                        .childBuildersSupplier().get()
                        .stream()
                        .map(this::buildRelatedChildTemplate)
                        .collect(Collectors.toSet());

                yield associationFunc.apply(relatedParentItem, childItems);
            }
            case ParentBuildDirective.ParentBuildDirectiveType3<T, U, TB, UB> dir -> {
                Set<U> childItems = dir
                        .childDirectivesSupplier().get()
                        .stream().map(childDir -> buildChildTemplate(childDir, true))
                        .collect(Collectors.toSet());

                dir.callback().accept(relatedParentItem);
                yield associationFunc.apply(relatedParentItem, childItems);
            }
        };

        // Execute finalAction if any
        if (finalExecutableAction != null) {
            finalExecutableAction.run();
        }

        return buildResult;
    }

    /// If the item is a buildable item:
    /// 1) build the item
    /// 2) execute callback if any
    /// 3) execute the finalAction if any and
    /// 4) add the buildResult to the resultList
    ///
    /// If the item is just a runnable, just execute the runnable. There is no build result in this case.
    ///
    /// NOTE: The buildItem cannot be both a runnable and buildable item. It is ensured so
    private U buildChildTemplate(ChildBuildDirective<U, UB> childDirective, boolean relatedChildItem) {
        // Build child item
        UB builder = childDirective.buildStep().get();
        U result = relatedChildItem ? buildRelatedChildTemplate(builder) : buildChildTemplate(builder);

        // Execute callback if any
        Consumer<U> callback = childDirective.callback();
        if (callback != null) {
            callback.accept(result);
        }

        return result;
    }
}
