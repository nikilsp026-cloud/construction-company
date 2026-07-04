// FILE: src/main/java/com/construction/config/AppProperties.java
package com.construction.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String name;
    private String tagline;
    private String email;
    private String phone;
    private String address;

    private Admin admin = new Admin();

    @Getter
    @Setter
    public static class Admin {
        private String username;
        private String email;
        private String password;
        private String fullName;
    }

}
