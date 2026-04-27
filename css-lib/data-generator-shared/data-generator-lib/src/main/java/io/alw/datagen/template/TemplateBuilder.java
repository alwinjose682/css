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
public abstract class TemplateBuilder<T extends TestDataGeneratable> {
    private final Deque<UnaryOperator<T>> relTemplateBuilders;
    private final Deque<RelatedTemplateBuilder<T>> relTemplateBuildersWithOperand;
    private int numOfChildTemplates;
    protected final T parent;

    /// Set the parentType obtained by building this template so that it becomes available to the related templates during their build
    /// Values of parentType such as io.alw.css.domain.referencedata.Counterparty#entityCode are required during the build of related templates
    protected TemplateBuilder(T parent) {
        this.relTemplateBuilders = new LinkedList<>();
        this.relTemplateBuildersWithOperand = new LinkedList<>();
        this.numOfChildTemplates = 0;
        this.parent = parent;
    }

    public abstract TemplateBuilder<T> withDefaults();

    public abstract T buildTemplate();

    protected abstract TemplateBuilder<T> childTemplate(T parent);

    protected T parent() {
        return parent;
    }

    protected boolean isParentTemplate() {
        return parent == null;
    }

    /// This method can be called recursively
    public TemplateBuilder<T> withRelatedTemplate(UnaryOperator<T> relTemplateBuilder) {
        relTemplateBuilders.push(relTemplateBuilder);
        return this;
    }

    /// This method can be called recursively and is indeed called recursively by some of the child classes!
    public TemplateBuilder<T> withRelatedTemplate(UnaryOperator<T> builderFunc, T operand) {
        relTemplateBuildersWithOperand.push(new RelatedTemplateBuilder<>(builderFunc, operand));
        return this;
    }

    /// Note: Invoking this method is optional. If not invoked ZERO related templates will be created
    public TemplateBuilder<T> childTemplate(int count) {
        this.numOfChildTemplates = count;
        return this;
    }

    /// Builds the parent template and its related templates, if any.
    /// The first element in the list is ALWAYS the parent. Rest of the elements build results of related templates.
    ///
    /// The related templates can access the build output, T, during their builds.
    /// Values of parentType such as io.alw.css.domain.referencedata.Counterparty#entityCode are required during the build of related templates
    ///
    /// Build creates the type based on the template and creates one or more related types if [TemplateBuilder#childTemplate(int)]>0.
    /// Related templates CAN have further related templates.
    ///
    /// The user must ensure that all fields that need to be explicitly set are indeed set explicitly by invoking relevant methods
    public List<T> buildWithChildTemplates() {
        // 1. Build the template
        final T parent = buildTemplate();
        List<T> result = new ArrayList<>();
        result.add(parent);

        // 2. Build child templates if any
        for (int idx = 0; idx < numOfChildTemplates; idx++) {
            List<T> relatedDefBuildResult = childTemplate(parent).buildWithChildTemplates();
            result.addAll(relatedDefBuildResult);
        }

        return Collections.unmodifiableList(result);
    }

    /// TODO: Write documentation
    public List<T> buildWithRelatedTemplates() {
        // 1. Build the template
        final T parentTemplate = buildTemplate();
        final List<T> result = new ArrayList<>();
        result.add(parentTemplate);

        // 2. Build all related templates dependent on the above built template
        while (!relTemplateBuilders.isEmpty()) {
            UnaryOperator<T> builderFunc = relTemplateBuilders.pop(); // MUST remove the element from the list. Because the TemplateBuilder is re-used(by CashMessageTemplate) and therefore the old elements should not remain in the list
            T relTemplate = builderFunc.apply(parentTemplate);
            result.add(relTemplate);
        }

        // 3. Build all related templates that have the values to be applied on the builder function
        while (!relTemplateBuildersWithOperand.isEmpty()) {
            RelatedTemplateBuilder<T> builder = relTemplateBuildersWithOperand.pop(); // MUST remove the element from the list. Because the TemplateBuilder is re-used(by CashMessageTemplate) and therefore the old elements should not remain in the list
            T relTemplate = builder.builderFunc().apply(builder.operand());
            result.add(relTemplate);
        }

        return Collections.unmodifiableList(result);
    }
}
