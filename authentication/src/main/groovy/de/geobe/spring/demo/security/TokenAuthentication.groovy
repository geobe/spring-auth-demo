package de.geobe.spring.demo.security

import de.geobe.spring.demo.domain.Token
import de.geobe.spring.demo.domain.User
import de.geobe.spring.demo.repository.TokenRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

import javax.naming.OperationNotSupportedException

/**
 * Created by georg beier on 19.04.2017.
 */
class TokenAuthentication implements Authentication {

    private Token token

    @Override
    Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> auths = new LinkedList<>()
        token?.user.roles.each { role ->
            auths.add(new SimpleGrantedAuthority(role.name))
        }
        return auths
    }

    @Override
    Object getCredentials() {
        return token?.key
    }

    @Override
    Object getDetails() {
        return null
    }

    @Override
    Object getPrincipal() {
        return token?.user
    }

    @Override
    boolean isAuthenticated() {
        return token?.user != null
    }

    @Override
    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) throw new IllegalArgumentException()
    }

    @Override
    String getName() {
        return token?.user.username
    }
}
