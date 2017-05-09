package de.geobe.spring.auth.security

import groovy.util.logging.Slf4j
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

import java.security.SecureRandom

/**
 * Created by georg beier on 28.04.2017.
 */
@Slf4j
@Configuration
class EncoderConfiguration {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        SecureRandom random = new SecureRandom();
        def encoder = new BCryptPasswordEncoder(12, random);
        log.info("PWEncoder: $encoder")
        return encoder
    }

}
