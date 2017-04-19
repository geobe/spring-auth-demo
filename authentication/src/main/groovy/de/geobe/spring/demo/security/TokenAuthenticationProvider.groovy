package de.geobe.spring.demo.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

/**
 * Created by georg beier on 19.04.2017.
 */
class TokenAuthenticationProvider implements AuthenticationProvider {
    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return authentication
    }

    @Override
    boolean supports(Class<?> authentication) {
        return TokenAuthentication.class
    }
}
