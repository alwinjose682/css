package io.alw.css.tradepublisher.store;

import io.alw.css.confirmation.ContextualId;
import io.alw.css.tradepublisher.generator.DayTicker;

import java.util.random.RandomGenerator;

public final class ExtendedStoreHelper<T extends ContextualId> extends StoreHelper<T> {

    public ExtendedStoreHelper(DayTicker dayTicker, Store<T> store, RandomGenerator rndm) {
        super(dayTicker, store, rndm);
    }

    public boolean removeById(long id, Purpose purpose){
        return ((ExtendedInMemoryStore<T>)store).removeById(id, purpose.storeIdx);
    }

}
