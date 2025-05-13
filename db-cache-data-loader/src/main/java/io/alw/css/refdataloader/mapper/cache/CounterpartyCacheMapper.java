package io.alw.css.refdataloader.mapper.cache;

import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.domain.referencedata.Counterparty;
import io.alw.css.model.referencedata.CounterpartyCache;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CounterpartyCacheMapper {
    static CounterpartyCacheMapper mapper() {
        return Mappers.getMapper(CounterpartyCacheMapper.class);
    }

    CounterpartyCache domainToCache(Counterparty counterparty);

    Counterparty cacheToDomain(CounterpartyCache counterpartyCache);
}
