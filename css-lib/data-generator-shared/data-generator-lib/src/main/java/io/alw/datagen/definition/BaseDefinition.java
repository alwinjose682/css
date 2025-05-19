package io.alw.datagen.definition;

import io.alw.datagen.TestDataGeneratable;

import java.util.*;
import java.util.function.UnaryOperator;

///  Note about `relatedTypeBuilders` and `relatedTypeBuildersWithInputVal`. Both are:
/// - Used to hold the function that produces an instance of type T
/// - Not thread-safe/concurrent-safe. Expected to be used only from a single thread
/// - Elements are added recursively(not concurrently) and hence ConcurrentModificationException will occur if these are iterated over using an Iterator.
/// To avoid this, the elements are simply popped/removed while recursively(not concurrently) being modified till its size becomes zero.
/// - An element when retrieved is popped/removed. The elements must be removed because the BaseDefinition instance is sometimes re-used(ex: by CashMessageDefinition) and therefore the old elements should not remain in the list
/// - Check the usage of [BaseDefinition#withRelatedType(UnaryOperator)] and [BaseDefinition#withRelatedType(UnaryOperator, TestDataGeneratable)] to understand how this is used
public abstract class BaseDefinition<T extends TestDataGeneratable> {
    private final Deque<UnaryOperator<T>> relatedTypeBuilders;
    private final Deque<RelatedTypeHolder<T>> relatedTypeBuildersWithInputVal;
    private int numOfChildDefinitions;
    protected final T parent;

    /// Set the parentType obtained by building this definition so that it becomes available to the related definitions during their build
    /// Values of parentType such as io.alw.css.domain.referencedata.Counterparty#entityCode are required during the build of related definitions
    protected BaseDefinition(T parent) {
        this.relatedTypeBuilders = new LinkedList<>();
        this.relatedTypeBuildersWithInputVal = new LinkedList<>();
        this.numOfChildDefinitions = 0;
        this.parent = parent;
    }

    public abstract BaseDefinition<T> withDefaults();

    public abstract T buildDefinition();

    protected abstract BaseDefinition<T> childDefinition(T parent);

    protected T parent() {
        return parent;
    }

    protected boolean isParentDefinition() {
        return parent == null;
    }

    /// This method can be called recursively
    public BaseDefinition<T> withRelatedType(UnaryOperator<T> relTypeBuilder) {
        relatedTypeBuilders.push(relTypeBuilder);
        return this;
    }

    /// This method can be called recursively and is indeed called recursively!
    public BaseDefinition<T> withRelatedType(UnaryOperator<T> relTypeBuilder, T t) {
        relatedTypeBuildersWithInputVal.push(new RelatedTypeHolder<>(relTypeBuilder, t));
        return this;
    }

    /// Note: Invoking this method is optional. If not invoked ZERO related definitions will be created
    public BaseDefinition<T> childDefinitions(int count) {
        this.numOfChildDefinitions = count;
        return this;
    }

    /// Builds the parent definition and its related definitions, if any.
    /// The first element in the list is ALWAYS the parent. Rest of the elements build results of related definitions.
    ///
    /// The related definitions can access the build output, T, during their builds.
    /// Values of parentType such as io.alw.css.domain.referencedata.Counterparty#entityCode are required during the build of related definitions
    ///
    /// Build creates the type based on the definition and creates one or more related types if [BaseDefinition#childDefinitions(int)]>0.
    /// Related definitions CAN have further related definitions.
    ///
    /// The user must ensure that all fields that need to be explicitly set are indeed set explicitly by invoking relevant methods of the type definition
    public List<T> buildWithChildDefinitions() {
        // 1. Build the definition
        final T parent = buildDefinition();
        List<T> result = new ArrayList<>();
        result.add(parent);

        // 2. Build child definitions if any
        for (int idx = 0; idx < numOfChildDefinitions; idx++) {
            List<T> relatedDefBuildResult = childDefinition(parent).buildWithChildDefinitions();
            result.addAll(relatedDefBuildResult);
        }

        return Collections.unmodifiableList(result);
    }

    /// TODO: Write documentation
    public List<T> buildWithRelatedDefinition() {
        // 1. Build the definition
        final T parent = buildDefinition();
        final List<T> result = new ArrayList<>();
        result.add(parent);

        // 2. Build all related types dependent on the above built definition
        while (!relatedTypeBuilders.isEmpty()) {
            UnaryOperator<T> relTypeBuilder = relatedTypeBuilders.pop(); // MUST remove the element from the list. Because the BaseDefinition is re-used(by CashMessageDefinition) and therefore the old elements should not remain in the list
            T related = relTypeBuilder.apply(parent);
            result.add(related);
        }

        // 3. Build all related types that have the values to be applied on the builder function
        while (!relatedTypeBuildersWithInputVal.isEmpty()) {
            RelatedTypeHolder<T> rth = relatedTypeBuildersWithInputVal.pop(); // MUST remove the element from the list. Because the BaseDefinition is re-used(by CashMessageDefinition) and therefore the old elements should not remain in the list
            T related = rth.relTypeBuilder().apply(rth.t());
            result.add(related);
        }

        return Collections.unmodifiableList(result);
    }
}
