package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.model.referencedata.CurrencyCache;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.CurrencyEntity;
import io.alw.css.domain.referencedata.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CurrencyEntityMapper {
    static CurrencyEntityMapper instance() {
        return Mappers.getMapper(CurrencyEntityMapper.class);
    }

    CurrencyEntity domainToEntity(Currency currency);

    Currency entityToDomain(CurrencyEntity currencyEntity);

    CurrencyCache entityToCache(CurrencyEntity currencyEntity);
}
