package io.alw.css.refdataloader.mapper.jpa;

import io.alw.css.refdataloader.domain.DataLoadStatus;
import io.alw.css.refdataloader.mapper.MapperUtil;
import io.alw.css.refdataloader.model.jpa.DataLoadStatusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface DataLoadStatusEntityMapper {
    static DataLoadStatusEntityMapper instance() {
        return Mappers.getMapper(DataLoadStatusEntityMapper.class);
    }

    @Mapping(target = "id", ignore = true)
    DataLoadStatusEntity domainToEntity(DataLoadStatus dataLoadStatus);

    DataLoadStatus entityToDomain(DataLoadStatusEntity dataLoadStatusEntity);
}
