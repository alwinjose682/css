package io.alw.css.model.referencedata;

/// NOTE: The ignite caches are created by sql DDL statements.
/// These names are used in SQL DDL statements to specify the cacheName with which the ignite key value api can access the cache
public final class IgniteCacheName {
    public static final String COUNTRY = "country";
    public static final String CURRENCY = "currency";
    public static final String COUNTERPARTY = "counterparty";
    public static final String SSI = "ssi";
    public static final String CP_NETTING_PROFILE = "cp_netting_profile";
    public static final String CP_SLA_MAPPING = "cp_sla_mapping";
    public static final String ENTITY = "entity";
    public static final String NOSTRO = "nostro";
    public static final String BOOK = "book";
}
