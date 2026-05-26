package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.*;
import java.util.function.UnaryOperator;

///  Note about `relatedTypeBuilders` and `relatedTypeBuildersWithInputVal`. Both are:
/// - Used to hold the function that produces an instance of type T
/// - Not thread-safe/concurrent-safe. Expected to be used only from a single thread
/// - Elements are added recursively(not concurrently) and hence ConcurrentModificationException will occur if these are iterated over using an Iterator.
/// To avoid this, the elements are simply popped/removed while recursively(not concurrently) being modified till its size becomes zero.
/// - An element when retrieved is popped/removed. The elements must be removed because the TemplateBuilder instance is sometimes re-used(ex: by CashMessageTemplate) and therefore the old elements should not remain in the list
/// - Check the usage of [TemplateBuilder#withRelatedTemplate(UnaryOperator)] and [TemplateBuilder#withRelatedType(UnaryOperator, TestDataGeneratable)] to understand how this is used
public abstract class TemplateBuilder<T extends TestDataGeneratable, R> {
    protected final T parent;

    /// Set the parentType obtained by building this template so that it becomes available to the related templates during their build
    /// Values of parentType such as io.alw.css.domain.referencedata.Counterparty#entityCode are required during the build of related templates
    protected TemplateBuilder(T parent) {
        this.parent = parent;
    }

    public abstract TemplateBuilder<T, R> withTemplateValues();

    public abstract R build();

    protected abstract T finalBuildInstruction();

    protected T parent() {
        return parent;
    }

    protected boolean isParentTemplate() {
        return parent == null;
    }

}
