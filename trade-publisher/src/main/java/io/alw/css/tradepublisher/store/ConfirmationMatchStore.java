package io.alw.css.tradepublisher.store;

import io.alw.css.tradepublisher.generator.DayTicker;

import java.util.random.RandomGenerator;

public final class ConfirmationMatchStore<T> extends StoreHelper<T> {

    public ConfirmationMatchStore(DayTicker dayTicker, Store<T> store, RandomGenerator rndm) {
        super(dayTicker, store, rndm);
    }



}
