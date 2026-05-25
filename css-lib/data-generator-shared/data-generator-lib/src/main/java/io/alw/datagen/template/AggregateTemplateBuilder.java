package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class AggregateTemplateBuilder<T extends TestDataGeneratable> extends TemplateBuilder<T> {
    private final Deque<UnaryOperator<T>> relTemplateBuilders;
    private final Deque<RelatedTemplateBuilder<T>> relTemplateBuildersWithOperand;

    protected AggregateTemplateBuilder(T parent) {
        super(parent);
        this.relTemplateBuilders = new LinkedList<>();
        this.relTemplateBuildersWithOperand = new LinkedList<>();
    }

    /// This method can be called recursively
    public TemplateBuilder<T> withRelatedObjectBuilder(UnaryOperator<T> relTemplateBuilder) {
        relTemplateBuilders.push(relTemplateBuilder);
        return this;
    }

    /// This method can be called recursively and is indeed called recursively by some of the child classes!
    public TemplateBuilder<T> withRelatedObjectBuilder(UnaryOperator<T> builderFunc, T operand) {
        relTemplateBuildersWithOperand.push(new RelatedTemplateBuilder<>(builderFunc, operand));
        return this;
    }

    /// Builds the parent template and then its related templates, if any.
    /// The related templates can access the build output, T, during their builds.
    @Override
    public List<T> build() {
        // 1. Build the template
        final T parent = finalBuildInstruction();
        final List<T> result = new ArrayList<>();
        result.add(parent);

        // 2. Build all related templates dependent on the above built template
        while (!relTemplateBuilders.isEmpty()) {
            UnaryOperator<T> builderFunc = relTemplateBuilders.pop(); // MUST remove the element from the list. Because the TemplateBuilder is re-used(by CashMessageTemplate) and therefore the old elements should not remain in the list
            T related = builderFunc.apply(parent);
            result.add(related);
        }

        // 3. Build all related templates that have the values to be applied on the builder function
        while (!relTemplateBuildersWithOperand.isEmpty()) {
            RelatedTemplateBuilder<T> builder = relTemplateBuildersWithOperand.pop(); // MUST remove the element from the list. Because the TemplateBuilder is re-used(by CashMessageTemplate) and therefore the old elements should not remain in the list
            T related = builder.builderFunc().apply(builder.operand());
            result.add(related);
        }

        return Collections.unmodifiableList(result);
    }
}
