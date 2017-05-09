package de.geobe.spring.auth.client

import de.geobe.spring.auth.client.service.AuthenticationTestService
import groovy.util.logging.Slf4j
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

/**
 * Created by georg beier on 01.05.2017.
 *
 * Low level Tests during development. Should be removed for production version.
 */
@Slf4j
//@SpringBootTest(classes = AuthclientApplication)
class AuthenticationTestServiceSpecification extends Specification {

    AuthenticationTestService authenticationTestService = new AuthenticationTestService()

    def setup() {
        ReflectionTestUtils.setField(authenticationTestService, 'EXPIRATIONTIME', 720000000)
        ReflectionTestUtils.setField(authenticationTestService, 'SECRET', 'my_test_secret')
        ReflectionTestUtils.setField(authenticationTestService, 'ALGORITHM', 'HS256')
    }

//    @Ignore
    def "info should be retrieved from server"() {
        when: 'I call getInfo on AuthenticationTestService to access a not protected resource'
        def reply = authenticationTestService.info
        log.info("Reply is $reply")
        then: 'a json encoded string should be returned'
        reply == '{hurz=42, burz=26, demo=-7, quack={message=never, answer=perhaps}}'
    }

//    @Ignore
    def 'login should return a web token or null on failure'() {
        when: 'I call login with suitable credentials'
        def reply = authenticationTestService.login('admin', 'admin')
        log.info("Reply is $reply")
        then: 'reply is json web token'
        reply ==~ /Bearer \w+\.\w+\..+/
        when: 'I logout with that token'
        def ok = authenticationTestService.logout(reply.replace('Bearer ', ''))
        then: 'it should succeed'
        ok
        when: 'I call login with wrong credentials'
        def reply2 = authenticationTestService.login('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply2 == null
    }

//    @Ignore
    def 'should get a restricted resource with a good webtoken'() {
        when: 'I call login with suitable credentials'
        def credentials = authenticationTestService.login('admin', 'admin')
        credentials.replace('Bearer ', '')
        and: 'I use it to access a restricted resource'
        def reply = authenticationTestService.accessResource('http://localhost:8070/rogin', credentials)
        log.info("Reply is $reply")
        then: 'I get the requested reply'
        reply
        cleanup: 'logout'
        authenticationTestService.logout(credentials)
    }
}
