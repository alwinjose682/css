package io.alw.css.refdataloader.mapper.cache;

import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.domain.referencedata.CounterpartySlaMapping;
import io.alw.css.model.referencedata.CounterpartySlaMappingCache;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CounterpartySlaMappingCacheMapper {
    static CounterpartySlaMappingCacheMapper instance() {
        return Mappers.getMapper(CounterpartySlaMappingCacheMapper.class);
    }

    CounterpartySlaMappingCache domainToCache(CounterpartySlaMapping counterpartySlaMapping);

    CounterpartySlaMapping cacheToDomain(CounterpartySlaMappingCache counterpartySlaMappingCache);
}
