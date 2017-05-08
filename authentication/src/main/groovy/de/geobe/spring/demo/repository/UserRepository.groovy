package de.geobe.spring.demo.repository

import de.geobe.spring.demo.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Created by georg beier on 12.04.2017.
 */
interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String name)
    List<User> findByUsernameLike(String name)
    @Query("select u from User u join u.roles r where r.name = ?1")
    List<User> selectByRoleName(String rolename)
}