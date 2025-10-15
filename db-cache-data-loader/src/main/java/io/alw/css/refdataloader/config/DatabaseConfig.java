package io.alw.css.refdataloader.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.alw.css.refdataloader.model.properties.DataSourceProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean("dBDatasource")
    public DataSource dataSource(DataSourceProperties dsProps) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(dsProps.driverClassName());
        config.setJdbcUrl(dsProps.url());
        config.setUsername(dsProps.username());
        config.setPassword(dsProps.password());
        config.setAutoCommit(false); // IMPORTANT (spring.datasource.hikari.auto-commit=false)
//        config.setConnectionTimeout();
//        config.setIdleTimeout();
//        config.setMaxLifetime();

        //TODO:
        // config.setThreadFactory(Thread.ofVirtual().factory());
        Properties info = new Properties();
        info.put("v$session.program", "CssApp-" + System.getProperty("jdbc.program-name"));
        config.setDataSourceProperties(info);

        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean emfBean(@Qualifier("dBDatasource") DataSource dataSource) {
        var hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        var emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setPersistenceUnitName("RefdataloaderPersistenceUnit");
        emf.setPackagesToScan("io.alw.css.refdataloader.model.jpa");
        emf.setJpaVendorAdapter(hibernateJpaVendorAdapter);

        Map<String, String> props = new HashMap<>();
//        setPerformanceProperties(props);
//        setCachingProperties(props);
        setOtherProperties(props);
        emf.setJpaPropertyMap(props);
        return emf;
    }

    private static void setOtherProperties(Map<String, String> props) {
        props.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
    }

    private void setPerformanceProperties(Map<String, String> props) {
        /*
        props.put("hibernate.connection.provider_disables_autocommit", "true"); // But, this does not work if spring's read-only transaction is used. The workaround to fix this causes other issues. Check vlad's article: "Spring Transaction and Connection Management"
        */

        props.put("hibernate.jdbc.batch_size", "200");
        props.put("hibernate.order_inserts", "true");
        props.put("hibernate.order_updates", "true");
        //        props.put("hibernate.jdbc.batch_versioned_data","true"); // Not Applicable for db-cache-data-loader
        //        props.put("hibernate.generate_statistics","true"); # Not used. Instead, JFR profile data produced by hibernate-jfr is used


//        props.put("eclipselink.jdbc.native-sql", "true");
    }

//    ------------------------------------------------------------------------------------------------------------------------------------
//    private void setCachingProperties(Map<String, String> props) {
//        props.put("eclipselink.cache.type.default", "NONE");
//        props.put("eclipselink.jdbc.cache-statements", "true");
//        props.put("eclipselink.jdbc.cache-statements.size", "500");
//    }

//    private PlatformTransactionManager txManager() {
//        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
//        jpaTransactionManager.setValidateExistingTransaction(true); // This is enabled temporarily only for some checks
//        return jpaTransactionManager;
//    }
}
