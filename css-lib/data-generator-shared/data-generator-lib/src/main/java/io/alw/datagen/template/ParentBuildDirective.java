package io.alw.datagen.template;

import io.alw.datagen.DataGeneratable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed interface ParentBuildDirective<T extends DataGeneratable, U extends DataGeneratable, TB, UB> {
    Supplier<TB> parentBuilderFunc();

    List<ChildBuildDirective<U, UB>> adhocChildDirectives();

    BiFunction<T, Set<U>, T> parentAndChildAssociationFunc();

    Runnable finalExecutableAction();

    record ParentBuildDirectiveType1<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            List<ChildBuildDirective<U, UB>> childDirectives,
            List<ChildBuildDirective<U, UB>> adhocChildDirectives,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc,
            Runnable finalExecutableAction) implements ParentBuildDirective<T, U, TB, UB> {
        public ParentBuildDirectiveType1(Supplier<TB> parentBuilderFunc, List<ChildBuildDirective<U, UB>> childDirectives, BiFunction<T, Set<U>, T> parentAndChildAssociationFunc, Runnable finalExecutableAction) {
            this(parentBuilderFunc, childDirectives, new ArrayList<>(), parentAndChildAssociationFunc, finalExecutableAction);
        }
    }

    record ParentBuildDirectiveType2<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            Supplier<List<UB>> childBuildersSupplier,
            List<ChildBuildDirective<U, UB>> adhocChildDirectives,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc,
            Runnable finalExecutableAction) implements ParentBuildDirective<T, U, TB, UB> {
        public ParentBuildDirectiveType2(Supplier<TB> parentBuilderFunc, Supplier<List<UB>> childBuildersSupplier, BiFunction<T, Set<U>, T> parentAndChildAssociationFunc, Runnable finalExecutableAction) {
            this(parentBuilderFunc, childBuildersSupplier, new ArrayList<>(), parentAndChildAssociationFunc, finalExecutableAction);
        }
    }

    record ParentBuildDirectiveType3<T extends DataGeneratable, U extends DataGeneratable, TB, UB>(
            Supplier<TB> parentBuilderFunc,
            Supplier<List<ChildBuildDirective<U, UB>>> childDirectivesSupplier,
            List<ChildBuildDirective<U, UB>> adhocChildDirectives,
            BiFunction<T, Set<U>, T> parentAndChildAssociationFunc,
            Consumer<T> callback,
            Runnable finalExecutableAction) implements ParentBuildDirective<T, U, TB, UB> {
        public ParentBuildDirectiveType3(Supplier<TB> parentBuilderFunc, Supplier<List<ChildBuildDirective<U, UB>>> childDirectivesSupplier, BiFunction<T, Set<U>, T> parentAndChildAssociationFunc, Consumer<T> callback, Runnable finalExecutableAction) {
            this(parentBuilderFunc, childDirectivesSupplier, new ArrayList<>(), parentAndChildAssociationFunc, callback, finalExecutableAction);
        }
    }
}
