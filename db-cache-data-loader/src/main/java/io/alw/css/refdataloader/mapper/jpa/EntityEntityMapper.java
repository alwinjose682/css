package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.model.referencedata.EntityCache;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.EntityEntity;
import io.alw.css.domain.referencedata.Entity;
import io.alw.css.refdataloader.model.jpa.ReferenceDataJpaEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface EntityEntityMapper {
    static EntityEntityMapper instance() {
        return Mappers.getMapper(EntityEntityMapper.class);
    }

    @Mapping(source = ".", target = "entityEntityPK")
    @Mapping(target = "nostros", ignore = true)
    @Mapping(target = "books", ignore = true)
    EntityEntity domainToEntity(Entity entity);

    @InheritInverseConfiguration
    Entity entityToDomain(EntityEntity entityEntity);

    @Mapping(target = ".", source = "entityEntityPK")
    EntityCache entityToCache(EntityEntity entityEntity);
}
