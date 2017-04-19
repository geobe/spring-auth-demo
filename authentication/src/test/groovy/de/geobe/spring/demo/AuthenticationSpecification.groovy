package de.geobe.spring.demo

import groovy.util.logging.Log
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import spock.lang.Specification

/**
 * Created by georg beier on 18.04.2017.
 */
@Slf4j
@SpringBootTest(classes = AuthenticationApplication)
class AuthenticationSpecification extends Specification {
    @Autowired
    AuthenticationManager authenticationManager

    def "should have an AuthenticationManager" (){
        expect:
        authenticationManager != null
    }

    def "should be possible to authenticate admin" (){
        when: 'I authorize as admin'
        def token = new UsernamePasswordAuthenticationToken('admin', 'admin')
        def auth = authenticationManager.authenticate(token)
        log.info("authenticate returned $auth")
        then: 'I get a fully populated Authentication'
        auth != null
    }

}
