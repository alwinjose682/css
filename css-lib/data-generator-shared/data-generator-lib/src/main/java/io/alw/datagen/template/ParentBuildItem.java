package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed interface ParentBuildItem<T extends DataGeneratable, U extends DataGeneratable, TB> {
    Supplier<TB> parentBuilderFunc();

    BiFunction<T, Set<U>, T> parentAndChildAssociationFunc();

    record ParentBuildItemType1<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            List<ChildBuildItem<U, UB>> childBuildItems,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc) implements ParentBuildItem<T, U, TB> {
    }

    record ParentBuildItemType2<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            Supplier<List<UB>> childBuildersSupplier,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc) implements ParentBuildItem<T, U, TB> {
    }

    record ParentBuildItemType3<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            Supplier<List<ChildBuildItem<U, UB>>> childBuildItems,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc,
            Consumer<T> parentConsumer) implements ParentBuildItem<T, U, TB> {
    }
}
