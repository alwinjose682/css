package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed interface ParentBuildDirective<T extends DataGeneratable, U extends DataGeneratable, TB> {
    Supplier<TB> parentBuilderFunc();

    BiFunction<T, Set<U>, T> parentAndChildAssociationFunc();

    Runnable finalExecutableAction();

    record ParentBuildDirectiveType1<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            List<ChildBuildDirective<U, UB>> childDirectives,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc,
            Runnable finalExecutableAction) implements ParentBuildDirective<T, U, TB> {
    }

    record ParentBuildDirectiveType2<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            Supplier<List<UB>> childBuildersSupplier,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc,
            Runnable finalExecutableAction) implements ParentBuildDirective<T, U, TB> {
    }

    record ParentBuildDirectiveType3<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            Supplier<List<ChildBuildDirective<U, UB>>> childDirectivesSupplier,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc,
            Consumer<T> callback,
            Runnable finalExecutableAction) implements ParentBuildDirective<T, U, TB> {
    }
}
