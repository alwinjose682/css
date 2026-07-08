package io.alw.css.tradepublisher.confirmation.mapper;

import io.alw.css.confirmation.MatchStatusEvent;
import io.alw.css.serialization.confirmation.MatchStatusEventAvro;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MatchStatusEventMapper {
    static MatchStatusEventMapper instance() {
        return Mappers.getMapper(MatchStatusEventMapper.class);
    }

    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    MatchStatusEventAvro domainToAvro(MatchStatusEvent matchStatusEvent);

    @InheritInverseConfiguration
    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    MatchStatusEvent avroToDomain(MatchStatusEventAvro matchStatusEventAvro);
}
