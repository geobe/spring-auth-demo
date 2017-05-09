package de.geobe.spring.auth.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * Created by georg beier on 19.04.2017.
 */
class TokenAuthentication implements Authentication {

    String username
    List<SimpleGrantedAuthority> authorities
    Object credentials

    @Override
    Object getDetails() {
        return null
    }

    @Override
    Object getPrincipal() {
        return username
    }

    @Override
    boolean isAuthenticated() {
        return credentials != null
    }

    @Override
    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) throw new IllegalArgumentException()
    }

    @Override
    String getName() {
        return username
    }
}
