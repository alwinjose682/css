package io.alw.css.tradepublisher.confirmation.mapper;

import io.alw.css.confirmation.TradeMatchRequest;
import io.alw.css.serialization.confirmation.TradeMatchRequestAvro;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TradeMatchRequestMapper {
    static TradeMatchRequestMapper instance() {
        return Mappers.getMapper(TradeMatchRequestMapper.class);
    }

    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    TradeMatchRequestAvro domainToAvro(TradeMatchRequest tradeMatchRequest);

    @InheritInverseConfiguration
    @Mapping(source = "tradeLegMatchAttributes", target = "tradeLegMatchAttributes")
    TradeMatchRequest avroToDomain(TradeMatchRequestAvro tradeMatchRequestAvro);
}
