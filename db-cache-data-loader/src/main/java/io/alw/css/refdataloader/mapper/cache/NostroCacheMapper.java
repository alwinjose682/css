package io.alw.css.refdataloader.mapper.cache;

import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.domain.referencedata.Nostro;
import io.alw.css.model.referencedata.NostroCache;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface NostroCacheMapper {
    static NostroCacheMapper instance() {
        return Mappers.getMapper(NostroCacheMapper.class);
    }

    NostroCache domainToCache(Nostro nostro);

    Nostro cacheToDomain(NostroCache nostroCache);
}
