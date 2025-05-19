package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.model.referencedata.CounterpartyCache;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.CounterpartyEntity;
import io.alw.css.domain.referencedata.Counterparty;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/// NOTE: The JPA entity classes of reference data have associations, ie; List or Set. But the domain model does not have the same. So mapping ignores them.
/// JPA entity classes have associations only to make it easier to persist the entity and all its associated entities in a single operation from the java layer. And this is the ONLY benefit/upside of using JPA.
@Mapper(uses = MapperUtil.class)
public interface CounterpartyEntityMapper {
    static CounterpartyEntityMapper instance() {
        return Mappers.getMapper(CounterpartyEntityMapper.class);
    }

    @Mapping(source = ".", target = "counterpartyEntityPK")
//    @Mapping(source = "parent", target = "parent") //, qualifiedByName = "booleanToYesNo")
//    @Mapping(source = "internal", target = "internal") //, qualifiedByName = "booleanToYesNo")
//    @Mapping(source = "active", target = "active") //, qualifiedByName = "booleanToYesNo")
    @Mapping(target = "ssiList", ignore = true)
    @Mapping(target = "cpNettingProfiles", ignore = true)
    @Mapping(target = "slaMappingEntities", ignore = true)
    CounterpartyEntity domainToEntity(Counterparty counterparty);

    @InheritInverseConfiguration
    Counterparty entityToDomain(CounterpartyEntity counterpartyEntity);

    @Mapping(source = "counterpartyEntityPK", target = ".")
    CounterpartyCache entityToCache(CounterpartyEntity counterpartyEntity);
}
