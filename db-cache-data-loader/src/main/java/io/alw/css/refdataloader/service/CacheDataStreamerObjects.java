package io.alw.css.refdataloader.service;

import io.alw.css.model.referencedata.*;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteDataStreamer;

final class CacheDataStreamerObjects {
    private final Ignite ignite;

    private volatile IgniteDataStreamer<CounterpartyCache.Key, CounterpartyCache> counterpartyStreamer;
    private volatile IgniteDataStreamer<CounterpartySlaMappingCache.Key, CounterpartySlaMappingCache> cpSlaMappingStreamer;
    private volatile IgniteDataStreamer<CounterpartyNettingProfileCache.Key, CounterpartyNettingProfileCache> cpNettingProfileStreamer;
    private volatile IgniteDataStreamer<SsiCache.Key, SsiCache> ssiStreamer;
    private volatile IgniteDataStreamer<NostroCache.Key, NostroCache> nostroStreamer;

    CacheDataStreamerObjects(Ignite ignite) {
        this.ignite = ignite;
    }

    void closeAllStreamers() {
        counterpartyStreamer.close();
        cpSlaMappingStreamer.close();
        cpNettingProfileStreamer.close();
        ssiStreamer.close();
        nostroStreamer.close();
    }

    IgniteDataStreamer<CounterpartyCache.Key, CounterpartyCache> counterpartyStreamer() {
        if (counterpartyStreamer == null) {
            synchronized (this) {
                if (counterpartyStreamer == null) {
                    counterpartyStreamer = ignite.dataStreamer(IgniteCacheName.COUNTERPARTY);
                }
            }
        }
        return counterpartyStreamer;
    }

    IgniteDataStreamer<CounterpartySlaMappingCache.Key, CounterpartySlaMappingCache> cpSlaMappingStreamer() {
        if (cpSlaMappingStreamer == null) {
            synchronized (this) {
                if (cpSlaMappingStreamer == null) {
                    cpSlaMappingStreamer = ignite.dataStreamer(IgniteCacheName.CP_SLA_MAPPING);
                }
            }
        }
        return cpSlaMappingStreamer;
    }

    IgniteDataStreamer<CounterpartyNettingProfileCache.Key, CounterpartyNettingProfileCache> cpNettingProfileStreamer() {
        if (cpNettingProfileStreamer == null) {
            synchronized (this) {
                if (cpNettingProfileStreamer == null) {
                    cpNettingProfileStreamer = ignite.dataStreamer(IgniteCacheName.CP_NETTING_PROFILE);
                }
            }
        }
        return cpNettingProfileStreamer;
    }

    IgniteDataStreamer<SsiCache.Key, SsiCache> ssiStreamer() {
        if (ssiStreamer == null) {
            synchronized (this) {
                if (ssiStreamer == null) {
                    ssiStreamer = ignite.dataStreamer(IgniteCacheName.SSI);
                }
            }
        }
        return ssiStreamer;
    }

    IgniteDataStreamer<NostroCache.Key, NostroCache> nostroStreamer() {
        if (nostroStreamer == null) {
            synchronized (this) {
                if (nostroStreamer == null) {
                    nostroStreamer = ignite.dataStreamer(IgniteCacheName.NOSTRO);
                }
            }
        }
        return nostroStreamer;
    }
}