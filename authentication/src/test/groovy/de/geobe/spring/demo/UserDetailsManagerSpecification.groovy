package de.geobe.spring.demo

import de.geobe.spring.demo.repository.RoleRepository
import de.geobe.spring.demo.repository.UserRepository
import de.geobe.spring.demo.service.TokenUserDetailsManager
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

    def "should boot up without errors"() {
        expect: "web application context exists"
        context != null
    }

    def "there should be an admin created"() {
        when: 'look for user admin'
        def u = userDetailsManager.loadUserByUsername('admin')
        then:
        u != null
        u?.authorities.size() == 3
    }

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
        roleRepository.count() == 4
//        cleanup:
//        if (u) userRepository.delete(u)
//        if (r) roleRepository.delete(r)
    }
}
