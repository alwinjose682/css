package io.alw.css.refdataloader.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("spring.datasource")
public class DataSourceProperties {
    private final String driverClassName;
    private final String url;
    private final String username;
    private final String password;

    @ConstructorBinding
    public DataSourceProperties(String driverClassName, String url, String username, String password) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String driverClassName() {
        return driverClassName;
    }

    public String password() {
        return password;
    }

    public String url() {
        return url;
    }

    public String username() {
        return username;
    }
}
