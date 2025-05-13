package io.alw.css.refdataloader.mapper.cache;

import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.domain.referencedata.Entity;
import io.alw.css.model.referencedata.EntityCache;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface EntityCacheMapper {
    static EntityCacheMapper instance(){
        return Mappers.getMapper(EntityCacheMapper.class);
    }

    EntityCache domainToCache(Entity entity);

    Entity cacheToDomain(EntityCache entityCache);
}
