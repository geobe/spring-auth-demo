package de.geobe.spring.auth.client

import de.geobe.spring.auth.util.AdminAccessService
import de.geobe.spring.auth.util.TokenService
import groovy.util.logging.Slf4j
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by georg beier on 01.05.2017.
 *
 * Test of the client admin api layer for the authentication service
 */
@Slf4j
//@SpringBootTest(classes = AuthclientApplication)
class AuthenticationServiceSpecification extends Specification {

    AdminAccessService adminAccessService = new AdminAccessService()
    TokenService tokenService = new TokenService()

    def setup() {
        ReflectionTestUtils.setField(tokenService, 'EXPIRATIONTIME', 720000000)
        ReflectionTestUtils.setField(tokenService, 'SECRET', 'my_test_secret')
        ReflectionTestUtils.setField(tokenService, 'ALGORITHM', 'HS256')
        adminAccessService.tokenService = tokenService
    }

//    @Ignore
    def 'login, logout with jwts should work'() {
        when: 'I call jwtsLogin with suitable credentials'
        def reply = adminAccessService.jwtsLogin('admin', 'admin')
        log.info("Reply is $reply")
        then: 'reply is json web token credentials'
        reply instanceof Map
        when: 'I call login with wrong credentials'
        def reply2 = adminAccessService.jwtsLogin('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply2 == [:]
        when: 'I logout'
        def ok = adminAccessService.logout(reply.credentials)
        then: 'logout always succeeds, so no evidence is gained here'
        ok
    }

//    @Ignore
    def 'admin should create and delete users'() {
        when: 'login as admin'
        def claims = adminAccessService.jwtsLogin('admin', 'admin')
        def cred = claims.credentials
        then: 'I am admin'
        claims.sub == 'admin'
        cred
        when: 'I create a user'
        def ok = adminAccessService.createUser(cred, 'schaf', 'schaf', ['BLÖK'])
        then: 'as admin I can do it'
        ok
        when: 'I logout (logout always returns 200 OK)'
        adminAccessService.logout(cred)
        ok = adminAccessService.deleteUser(cred, 'schaf')
        then:'no more admin ops should succeed'
        !ok
        when: 'I login again for new authentication token'
        claims = adminAccessService.jwtsLogin('admin', 'admin')
        cred = claims.credentials
        ok = adminAccessService.deleteUser(cred, 'schaf')
        then: 'delete operation should succeed'
        ok
        cleanup: 'and logout again'
        adminAccessService.deleteUser(cred, 'schaf')
        adminAccessService.deleteRole(cred, 'BLÖK')
        adminAccessService.logout(cred)
    }

//    @Ignore
//    @Transactional
    def 'creating and deleting roles should work sensibly'() {
        when: 'after login as admin I create a new user with two roles'
        def loginresult = adminAccessService.jwtsLogin('admin', 'admin')
        def cred = loginresult.credentials
        def ok = adminAccessService.createUser(cred, 'wolf', 'grrrr', ['HIUUUU', 'KNURR'])
        and: 'I get a list of all roles'
        def roles = (List<String>) adminAccessService.getRoles(cred)
        log.info("roles in db are $roles")
        then: 'wolf is there and it contains Roles of new user'
        ok
        roles.containsAll(['HIUUUU', 'KNURR'])
        when: 'I delete one role of wolf'
        adminAccessService.deleteRole(cred, 'KNURR')
        roles = (List<String>) adminAccessService.getRoles(cred)
        then: 'role should be removed from db'
        ! roles.contains('KNURR')
        cleanup: ' delete wolf and its roles and logout'
        ok = adminAccessService.deleteUser(cred, 'wolf')
        adminAccessService.deleteRole(cred, 'HIUUUU')
        adminAccessService.logout(cred)
    }

//    @Transactional
    def 'working with user and role lists should work'() {
        setup: 'login and get my own roles'
        def loginresult = adminAccessService.jwtsLogin('admin', 'admin')
        def cred = loginresult.credentials
        def auths = loginresult.authorities
        log.info("login: $loginresult")
        log.info("Authorities: $auths")
        def myRoles = auths.collect {it.toString().replace('ROLE_', '')}
        log.info("my roles are $myRoles")
        and: 'create some users and roles'
        def created = adminAccessService.createUser(cred, 'wolf', 'grrrr', ['HIUUUU', 'KNURR'])
        log.info("created user wolf $created")
        created = adminAccessService.createUser(cred, 'schaf', 'schaf', ['BLÖK'])
        log.info("created user schaf $created")
        created = adminAccessService.createRole(cred, 'LION')
        log.info("created role LION $created")
        when: 'I get users and roles'
        def roles = adminAccessService.getRoles(cred)
        def users = adminAccessService.getUsers(cred)
        then: 'everyone is here'
        ['admin', 'schaf', 'wolf'].containsAll(users)
        (myRoles + ['HIUUUU', 'KNURR'] + 'BLÖK' + 'LION').containsAll(roles)
        when: 'I delete all these new users and roles'
        def delRoles = roles - myRoles
        def delUsers = users - 'admin'
        log.info("deleting $delUsers and $delRoles")
        delRoles.each { role ->
            adminAccessService.deleteRole(cred, role)
        }
        delUsers.each { user ->
            adminAccessService.deleteUser(cred, user)
        }
        users = adminAccessService.getUsers(cred)
        roles = adminAccessService.getRoles(cred)
        then: ' everything is clean again'
        users == ['admin']
        myRoles.containsAll(roles)
        cleanup: 'logout'
        adminAccessService.logout(cred)
    }
}
