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
    @Query("from User u left join u.roles role where role.name = ?1")
    List<User> selectByRole(String rolename)
}