package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public abstract class AggregateTemplateBuilder<T extends DataGeneratable, U extends DataGeneratable, TB, UB> extends TemplateBuilder<T, AggregateTemplateBuilderResult<T>> {
    private final Deque<ChildBuildDirective<U, UB, ?>> childTemplateDirectives;
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

    /// Returns parent and child association function for the template, not for related template
    protected abstract BiConsumer<T, Set<U>> parentAndChildAssociationFunc();

    @Override
    protected T buildRootTemplate() {
        return buildParentTemplate();
    }

    /// This method can be called recursively
    ///
    /// NOTE: The items inserted in the queue will be removed in the same order as they are inserted
    public AggregateTemplateBuilder<T, U, TB, UB> withChildTemplateDirective(Consumer<U> callback, Supplier<UB> buildStep) {
        childTemplateDirectives.addFirst(new ChildBuildDirective.ChildBuildDirectiveType1<>(callback, buildStep));
        return this;
    }

    public AggregateTemplateBuilder<T, U, TB, UB> withChildTemplateDirective(ChildBuildDirective<U, UB, ?> directive) {
        childTemplateDirectives.addFirst(directive);
        return this;
    }

    /// This method can be called recursively
    ///
    /// NOTE: The items inserted in the queue will be removed in the same order as they are inserted
    public AggregateTemplateBuilder<T, U, TB, UB> withChildTemplateDirective(Runnable runnable) {
        childTemplateDirectives.addFirst(new ChildBuildDirective.ChildBuildDirectiveType2<>(runnable));
        return this;
    }

    public AggregateTemplateBuilder<T, U, TB, UB> withRelatedTemplateDirective(ParentBuildDirective<T, U, TB, UB> parentBuildDirective) {
        relatedDirectives.add(parentBuildDirective);
        return this;
    }

    /// Builds the template(parent+child) and then its related objects(parent+child), if any.
    ///
    /// NOTE: Each 'T' is expected to be unique. Hence, they are added in a [Set]
    ///
    /// NOTE: The grouped and related items will be removed(retrieved for build) in the same order as they were inserted
    ///
    /// see also {@link AggregateTemplateBuilder#buildChildTemplate(io.alw.datagen.template.ChildBuildDirective.ChildBuildDirectiveType1, boolean)}}
    @Override
    public AggregateTemplateBuilderResult<T> build() {
        // 1. Build the parent template
        final T parentTemplate = buildRootTemplate();

        // 2. Build the child templates that need to be grouped together with parent/root template
        final Set<U> childItems = new HashSet<>();
        while (!childTemplateDirectives.isEmpty()) {
            ChildBuildDirective<U, UB, ?> childDirective = childTemplateDirectives.removeLast();
            // Build the child directive
            switch (childDirective) {
                case ChildBuildDirective.ChildBuildDirectiveType1<U, UB, ?> dir -> {
                    childItems.add(buildChildTemplate(dir, false));
                }
                case ChildBuildDirective.ChildBuildDirectiveType2<U, UB, ?> dir -> {
                    dir.runnable().run();
                }
                case ChildBuildDirective.ChildBuildDirectiveType3<U, UB, ?> dir -> {
                    childItems.add(buildChildTemplate(dir, false));
                }
            }
        }

        // 3. Associate the parent and child templates
        parentAndChildAssociationFunc().accept(parentTemplate, childItems);

        // 4. Build related templates that do NOT need to be grouped together with parent/root template
        final List<T> relatedTemplates = new ArrayList<>();
        while (!relatedDirectives.isEmpty()) {
            ParentBuildDirective<T, U, TB, UB> parentBuildDirective = relatedDirectives.removeLast();
            T t = buildRelatedTemplate(parentBuildDirective);
            relatedTemplates.add(t);
        }

        return new AggregateTemplateBuilderResult<>(parentTemplate, relatedTemplates);
    }

    /// Builds the related parent and its multiple child items
    private T buildRelatedTemplate(ParentBuildDirective<T, U, TB, UB> parentBuildDirective) {
        Supplier<TB> parentBuilderFunc = parentBuildDirective.parentBuilderFunc();
        T relatedParentItem = buildRelatedParentTemplate(parentBuilderFunc.get());
        Runnable finalExecutableAction = parentBuildDirective.finalExecutableAction();

        // The related parent and child association function
        BiFunction<T, Set<U>, T> associationFunc = parentBuildDirective.parentAndChildAssociationFunc();

        // Execute the specific build directive to obtain the final result(the fully built related parent item)
        var buildResult = switch (parentBuildDirective) {
            case ParentBuildDirective.ParentBuildDirectiveType1<T, U, TB, UB> dir -> {
                Set<U> childItems = buildChildTemplate(dir.childDirectives(), true);
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
                Set<U> childItems = buildChildTemplate(dir.childDirectivesSupplier().get(), true);
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

    private Set<U> buildChildTemplate(List<ChildBuildDirective<U, UB, ?>> childDirectives, boolean relatedChildItem) {
        Set<U> childItems = new HashSet<>();
        for (ChildBuildDirective<U, UB, ?> childDir : childDirectives) {
            switch (childDir) {
                case ChildBuildDirective.ChildBuildDirectiveType1<U, UB, ?> dir -> childItems.add(buildChildTemplate(dir, relatedChildItem));
                case ChildBuildDirective.ChildBuildDirectiveType2<U, UB, ?> dir -> dir.runnable().run();
                case ChildBuildDirective.ChildBuildDirectiveType3<U, UB, ?> dir -> childItems.add(buildChildTemplate(dir, relatedChildItem));
            }
        }

        return Collections.unmodifiableSet(childItems);
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
    private U buildChildTemplate(ChildBuildDirective.ChildBuildDirectiveType1<U, UB, ?> childDirective, boolean relatedChildItem) {
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

    private <P> U buildChildTemplate(ChildBuildDirective.ChildBuildDirectiveType3<U, UB, P> childDirective, boolean relatedChildItem) {
        // Build child item
        P buildStepParam = childDirective.buildStepParamSupplier().get();
        Function<P, UB> buildStep = childDirective.buildStep();
        UB builder = buildStep.apply(buildStepParam);
        U result = relatedChildItem ? buildRelatedChildTemplate(builder) : buildChildTemplate(builder);

        // Execute callback if any
        BiConsumer<P, U> callback = childDirective.callback();
        if (callback != null) {
            callback.accept(buildStepParam, result);
        }

        return result;
    }
}
