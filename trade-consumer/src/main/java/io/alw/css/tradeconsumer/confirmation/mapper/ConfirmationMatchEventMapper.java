package io.alw.css.tradeconsumer.confirmation.mapper;

import io.alw.css.confirmation.ConfirmationMatchEvent;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConfirmationMatchEventMapper {
    static ConfirmationMatchEventMapper instance() {
        return Mappers.getMapper(ConfirmationMatchEventMapper.class);
    }

    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    ConfirmationMatchStatusAvro domainToAvro(ConfirmationMatchEvent domain);

    @InheritInverseConfiguration
    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    ConfirmationMatchEvent avroToDomain(ConfirmationMatchStatusAvro avro);
}
