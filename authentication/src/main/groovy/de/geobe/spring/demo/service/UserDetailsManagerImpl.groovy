package de.geobe.spring.demo.service

import de.geobe.spring.demo.domain.Role
import de.geobe.spring.demo.domain.User
import de.geobe.spring.demo.repository.RoleRepository
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
import org.springframework.web.HttpRequestMethodNotSupportedException

/**
 * Created by georg beier on 17.04.2017.
 */
@Service
class UserDetailsManagerImpl implements UserDetailsManager {
    @Autowired
    private UserRepository userRepository
    @Autowired
    private RoleRepository roleRepository
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
        u.password = user.password
        u.enabled = user.enabled
        List<Role> roles = manageRoles(user)
        u.updateRoles(roles)
        userRepository.saveAndFlush(u)
    }

    @Override
    @Transactional
    void deleteUser(String username) {
        User u = userRepository.findByUsername(username)
        if (u) userRepository.delete(u)
    }

    @Override
    void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("cannot change password without user details")
    }

    @Transactional
    void changePassword(UserDetails user, String oldPassword, String newPassword) {
        User u = userRepository.findByUsername(user.username)
        if (pwEncoder.matches(oldPassword, u.password)) {
            u.password = pwEncoder.encode(newPassword)
            userRepository.saveAndFlush(u)
        }
    }

    @Override
    @Transactional(readOnly = true)
    boolean userExists(String username) {
        User u = userRepository.findByUsername(username)
        return u != null
    }
}
