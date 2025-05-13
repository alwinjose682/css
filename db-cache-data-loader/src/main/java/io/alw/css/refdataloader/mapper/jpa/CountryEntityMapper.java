package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.model.referencedata.CountryCache;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.CountryEntity;
import io.alw.css.domain.referencedata.Country;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CountryEntityMapper {
    static CountryEntityMapper instance() {
        return Mappers.getMapper(CountryEntityMapper.class);
    }

    CountryEntity domainToEntity(Country country);

    Country entityToDomain(CountryEntity countryEntity);

    CountryCache entityToCache(CountryEntity countryEntity);
}
