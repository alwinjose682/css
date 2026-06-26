package io.alw.css.cashflowconsumer.repository.mapper;

import io.alw.css.cashflowconsumer.model.jpa.TradeLinkEntity;
import io.alw.css.serialization.trade.TradeLinkAvro;

public interface TradeLinkMapper {
    static TradeLinkEntity mapToEntity(TradeLinkAvro tl, long tradeId, int tradeVersion) {

    }
}
