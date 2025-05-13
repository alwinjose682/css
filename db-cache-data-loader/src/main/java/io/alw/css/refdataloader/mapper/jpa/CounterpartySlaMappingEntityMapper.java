package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.model.referencedata.CounterpartySlaMappingCache;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.CounterpartySlaMappingEntity;
import io.alw.css.domain.referencedata.CounterpartySlaMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CounterpartySlaMappingEntityMapper {
    static CounterpartySlaMappingEntityMapper instance() {
        return Mappers.getMapper(CounterpartySlaMappingEntityMapper.class);
    }

    @Mapping(source = ".", target = "counterpartySlaMappingEntityPK")
    @Mapping(target = "counterparty", ignore = true)
    CounterpartySlaMappingEntity domainToEntity(CounterpartySlaMapping counterpartySlaMapping);

    @InheritInverseConfiguration
    CounterpartySlaMapping entityToDomain(CounterpartySlaMappingEntity counterpartySlaMappingEntity);

    @Mapping(target = ".", source = "counterpartySlaMappingEntityPK")
    CounterpartySlaMappingCache entityToCache(CounterpartySlaMappingEntity counterpartySlaMappingEntity);
}
