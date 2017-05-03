package de.geobe.spring.demo

import de.geobe.spring.demo.repository.UserRepository
import de.geobe.spring.demo.service.AdminTestService
import de.geobe.spring.demo.service.AuthenticationTestService
import de.geobe.spring.demo.service.TokenService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by georg beier on 01.05.2017.
 */
@Slf4j
@SpringBootTest(classes = AuthenticationApplication,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AuthenticationControllerSpecification extends Specification {
    @Autowired
    AuthenticationTestService authenticationTestService
    @Autowired
    AdminTestService adminTestService
    @Autowired
    TokenService tokenService
    @Autowired
    UserRepository userRepository

    @Ignore
    def "info should be retrieved from server"() {
        when: 'I call getInfo on AuthenticationService'
        def reply = authenticationTestService.info
        log.info("Reply is $reply")
        then: 'a json encoded string should be returned'
        reply == '{hurz=42, burz=26, demo=-7, quack={message=never, answer=perhaps}}'
    }

    @Ignore
    def 'login should return a web token or null on failure'() {
        when: 'I call login with suitable credentials'
        def reply = authenticationTestService.login('admin', 'admin')
        log.info("Reply is $reply")
        then: 'reply is json web token'
        reply ==~ /Bearer \w+\.\w+\..+/
        when: 'I call login with wrong credentials'
        reply = authenticationTestService.login('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply == null
    }

    @Ignore
    def 'login with jwts should return a web token or null on failure'() {
        when: 'I call jwtsLogin with suitable credentials'
        def reply = adminTestService.jwtsLogin('admin', 'admin')
        log.info("Reply is $reply")
        then: 'reply is json web token'
        reply ==~ /Bearer \w+\.\w+\..+/
        when: 'I call login with wrong credentials'
        reply = adminTestService.jwtsLogin('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply == null
    }

    @Ignore
    def 'should get a restricted resource with a good webtoken'() {
        when: 'I call login with suitable credentials'
        def token = adminTestService.jwtsLogin('admin', 'admin')
        token.replace('Bearer ', '')
        log.info("Token is $token")
        and: 'I use it to access a restricted ressource'
        def reply = authenticationTestService.accessRessource('http://localhost:8070/rogin', token)
        log.info("Reply is $reply")
        then: 'I get the requested reply'
        reply

    }

    @Ignore
    def 'should be able to create a new user'() {
        when: 'I create a new user using jwts'
        def name = 'Humpert' + System.currentTimeMillis()
        def reply = adminTestService.createUser(name, 'pumpert', ['HAPPY', 'SAD', 'BUSY'])
        log.info("Created: $reply")
        then: 'new user should exist'
        reply
        reply =~ '201'
        when: 'I login as this new user'
        def auth = adminTestService.jwtsLogin(name, 'pumpert')
        def body = tokenService.parseToken(auth)
        def uname = body.get('sub')
        then: 'user should be authenticated and existing in database'
        uname == name
        userRepository.findByUsername(name)
//        when: 'I look into SecurityContext (via TokenService)'
//        def authname = tokenService.currentUser
//        then: 'the user should be registered there'
//        authname == name
    }

    @Ignore
    def 'should be able to modify a user'() {
        when: 'I create a new user using jwts'
        def name = 'Humpert' + System.currentTimeMillis()
        def reply = adminTestService.createUser(name, 'pumpert', ['HAPPY', 'SAD', 'BUSY'])
        log.info("Created: $reply")
        then: 'new user should exist'
        reply
        reply =~ '201'
        when: 'I modify user and roles'
        reply = adminTestService.updateUser(name, 'trixi', ['CHEF', 'BUSY'])
        log.info("Updated: $reply")
        def auth = adminTestService.jwtsLogin(name, 'trixi')
        log.info("Authenticated: $auth")
        def u = userRepository.findByUsername(name)
        then: 'user should be authenticated and existing in database'
        auth
        u.roles.collect { it.authority }.contains('ROLE_CHEF')
        !u.roles.collect { it.authority }.contains('ROLE_HAPPY')
    }

    @Ignore
    def 'should be able to delete a user'() {
        when: 'I find sample users'
        def humperts = userRepository.findByUsernameLike('Humpert%')
        and: 'delete all of them'
        def reply
        humperts.each { humpert ->
            reply = adminTestService.deleteUser(humpert.username)
            log.info("Deleted: $reply")
        }
        then: 'operation succeeds'
        reply =~ '200'
        !userRepository.findByUsernameLike('Humpert%')
    }

    def 'should be able to change own password'() {
        setup: 'I create a temporary user, log in and get encrypted credentials'
        def name = 'Larry' + System.currentTimeMillis()
        def reply = adminTestService.createUser(name, 'humpf', ['DER_LARRY'])
        log.info("Created: $reply")
        def authtoken = adminTestService.jwtsLogin(name, 'humpf')
        log.info("Authenticated: $authtoken")
        def content = tokenService.parseToken(authtoken)
        log.info("auth content: $content")
        def credentials = content.credentials
        when: 'this user changes his password with correct input'
        def changed = adminTestService.changePassword(credentials, 'humpf', 'strumpf')
        authtoken = adminTestService.jwtsLogin(name, 'strumpf')
        log.info("Authenticated with new password: $authtoken")
        then: 'password is changed and login with new password is possible'
        changed
        authtoken
        when: 'I try to change with wrong old password'
        credentials = tokenService.parseToken(authtoken).credentials
        changed = adminTestService.changePassword(credentials, 'humpf', 'strumpf')
        then: 'change password has failed'
        !changed
    }
}
