package io.alw.datagen.template;

import io.alw.datagen.TestDataGeneratable;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed interface ParentBuildItem<T extends TestDataGeneratable, U extends TestDataGeneratable, TB> {
    Supplier<TB> parentBuilderFunc();

    BiFunction<T, Set<U>, T> parentAndChildAssociationFunc();

    record ParentBuildItemType1<T extends TestDataGeneratable, U extends TestDataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            List<ChildBuildItem<U, UB>> childBuildItems,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc) implements ParentBuildItem<T, U, TB> {
    }

    record ParentBuildItemType2<T extends TestDataGeneratable, U extends TestDataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            Supplier<List<UB>> childBuildersSupplier,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc) implements ParentBuildItem<T, U, TB> {
    }

    record ParentBuildItemType3<T extends TestDataGeneratable, U extends TestDataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            Supplier<List<ChildBuildItem<U, UB>>> childBuildItems,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc,
            Consumer<T> parentConsumer) implements ParentBuildItem<T, U, TB> {
    }
}
