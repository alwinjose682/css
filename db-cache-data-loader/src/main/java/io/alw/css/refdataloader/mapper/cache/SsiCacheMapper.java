package io.alw.css.refdataloader.mapper.cache;

import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.domain.referencedata.Ssi;
import io.alw.css.model.referencedata.SsiCache;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface SsiCacheMapper {
    static SsiCacheMapper instance() {
        return Mappers.getMapper(SsiCacheMapper.class);
    }

    SsiCache domainToCache(Ssi ssi);

    Ssi cacheToDomain(SsiCache ssiCache);
}
