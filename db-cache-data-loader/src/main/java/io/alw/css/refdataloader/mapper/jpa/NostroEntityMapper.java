package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.model.referencedata.NostroCache;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.NostroEntity;
import io.alw.css.domain.referencedata.Nostro;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface NostroEntityMapper {
    static NostroEntityMapper instance() {
        return Mappers.getMapper(NostroEntityMapper.class);
    }

    @Mapping(source = ".", target = "nostroEntityPK")
    @Mapping(target = "entity", ignore = true)
    NostroEntity domainToEntity(Nostro nostro);

    @InheritInverseConfiguration
    Nostro entityToDomain(NostroEntity nostroEntity);

    @Mapping(target = ".", source = "nostroEntityPK")
    NostroCache entityToCache(NostroEntity nostroEntity);
}
