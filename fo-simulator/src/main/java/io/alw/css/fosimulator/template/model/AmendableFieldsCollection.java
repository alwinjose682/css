package io.alw.css.fosimulator.template.model;

import io.alw.css.domain.trade.TradeLegType;

import java.util.*;

public final class AmendableFieldsCollection {
    private final Set<AmendableField> amendableFieldsForTrade;
    private final Map<TradeLegType, Set<AmendableField>> amendableFieldsForTradeLeg;

    public AmendableFieldsCollection() {
        amendableFieldsForTradeLeg = new HashMap<>();
        amendableFieldsForTrade = new HashSet<>();
    }

    public AmendableFieldsCollection addForTrade(AmendableField cpCode) {
        amendableFieldsForTrade.add(cpCode);
        return this;
    }

    public AmendableFieldsCollection addForTradeLeg(TradeLegType tradeLegType, AmendableField field) {
        amendableFieldsForTradeLeg.computeIfAbsent(tradeLegType, _ -> new HashSet<>()).add(field);
        return this;
    }

    public AmendableFieldsCollection addForTradeLeg(TradeLegType tradeLegType, AmendableFieldSupplier supplier) {
        amendableFieldsForTradeLeg.computeIfAbsent(tradeLegType, _ -> new HashSet<>()).add(supplier);
        return this;
    }

    public Set<AmendableField> getForTrade() {
        return amendableFieldsForTrade;
    }

    public Set<AmendableField> getForTradeLeg(TradeLegType type) {
        var fields = amendableFieldsForTradeLeg.get(type);
        return fields == null ? null : Collections.unmodifiableSet(fields);
    }
}
