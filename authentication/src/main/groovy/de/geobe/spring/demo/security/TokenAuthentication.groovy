package de.geobe.spring.demo.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * Created by georg beier on 19.04.2017.
 */
class TokenAuthentication implements Authentication {

    String username
    List<SimpleGrantedAuthority> authorities
    Object credentials

//    @Override
//    Collection<? extends GrantedAuthority> getAuthorities() {
//        List<SimpleGrantedAuthority> auths = new LinkedList<>()
//        token?.user.roles.each { role ->
//            auths.add(new SimpleGrantedAuthority(role.username))
//        }
//        return auths
//    }

//    @Override
//    Object getCredentials() {
//        return token?.key
//    }

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
