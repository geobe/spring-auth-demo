package de.geobe.spring.demo.service

import de.geobe.spring.demo.domain.Role
import de.geobe.spring.demo.domain.User
import de.geobe.spring.demo.repository.RoleRepository
import de.geobe.spring.demo.repository.TokenRepository
import de.geobe.spring.demo.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by georg beier on 17.04.2017.
 */
@Service
class TokenUserDetailsManager implements UserDetailsManager {
    @Autowired
    private UserRepository userRepository
    @Autowired
    private RoleRepository roleRepository
    @Autowired
    TokenRepository tokenRepository
    @Autowired
    TokenService tokenService
    @Autowired
    private BCryptPasswordEncoder pwEncoder;

    /**
     * Creates a spring default UserDetails implementation object from the database
     * @param name
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    @Transactional(readOnly = true)
    UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(name)
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : u.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.name));
        }
        return new org.springframework.security.core.userdetails.User(u.username,
                u.password, grantedAuthorities);
    }

    @Override
    @Transactional
    void createUser(UserDetails user) {
        User u = new User(username: user.username,
                password: pwEncoder.encode(user.password),
                enabled: user.enabled)
        List<Role> roles = manageRoles(user)
        roles.each { role -> u.addRole(role) }
        userRepository.saveAndFlush(u)
    }

    private List<Role> manageRoles(UserDetails user) {
        List<String> rolenames = user.getAuthorities().collect { it.authority }
        List<Role> roles = roleRepository.findByNameIn(rolenames)
        rolenames.minus(roles.collect { it.name }).each { name ->
            def role = new Role(name: name)
            roles.add(role)
        }
        roles
    }

    @Override
    @Transactional
    void updateUser(UserDetails user) {
        User u = userRepository.findByUsername(user.username)
        u.password = pwEncoder.encode(user.password)
        u.enabled = user.enabled
        List<Role> roles = manageRoles(user)
        u.updateRoles(roles)
        userRepository.saveAndFlush(u)
    }

    @Override
    @Transactional
    void deleteUser(String username) {
        User u = userRepository.findByUsername(username)
        if (u) {
            def roles = []
            roles.addAll(u.roles)
            roles.each { role ->
                u.removeRole(role)
            }
            tokenRepository.deleteByUser(u)
            userRepository.delete(u)
        }
    }

    @Override
    void changePassword(String oldPassword, String newPassword) {
        changePassword(tokenService.currentUser, oldPassword, newPassword)
    }

    @Transactional
    boolean changePassword(String username, String oldPassword, String newPassword) {
        User u = userRepository.findByUsername(username)
        if (pwEncoder.matches(oldPassword, u.password)) {
            u.password = pwEncoder.encode(newPassword)
            userRepository.saveAndFlush(u)
            return true
        }
        return false
    }

    @Override
    @Transactional(readOnly = true)
    boolean userExists(String username) {
        User u = userRepository.findByUsername(username)
        return u != null
    }
}
