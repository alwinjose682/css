package io.alw.css.refdataloader.mapper.cache;

import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.domain.referencedata.Country;
import io.alw.css.model.referencedata.CountryCache;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CountryCacheMapper {
    static CountryCacheMapper instance() {
        return Mappers.getMapper(CountryCacheMapper.class);
    }

    CountryCache domainToCache(Country country);

    Country cacheToDomain(CountryCache countryCache);
}
