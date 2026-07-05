package io.alw.css.tradeconsumer.cashflow.repository.mapper;

import io.alw.css.domain.cashflow.Cashflow;
import io.alw.css.tradeconsumer.cashflow.model.jpa.CashflowEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtil.class)
public interface CashflowMapper {
    static CashflowMapper instance() {
        return Mappers.getMapper(CashflowMapper.class);
    }

    @Mapping(source = ".", target = "cashflowEntityPK")
//    @Mapping(target = "tradeLinks", ignore = true)
    CashflowEntity mapToEntity_excludingAssociations(Cashflow cashflow);

    @Mapping(target = ".", source = "cashflowEntityPK")
//    @Mapping(target = "tradeLinks", ignore = true)
    Cashflow mapToDomain_excludingAssociations(CashflowEntity cashflowEntity);

    static CashflowEntity mapToEntity(Cashflow cashflow) {
        return instance().mapToEntity_excludingAssociations(cashflow);
    }
}
