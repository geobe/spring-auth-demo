package de.geobe.spring.demo.domain

import groovy.util.logging.Slf4j
import org.hibernate.annotations.Cascade

/**
 * Created by georg beier on 12.04.2017.
 */
import javax.persistence.*;
import java.util.Set;
@Slf4j
@Entity
@Table(name = "tbl_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id
    @Column(unique = true, nullable = false)
    private String username
    private String password
    private boolean enabled
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rel_user_role", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Role> roles = new HashSet<>();

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles)
    }

    public void addRole(Role role) {
        roles.add(role)
    }

    public void removeRole(Role role) {
        roles.remove(role)
    }

    public void updateRoles(Collection<Role> newRoles) {
        roles.clear()
        roles.addAll(newRoles)
    }

    @Override
    String toString() {
        return username
    }

    @Override
    int hashCode() {
        return username.hashCode()
    }
}