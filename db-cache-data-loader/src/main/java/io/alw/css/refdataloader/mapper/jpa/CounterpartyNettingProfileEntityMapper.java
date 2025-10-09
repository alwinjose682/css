package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.model.referencedata.CounterpartyNettingProfileCache;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.CounterpartyNettingProfileEntity;
import io.alw.css.domain.referencedata.CounterpartyNettingProfile;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/// NOTE: The JPA entity classes of reference data have associations, ie; List or Set. But the domain model does not have the same. So mapping ignores them.
/// JPA entity classes have associations only to make it easier to persist the entity and all its associated entities in a single operation from the java layer. And this is the ONLY benefit/upside of using JPA.
@Mapper(uses = MapperUtil.class)
public interface CounterpartyNettingProfileEntityMapper {
    static CounterpartyNettingProfileEntityMapper instance() {
        return Mappers.getMapper(CounterpartyNettingProfileEntityMapper.class);
    }

    @Mapping(source = ".", target = "counterpartyNettingProfileEntityPK")
    @Mapping(target = "counterparty", ignore = true)
    CounterpartyNettingProfileEntity domainToEntity(CounterpartyNettingProfile counterpartyNettingProfile);

    @InheritInverseConfiguration
    CounterpartyNettingProfile entityToDomain(CounterpartyNettingProfileEntity counterpartyNettingProfileEntity);

    @Mapping(source = "counterpartyNettingProfileEntityPK", target = ".")
    CounterpartyNettingProfileCache entityToCache(CounterpartyNettingProfileEntity counterpartyNettingProfileEntity);
}
