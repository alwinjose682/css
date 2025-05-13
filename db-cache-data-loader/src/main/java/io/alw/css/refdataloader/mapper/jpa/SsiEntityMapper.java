package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.model.referencedata.SsiCache;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.SsiEntity;
import io.alw.css.domain.referencedata.Ssi;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface SsiEntityMapper {
    static SsiEntityMapper instance() {
        return Mappers.getMapper(SsiEntityMapper.class);
    }

    @Mapping(source = ".", target = "ssiEntityPK")
    @Mapping(target = "counterparty", ignore = true)
    SsiEntity domainToEntity(Ssi ssi);

    @InheritInverseConfiguration
    Ssi entityToDomain(SsiEntity ssiEntity);

    @Mapping(target = ".", source = "ssiEntityPK")
    SsiCache entityToCache(SsiEntity ssiEntity);
}
