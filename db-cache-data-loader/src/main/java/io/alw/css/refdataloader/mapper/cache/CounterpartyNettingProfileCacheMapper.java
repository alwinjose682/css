package io.alw.css.refdataloader.mapper.cache;

import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.domain.referencedata.CounterpartyNettingProfile;
import io.alw.css.model.referencedata.CounterpartyNettingProfileCache;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CounterpartyNettingProfileCacheMapper {
    static CounterpartyNettingProfileCacheMapper instance(){
        return Mappers.getMapper(CounterpartyNettingProfileCacheMapper.class);
    }

    CounterpartyNettingProfileCache domainToCache(CounterpartyNettingProfile counterpartyNettingProfile);

    CounterpartyNettingProfile cacheToDomain(CounterpartyNettingProfileCache counterpartyNettingProfileCache);
}
