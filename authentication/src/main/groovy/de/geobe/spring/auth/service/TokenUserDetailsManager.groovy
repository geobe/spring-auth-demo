package de.geobe.spring.auth.service

import de.geobe.spring.auth.domain.Role
import de.geobe.spring.auth.domain.User
import de.geobe.spring.auth.repository.RoleRepository
import de.geobe.spring.auth.repository.TokenRepository
import de.geobe.spring.auth.repository.UserRepository
import de.geobe.spring.auth.util.TokenService
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
 * Implementation of spring's UserDetailsManager for our specific database schema
 * Some additional methods are added for explicit role management.
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

    /**
     * delete a role and remove it from all users that have this role. As usual in spring,
     * standard role prefix from RoleVoter (default is ROLE_) is prepended before rolename.
     * @param rolename without prefix
     * @return true if there was a role found for deletion
     */
    @Transactional
    boolean deleteRole(String rolename) {
        def pfx = 'ROLE_'
        Role role = roleRepository.findByName(pfx + rolename)
        if(role) {
            List<User> users = userRepository.selectByRoleName(pfx+rolename)
            users.each { User user ->
                user.removeRole(role)
            }
            roleRepository.delete(role.id)
            return true
        }
        return false
    }

    /**
     * create a new role in the repository. As usual in spring,
     * standard role prefix from RoleVoter (default is ROLE_) is prepended before rolename.
     * @param rolename without prefix
     * @return true if no exception caused by unique constraint violation was thrown
     */
    @Transactional
    boolean createRole(String rolename) {
        def pfx = 'ROLE_'
        try {
            def role = new Role(name: pfx+rolename)
            roleRepository.saveAndFlush(role)
            return true
        } catch (RuntimeException rex) {
            return false
        }
    }

    /**
     * get all role names with prefix stripped
     * @return List of role names
     */
    @Transactional(readOnly = true)
    def getRoles(){
        def roles = roleRepository.findAll()
        def pfx = 'ROLE_'
        return roles.collect { role ->
            role.authority.replace(pfx, '')
        }
    }

    /**
     * get all user names
     * @return List of user names
     */
    @Transactional(readOnly = true)
    def getUsers(){
        def users = userRepository.findAll()
        return users.collect { it.username }
    }

}
