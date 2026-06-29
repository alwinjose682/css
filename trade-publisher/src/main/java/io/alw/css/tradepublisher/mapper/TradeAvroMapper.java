package io.alw.css.tradepublisher.mapper;

import io.alw.css.domain.common.TradeLink;
import io.alw.css.domain.trade.Trade;
import io.alw.css.domain.trade.TradeLeg;
import io.alw.css.serialization.trade.TradeAvro;
import io.alw.css.serialization.trade.TradeLegAvro;
import io.alw.css.serialization.trade.TradeLinkAvro;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Mapper
public interface TradeAvroMapper {
    //NOTE: MapStruct does correctly map between the two types of enum values such as 'PayOrReceive' and 'TradeEventAction'
    static TradeAvroMapper instance() {
        return Mappers.getMapper(TradeAvroMapper.class);
    }

    @Mapping(source = "tradeLinks", target = "tradeLinks", qualifiedByName = "mapTradeLinksToAvro")
    TradeAvro domainToAvro(Trade trade);

    @Mapping(target = "tradeLinks", source = "tradeLinks", qualifiedByName = "mapAvroToTradeLinks")
    @InheritInverseConfiguration
    Trade avroToDomain(TradeAvro tradeAvro);


    List<TradeLegAvro> tradeLegToAvro(Set<TradeLeg> tradeLegs);

    Set<TradeLeg> tradeLegAvroToDomain(List<TradeLegAvro> tradeLegs);

    @Mapping(source = "valueDate", target = "valueDate", qualifiedByName = "mapJavaTimeDateToString")
    TradeLegAvro tradeLegToAvro(TradeLeg tradeLeg);

    @Mapping(target = "valueDate", source = "valueDate", qualifiedByName = "mapStringToJavaTimeDate")
    TradeLeg tradeLegAvroToDomain(TradeLegAvro tradeLeg);

    @Named("mapTradeLinksToAvro")
    static List<TradeLinkAvro> mapTradeLinksToAvro(List<TradeLink> tradeLinks) {
        return tradeLinks == null
                ? null
                : tradeLinks.stream().map(tl -> new TradeLinkAvro(
                tl.linkType(),
                tl.relatedReference(),
                tl.relatedTradeId(),
                tl.relatedTradeVersion()
        )).toList();
    }

    @Named("mapAvroToTradeLinks")
    static List<TradeLink> mapAvroToTradeLinks(List<TradeLinkAvro> tradeLinksAvro) {
        return tradeLinksAvro == null
                ? null
                : tradeLinksAvro.stream().map(tla -> new TradeLink(
                tla.getLinkType(),
                tla.getRelatedReference(),
                tla.getRelatedTradeId(),
                tla.getRelatedTradeVersion()
        )).toList();
    }

    @Named("mapJavaTimeDateToString")
    static String mapJavaTimeDateToString(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_DATE) : null;
    }

    @Named("mapStringToJavaTimeDate")
    static LocalDate mapStringToJavaTimeDate(String strDate) {
        return strDate != null ? LocalDate.parse(strDate, DateTimeFormatter.ISO_DATE) : null;
    }
}
