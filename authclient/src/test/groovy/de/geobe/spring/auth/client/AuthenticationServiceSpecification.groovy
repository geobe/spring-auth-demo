package de.geobe.spring.auth.client

import de.geobe.spring.auth.util.AdminAccess
import de.geobe.spring.auth.util.AdminAccessImpl
import de.geobe.spring.auth.util.TokenService
import groovy.util.logging.Slf4j
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

/**
 * Created by georg beier on 01.05.2017.
 *
 * Test of the client admin api layer for the authentication service
 */
@Slf4j
//@SpringBootTest(classes = AuthclientApplication)
class AuthenticationServiceSpecification extends Specification {

    AdminAccess adminAccess = new AdminAccessImpl()
    TokenService tokenService = new TokenService()

    def setup() {
        ReflectionTestUtils.setField(tokenService, 'EXPIRATIONTIME', 720000000)
        ReflectionTestUtils.setField(tokenService, 'SECRET', 'my_test_secret')
        ReflectionTestUtils.setField(tokenService, 'ALGORITHM', 'HS256')
        adminAccess.tokenService = tokenService
    }

//    @Ignore
    def 'login, logout with jwts should work'() {
        when: 'I call jwtsLogin with suitable credentials'
        def reply = adminAccess.jwtsLogin('admin', 'admin')
        log.info("Reply is $reply")
        then: 'reply is json web token credentials'
        reply instanceof Map
        when: 'I call login with wrong credentials'
        def reply2 = adminAccess.jwtsLogin('admin', 'mimi')
        log.info("Reply is $reply")
        then: 'reply is null'
        reply2 == [:]
        when: 'I logout'
        def ok = adminAccess.logout(reply.credentials)
        then: 'logout always succeeds, so no evidence is gained here'
        ok
    }

//    @Ignore
    def 'admin should create and delete users'() {
        when: 'login as admin'
        def claims = adminAccess.jwtsLogin('admin', 'admin')
        def cred = claims.credentials
        then: 'I am admin'
        claims.sub == 'admin'
        cred
        when: 'I create a user'
        def ok = adminAccess.createUser(cred, 'schaf', 'schaf', ['BLÖK'])
        then: 'as admin I can do it'
        ok
        when: 'I logout (logout always returns 200 OK)'
        adminAccess.logout(cred)
        ok = adminAccess.deleteUser(cred, 'schaf')
        then:'no more admin ops should succeed'
        !ok
        when: 'I login again for new authentication token'
        claims = adminAccess.jwtsLogin('admin', 'admin')
        cred = claims.credentials
        ok = adminAccess.deleteUser(cred, 'schaf')
        then: 'delete operation should succeed'
        ok
        cleanup: 'and logout again'
        adminAccess.deleteUser(cred, 'schaf')
        adminAccess.deleteRole(cred, 'BLÖK')
        adminAccess.logout(cred)
    }

//    @Ignore
//    @Transactional
    def 'creating and deleting roles should work sensibly'() {
        when: 'after login as admin I create a new user with two roles'
        def loginresult = adminAccess.jwtsLogin('admin', 'admin')
        def cred = loginresult.credentials
        def ok = adminAccess.createUser(cred, 'wolf', 'grrrr', ['HIUUUU', 'KNURR'])
        and: 'I get a list of all roles'
        def roles = (List<String>) adminAccess.getRoles(cred)
        log.info("roles in db are $roles")
        then: 'wolf is there and it contains Roles of new user'
        ok
        roles.containsAll(['HIUUUU', 'KNURR'])
        when: 'I delete one role of wolf'
        adminAccess.deleteRole(cred, 'KNURR')
        roles = (List<String>) adminAccess.getRoles(cred)
        then: 'role should be removed from db'
        ! roles.contains('KNURR')
        cleanup: ' delete wolf and its roles and logout'
        ok = adminAccess.deleteUser(cred, 'wolf')
        adminAccess.deleteRole(cred, 'HIUUUU')
        adminAccess.logout(cred)
    }

//    @Transactional
    def 'working with user and role lists should work'() {
        setup: 'login and get my own roles'
        def loginresult = adminAccess.jwtsLogin('admin', 'admin')
        def cred = loginresult.credentials
        def auths = loginresult.authorities
        log.info("login: $loginresult")
        log.info("Authorities: $auths")
        def myRoles = auths.collect {it.toString().replace('ROLE_', '')}
        log.info("my roles are $myRoles")
        and: 'create some users and roles'
        def created = adminAccess.createUser(cred, 'wolf', 'grrrr', ['HIUUUU', 'KNURR'])
        log.info("created user wolf $created")
        created = adminAccess.createUser(cred, 'schaf', 'schaf', ['BLÖK'])
        log.info("created user schaf $created")
        created = adminAccess.createRole(cred, 'LION')
        log.info("created role LION $created")
        when: 'I get users and roles'
        def roles = adminAccess.getRoles(cred)
        def users = adminAccess.getUsers(cred)
        then: 'everyone is here'
        ['admin', 'schaf', 'wolf'].containsAll(users)
        (myRoles + ['HIUUUU', 'KNURR'] + 'BLÖK' + 'LION').containsAll(roles)
        when: 'I delete all these new users and roles'
        def delRoles = roles - myRoles
        def delUsers = users - 'admin'
        log.info("deleting $delUsers and $delRoles")
        delRoles.each { role ->
            adminAccess.deleteRole(cred, role)
        }
        delUsers.each { user ->
            adminAccess.deleteUser(cred, user)
        }
        users = adminAccess.getUsers(cred)
        roles = adminAccess.getRoles(cred)
        then: ' everything is clean again'
        users == ['admin']
        myRoles.containsAll(roles)
        cleanup: 'logout'
        adminAccess.logout(cred)
    }
}
