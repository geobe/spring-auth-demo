package de.geobe.spring.demo.security

import groovy.util.logging.Slf4j
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

/**
 * Created by georg beier on 19.04.2017.
 */
@Slf4j
class TokenAuthenticationProvider implements AuthenticationProvider {
    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("TokenAuthenticationProvider called with $authentication")
        return authentication
    }

    @Override
    boolean supports(Class<?> authentication) {
        return TokenAuthentication.class
    }
}
