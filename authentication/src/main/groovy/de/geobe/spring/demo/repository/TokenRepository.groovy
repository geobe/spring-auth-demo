package de.geobe.spring.demo.repository

import de.geobe.spring.demo.domain.Token
import de.geobe.spring.demo.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Created by georg beier on 19.04.2017.
 */
interface TokenRepository extends JpaRepository<Token, Long> {
    @Query('select t.user from Token t where t.key = :key')
    User findUserForKey(String key)
    Token findByKey(String key)
}
