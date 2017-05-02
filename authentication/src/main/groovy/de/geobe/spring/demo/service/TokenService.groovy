package de.geobe.spring.demo.service

import groovy.util.logging.Slf4j
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

/**
 * Created by georg beier on 02.05.2017.
 */
@Slf4j
@Service
class TokenService {

    @Value('${geobe.jwt.expiration}')
    final long EXPIRATIONTIME
    @Value('${geobe.jwt.secret}')
    final String SECRET
    @Value('${geobe.jwt.algorithm}')
    final String ALGORITHM
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";

    String makeToken(Map<String, String> claims, String subject) {
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.forName(ALGORITHM), SECRET)
                .compact();
        token
    }

    Map<String, Object> parseToken(String jwsToken) {
        def body = Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(
                jwsToken.replace(TOKEN_PREFIX, ""))
                .getBody()
        body
    }

    ResponseEntity postForEntity(RestTemplate template,
                                 URI uri,
                                 String token,
                                 Map<String, String> moreHeaders = [:]) {
        def headers = new HttpHeaders()
        headers.add(HEADER_STRING, TOKEN_PREFIX + " " + token)
        moreHeaders.each { k, v ->
            headers.add(k, v)
        }
        HttpEntity<String> httpEntity = new HttpEntity<>(headers)
        try {
            def reply = template.postForEntity(uri, httpEntity, String.class)
            log.info("Reply is $reply")
            return reply
        } catch (Exception ex) {
            log.info("Exception $ex")
        }
    }

    RestTemplate makeTemplate(int ... codes = [200]) {
        RestTemplate template = new RestTemplate()
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            protected boolean hasError(HttpStatus statusCode) {
                if (codes.contains(statusCode.value()))
                    return false;
                else
                    return true
            }
        })
        template
    }

    Map<String, Object> getTokenContent(HttpHeaders headers) {
        def auth = headers.get(TokenService.HEADER_STRING)
        def jwts = auth?.get(0).replace(TokenService.TOKEN_PREFIX, '')
        def content = parseToken(jwts)
        content
    }

    def getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication()?.getPrincipal()

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
        } else if (principal) {
            String username = principal.toString();
        } else {
            "MickyMaus"
        }
    }
}
