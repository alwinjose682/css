package io.alw.css.tradeconsumer.confirmation.mapper;

import io.alw.css.confirmation.ConfirmationMatchStatus;
import io.alw.css.serialization.confirmation.ConfirmationMatchStatusAvro;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConfirmationMatchStatusMapper {
    static ConfirmationMatchStatusMapper instance() {
        return Mappers.getMapper(ConfirmationMatchStatusMapper.class);
    }

    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    ConfirmationMatchStatusAvro domainToAvro(ConfirmationMatchStatus domain);

    @InheritInverseConfiguration
    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    ConfirmationMatchStatus avroToDomain(ConfirmationMatchStatusAvro avro);
}
