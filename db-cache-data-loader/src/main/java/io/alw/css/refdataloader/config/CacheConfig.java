package io.alw.css.refdataloader.config;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.SslMode;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class CacheConfig {

    @Value("${ignite.hosts}")
    String igniteHosts;

    @Bean
    public ClientConfiguration thinClientConfiguration() {
        Ignition.setClientMode(true);
        ClientConfiguration cfg = new ClientConfiguration();
        cfg.setSslMode(SslMode.DISABLED);
        cfg.setAddresses(igniteHosts.split(","));
        return cfg;
    }

    //    @Bean -> Does NOT work as of now!!!
    public IgniteConfiguration thickClientConfiguration() {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Arrays.asList(igniteHosts.split(",")));
        spi.setIpFinder(ipFinder);

        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setIgniteInstanceName("refdata-loader");
        cfg.setDiscoverySpi(spi);

        return cfg;
    }
}
