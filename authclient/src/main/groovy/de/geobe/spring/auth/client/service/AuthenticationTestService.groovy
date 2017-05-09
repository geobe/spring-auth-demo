package de.geobe.spring.auth.client.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

/**
 * Created by georg beier on 01.05.2017.
 * Test service for REST methods that were used during development
 * for understanding spring security but are not planned for production
 */
@Slf4j
@Service
class AuthenticationTestService {
    @Value('${geobe.jwt.expiration}')
    final long EXPIRATIONTIME
    @Value('${geobe.jwt.secret}')
    final String SECRET
    @Value('${geobe.jwt.algorithm}')
    final String ALGORITHM
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";

    def getInfo() {
        RestTemplate template = new RestTemplate()
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            protected boolean hasError(HttpStatus statusCode) {
                if (statusCode.value() == 401)
                    return false;
                else
                    return true
            }
        })
        def uri = new URI('http://localhost:8070/info')
        try {
            def reply = template.getForObject(uri, String.class)
            return reply
        } catch (Exception ex) {
            return ex.toString()
        }
    }

    def login(String uname, String pw) {
        RestTemplate template = new RestTemplate()
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            protected boolean hasError(HttpStatus statusCode) {
                if (statusCode.value() == 200)
                    return false;
                else
                    return true
            }
        })
        def uri = new URI('http://localhost:8070/login')
        ObjectMapper mapper = new ObjectMapper()
        def body = mapper.writeValueAsString([username: uname,
                                              password: pw])
        try {
            def reply = template.postForEntity(uri, body, String.class)
            def auth = reply.headers.get(HEADER_STRING)
            return auth?.get(0)
        } catch (Exception ex) {
            log.info("Exception $ex")
        }
    }

    def jwtsLogin(String uname, String pw) {
        RestTemplate template = new RestTemplate()
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            protected boolean hasError(HttpStatus statusCode) {
                if (statusCode.value() == 200)
                    return false;
                else
                    return true
            }
        })
        def uri = new URI('http://localhost:8070/login')
        String token = Jwts.builder()
                .setClaims([password: pw])
                .setSubject(uname)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.forName(ALGORITHM), SECRET)
                .compact();
        def headers = new HttpHeaders()
        headers.add(HEADER_STRING, TOKEN_PREFIX + " " + token)
        HttpEntity<String> httpEntity = new HttpEntity<>(headers)
        try {
            def reply = template.postForEntity(uri, httpEntity, String.class)
            log.info("Reply is $reply")
            def auth = reply.headers.get(HEADER_STRING)
            return auth?.get(0)
        } catch (Exception ex) {
            log.info("Exception $ex")
        }
    }

    def logout(String credentials) {
        RestTemplate template = new RestTemplate()
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            protected boolean hasError(HttpStatus statusCode) {
                if (statusCode.value() == 200 || statusCode.value() == 403)
                    return false;
                else
                    return true
            }
        })
        def uri = new URI('http://localhost:8070/logout')
        def claims = [credentials: credentials]
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject('')
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.forName(ALGORITHM), SECRET)
                .compact();
        def headers = new HttpHeaders()
        headers.add(HEADER_STRING, TOKEN_PREFIX + " " + token)
        HttpEntity<String> httpEntity = new HttpEntity<>(headers)
        try {
            def responseEntity = template.postForEntity(uri, httpEntity, String.class)
            log.info("Reply is $responseEntity")
            return responseEntity?.statusCode == HttpStatus.OK
        } catch (Exception ex) {
            log.info("Exception $ex")
            return false
        }
    }

    def accessResource(String url, String token) {
        RestTemplate template = new RestTemplate()
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            protected boolean hasError(HttpStatus statusCode) {
                if (statusCode.value() == 200)
                    return false;
                else
                    return true
            }
        })
        def uri = new URI(url)
        ObjectMapper mapper = new ObjectMapper()
        def body = mapper.writeValueAsString([aparam: 'Hurz',
                                              bparam: 'Burz'])
        def headers = new HttpHeaders()
        headers.add('Authorization', token)
        headers.add("Content-Type", "application/json")
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers)
        try {
            def reply = template.postForEntity(uri, httpEntity, String.class)
            log.info("Reply is $reply")
            return reply.body
        } catch (Exception ex) {
            log.info("Exception $ex")
        }

    }
}
