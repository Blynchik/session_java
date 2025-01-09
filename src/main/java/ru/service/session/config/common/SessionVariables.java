package ru.service.session.config.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("session")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionVariables {

    private long duration;
    private long lifetime;
}
