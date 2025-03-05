package org.demo.bankingtestapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.roles")
public class SecurityProperties {

    private List<String> publicEndpoints = Collections.emptyList();
    private List<String> user = Collections.emptyList();
    private List<String> admin = Collections.emptyList();
    private List<String> agent = Collections.emptyList();
}
