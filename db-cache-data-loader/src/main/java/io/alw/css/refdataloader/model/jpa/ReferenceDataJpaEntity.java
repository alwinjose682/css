package io.alw.css.refdataloader.model.jpa;

public sealed interface ReferenceDataJpaEntity
        permits CountryEntity, CurrencyEntity, EntityEntity, NostroEntity, CounterpartyEntity, SsiEntity, CounterpartyNettingProfileEntity, CounterpartySlaMappingEntity, BookEntity
{
}
