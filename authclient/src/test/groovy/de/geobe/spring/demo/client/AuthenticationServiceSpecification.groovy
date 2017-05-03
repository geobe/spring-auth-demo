package de.geobe.spring.demo.client

import de.geobe.spring.demo.client.service.AuthenticationService
import de.geobe.spring.demo.service.AdminAccessService
import de.geobe.spring.demo.service.TokenService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by georg beier on 01.05.2017.
 */
@Slf4j
//@SpringBootTest(classes = AuthclientApplication)
class AuthenticationServiceSpecification extends Specification {
//    @Autowired
    AuthenticationService authenticationService = new AuthenticationService()
//    @Autowired
    AdminAccessService adminAccessService = new AdminAccessService()
    TokenService tokenService = new TokenService()

    def setup() {
        ReflectionTestUtils.setField(authenticationService, 'EXPIRATIONTIME', 720000000)
        ReflectionTestUtils.setField(authenticationService, 'SECRET', 'my_test_secret')
        ReflectionTestUtils.setField(authenticationService, 'ALGORITHM', 'HS256')

        ReflectionTestUtils.setField(tokenService, 'EXPIRATIONTIME', 720000000)
        ReflectionTestUtils.setField(tokenService, 'SECRET', 'my_test_secret')
        ReflectionTestUtils.setField(tokenService, 'ALGORITHM', 'HS256')
        adminAccessService.tokenService = tokenService
    }

//    @Ignore
    def "info should be retrieved from server"() {
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
        def reply2 = authenticationService.login('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply2 == null
        cleanup: 'logout'
        adminAccessService.logout(tokenService.parseToken(reply).credentials)
    }

//    @Ignore
    def 'login with jwts should return a web token or null on failure'() {
        when: 'I call jwtsLogin with suitable credentials'
        def reply = authenticationService.jwtsLogin('admin', 'admin')
        log.info("Reply is $reply")
        then: 'reply is json web token'
        reply ==~ /Bearer \w+\.\w+\..+/
        when: 'I call login with wrong credentials'
        def reply2 = authenticationService.jwtsLogin('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply2 == null
        cleanup: 'logout'
        adminAccessService.logout(tokenService.parseToken(reply).credentials)
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
        cleanup: 'logout'
        adminAccessService.logout(tokenService.parseToken(token).credentials)
    }

    def 'login, logout should succeed with adminAccessService'() {
        when: 'I call jwtsLogin with suitable credentials'
        def claims = adminAccessService.jwtsLogin('admin', 'admin')
        log.info("Reply is $claims")
        then: 'claims is map with all needed infos'
        claims.sub == 'admin'
        claims.credentials
        when: 'I create a user'
        def ok = adminAccessService.createUser(claims.credentials, 'wolf', 'schaf', ['BLÖK'])
        then: 'as admin I can do it'
        ok
        when: 'I logout (logout always returnd 200 OK)'
        adminAccessService.logout(claims.credentials)
        ok = adminAccessService.deleteUser(claims.credentials, 'wolf')
        then:'no more admin ops should succeed'
        !ok
        when: 'I login again for new authentication token'
        claims = adminAccessService.jwtsLogin('admin', 'admin')
        ok = adminAccessService.deleteUser(claims.credentials, 'wolf')
        then: 'delete operation should succeed'
        ok
        cleanup: 'logout again'
        adminAccessService.logout(claims.credentials)
    }
}
