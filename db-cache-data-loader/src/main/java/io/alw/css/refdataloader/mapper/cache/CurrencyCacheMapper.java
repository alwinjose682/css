package io.alw.css.refdataloader.mapper.cache;

import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.domain.referencedata.Currency;
import io.alw.css.model.referencedata.CurrencyCache;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CurrencyCacheMapper {
    static CurrencyCacheMapper instance() {
        return Mappers.getMapper(CurrencyCacheMapper.class);
    }

    CurrencyCache domainToCache(Currency currency);

    Currency cacheToDomain(CurrencyCache currencyCache);
}
