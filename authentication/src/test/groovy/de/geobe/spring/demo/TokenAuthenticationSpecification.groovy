package de.geobe.spring.demo

import de.geobe.spring.demo.service.TokenAuthenticationService
import groovy.util.logging.Slf4j
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import spock.lang.Ignore
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by georg beier on 19.04.2017.
 */
@Slf4j
@SpringBootTest(classes = AuthenticationApplication)
class TokenAuthenticationSpecification extends Specification {

    @Value('${geobe.jwt.expiration}')
    final long EXPIRATIONTIME
    @Value('${geobe.jwt.secret}')
    final String SECRET
    final String TESTTOKEN = 'eyJhbGciOiJIUzI1NiJ9' +
            '.eyJwcmluY2lwYWwiOiJhZG1pbiIsImF1dGhvcml0aWVzIjpbXSwiY3JlZGVudGlhbHMiOiIvUFNrZnRTSTM5' +
            'YTFDUktXWk14anRIcFhpWW05TkV2RnNpL2dyZlREZjFZPSIsInN1YiI6ImFkbWluIiwiZXhwIjoxNDkzMzk5N' +
            'TQ5fQ.-HW06cOzcvKSrrFlBzfQklrhEZ4S3f8l1KKjBSd7Vos'
//    static final String TOKEN_PREFIX = "Bearer";
//    static final String HEADER_STRING = "Authorization";

    @Autowired
    TokenAuthenticationService tokenAuthenticationService

    def setup() {
        log.info("Expiration time is ${EXPIRATIONTIME} msec")
        log.info("Secret is ${SECRET}")
    }

//    @Ignore
    def 'WebToken should be added to response header'() {
        setup: 'we need a response to add a header'
        HttpServletResponse rsp = new MockHttpServletResponse()
        def admin = new UsernamePasswordAuthenticationToken(
                'admin',
                'admin',
                Collections.emptyList()
        )

        when: 'I add authentication to rsp header'
        tokenAuthenticationService.addAuthentication(rsp, admin)
        def token = rsp.getHeader(TokenAuthenticationService.HEADER_STRING)
        def jwt = token.replace(TokenAuthenticationService.TOKEN_PREFIX, "")
        def parser = Jwts.parser().setSigningKey(SECRET)
        def claims = parser.parseClaimsJws(jwt)
        def body = claims.body
        def sub = body.getSubject()
        log.info("Token: $jwt")
        log.info("claims: $claims")
        log.info("body: $body")
        log.info("subject: $sub")
        then:
        token.startsWith(TokenAuthenticationService.TOKEN_PREFIX)
        sub == 'admin'
    }

    @Ignore
    def 'Webtoken should be successfully decoded'() {
        setup: 'I need a HttpServletRequest'
        HttpServletRequest req = new MockHttpServletRequest()
        req.addHeader(TokenAuthenticationService.HEADER_STRING,
                TokenAuthenticationService.TOKEN_PREFIX + " " + TESTTOKEN)
        when: 'I get authentication from the request'
        def auth = tokenAuthenticationService.getAuthentication(req)
        log.info("Authentication: $auth")
        log.info("Authorities: ${auth?.authorities}")
        then: 'I should get authentication data'
        auth?.principal.toString() == 'admin'
        auth?.authenticated
        auth?.authorities.collect {it.authority}.containsAll(['ROLE_default', 'ROLE_admin', 'ROLE_user'])
    }

    @Ignore
    def 'Principal should be successfully encoded and decoded' () {
        setup: 'I create a mock of a principal string'
        def prip = 'meister,ROLE_master,ROLE_knister'
        when: 'I encode this and decode it afterwards'
        def eprip = tokenAuthenticationService.encryptPrincipal(prip)
        def dprip = tokenAuthenticationService.decryptPrincipal(eprip)
        log.info("encoded: $eprip")
        log.info("decoded: $dprip")
        then: 'they should be equal'
        dprip == prip
    }
}
