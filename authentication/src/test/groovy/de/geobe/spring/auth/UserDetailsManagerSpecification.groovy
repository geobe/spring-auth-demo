package de.geobe.spring.auth

import de.geobe.spring.auth.repository.RoleRepository
import de.geobe.spring.auth.repository.UserRepository
import de.geobe.spring.auth.service.TokenUserDetailsManager
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.User
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

/**
 * Created by georg beier on 17.04.2017.
 */
@Slf4j
@SpringBootTest(classes = AuthenticationApplication)
class UserDetailsManagerSpecification extends Specification {

    @Autowired
    TokenUserDetailsManager userDetailsManager
    @Autowired
    WebApplicationContext context
    @Autowired
    UserRepository userRepository
    @Autowired
    RoleRepository roleRepository

//    @Ignore
    def "should boot up without errors"() {
        expect: "web application context exists"
        context != null
    }

//    @Ignore
    def "there should be an admin created"() {
        when: 'look for user admin'
        def u = userDetailsManager.loadUserByUsername('admin')
        then:
        u != null
        u?.authorities.size() == 3
    }

//    @Ignore
    @Transactional
    def "should be possible to create new user with a new role"() {
        when: 'I create a new user'
        User.UserBuilder builder = User.withUsername("testuser2");
        builder.password("teuteu");
        builder.roles("tester");
        userDetailsManager.createUser(builder.build());
        and: ' I retrieve that user from db'
        def u = userRepository.findByUsername('testuser2')
        def r = u?.roles.first()
        then:
        u != null
        u.username == 'testuser2'
        r != null
        r.name.endsWith 'tester'
        roleRepository.findByNameLike('%tester')
//        cleanup:
//        if (u) userRepository.delete(u)
//        if (r) roleRepository.delete(r)
    }

    def 'should be possible to delete all Humperts'(){
        when: 'there are users'
        def humperts = userRepository.findByUsernameLike('Humpert%')
        humperts.each {h ->
            userDetailsManager.deleteUser(h.username)
        }
        then: 'no more of them'
        !userRepository.findByUsernameLike('Humpert%')
    }
}
