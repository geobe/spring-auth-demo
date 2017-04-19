package de.geobe.spring.demo.service

import groovy.util.logging.Slf4j
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacSigner
import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static java.util.Collections.emptyList

/**
 * Created by georg beier on 18.04.2017.
 * based on article
 * @see <a href="https://auth0.com/blog/securing-spring-boot-with-jwts/>
 * "Securing Spring Boot with JWTs" </a> and using
 * @see <a href="https://github.com/jwtk/jjwt">io.jsonwebtoken library</a>.
 */
@Slf4j
@Service
class TokenAuthenticationService {
    @Value('${geobe.jwt.expiration}')
    final long EXPIRATIONTIME
    @Value('${geobe.jwt.secret}')
    final String SECRET
    @Value('${geobe.jwt.algorithm}')
    final String ALGORITHM
    @Value('${geobe.jwt.tokenkey}')
    final String TOKENKEY
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";

    private MacSigner authSigner

    /**
     * adds authentification information to a response header.
     * @param response response to be decorated
     * @param username authentication information
     */
    void addAuthentication(HttpServletResponse response, Authentication authentication) {
        String JWT = Jwts.builder()
                .setClaims([principal  : authentication.principal,
                            authorities: authentication.authorities,
                            credentials: makeCredentials(authentication)])
                .setSubject(authentication.principal.toString())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.forName(ALGORITHM), SECRET)
                .compact();
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
        log.info("Authentication: $authentication")
    }

    public String makeCredentials(Authentication authentication) {
        byte[] data = authentication.principal.toString().bytes
        if(! authSigner) authSigner =
                new MacSigner(SignatureAlgorithm.forName(ALGORITHM), TOKENKEY.bytes)
        return Base64.getEncoder().encodeToString(authSigner.sign(data))
    }

    /**
     * retrieve authentication from request header
     * @param request incoming http request
     * @return authetication information
     */
    Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            // parse the token.
            def body = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody()
            def user = body.getSubject();
            log.info("User: $user, Body: $body")
//TODO securely identify user from JWT
            return user != null ?
                    (new UsernamePasswordAuthenticationToken(user, null, emptyList())) :
                    null;
        }
        return null;
    }
}
