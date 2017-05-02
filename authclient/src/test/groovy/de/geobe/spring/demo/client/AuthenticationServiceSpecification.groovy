package de.geobe.spring.demo.client

import de.geobe.spring.demo.client.service.AuthenticationService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by georg beier on 01.05.2017.
 */
@Slf4j
//@SpringBootTest(classes = AuthclientApplication)
class AuthenticationServiceSpecification extends Specification {
    @Autowired
    AuthenticationService authenticationService = new AuthenticationService()

    def setup() {
        ReflectionTestUtils.setField(authenticationService, 'EXPIRATIONTIME', 720000000)
        ReflectionTestUtils.setField(authenticationService, 'SECRET', 'my_test_secret')
        ReflectionTestUtils.setField(authenticationService, 'ALGORITHM', 'HS256')
    }

    @Ignore
    def "info should be retrieved from server"(){
        when: 'I call getInfo on AuthenticationService'
        def reply = authenticationService.info
        log.info("Reply is $reply")
        then: 'a json encoded string should be returned'
        reply == '{hurz=42, burz=26, demo=-7, quack={message=never, answer=perhaps}}'
    }

//    @Ignore
    def 'login should return a web token or null on failure'() {
        when: 'I call login with suitable credentials'
        def reply = authenticationService.login('admin', 'admin')
        log.info("Reply is $reply")
        then: 'reply is json web token'
        reply ==~ /Bearer \w+\.\w+\..+/
        when: 'I call login with wrong credentials'
        reply = authenticationService.login('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply == null
    }

//    @Ignore
    def 'login with jwts should return a web token or null on failure'() {
        when: 'I call jwtsLogin with suitable credentials'
        def reply = authenticationService.jwtsLogin('admin', 'admin')
        log.info("Reply is $reply")
        then: 'reply is json web token'
        reply ==~ /Bearer \w+\.\w+\..+/
        when: 'I call login with wrong credentials'
        reply = authenticationService.jwtsLogin('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply == null
    }

    def 'should get a restricted resource with a good webtoken'() {
        when: 'I call login with suitable credentials'
        def token = authenticationService.jwtsLogin('admin', 'admin')
        token.replace('Bearer ', '')
        log.info("Token is $token")
        and: 'I use it to access a restricted ressource'
        def reply = authenticationService.accessRessource('http://localhost:8070/rogin', token)
        log.info("Reply is $reply")
        then: 'I get the requested reply'
        reply

    }
}
