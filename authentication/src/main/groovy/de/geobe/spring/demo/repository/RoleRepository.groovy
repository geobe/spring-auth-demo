package de.geobe.spring.demo.repository

import de.geobe.spring.demo.domain.Role
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by georg beier on 17.04.2017.
 */
interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name)
    List<Role>  findByNameLike(String pattern)
    List<Role>  findByNameIn(Collection<String> roles)
}