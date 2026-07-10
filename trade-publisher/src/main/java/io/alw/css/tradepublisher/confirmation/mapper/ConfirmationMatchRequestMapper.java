package io.alw.css.tradepublisher.confirmation.mapper;

import io.alw.css.confirmation.ConfirmationMatchRequest;
import io.alw.css.serialization.confirmation.ConfirmationMatchRequestAvro;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConfirmationMatchRequestMapper {
    static ConfirmationMatchRequestMapper instance() {
        return Mappers.getMapper(ConfirmationMatchRequestMapper.class);
    }

    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    ConfirmationMatchRequestAvro domainToAvro(ConfirmationMatchRequest confirmationMatchRequest);

    @InheritInverseConfiguration
    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    ConfirmationMatchRequest avroToDomain(ConfirmationMatchRequestAvro confirmationMatchRequestAvro);
}
