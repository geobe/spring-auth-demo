package de.geobe.spring.demo.repository

import de.geobe.spring.demo.domain.Token
import de.geobe.spring.demo.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Created by georg beier on 19.04.2017.
 */
interface TokenRepository extends JpaRepository<Token, Long> {
    @Query('select t.user from Token t where t.key = :key')
    User findUserForKey(@Param(value = 'key') String key)
    Token findByKey(String key)
    @Query(value = 'select exists(select 1 from tbl_token t where t.key =:key) as x', nativeQuery = true)
    boolean keyExists(@Param(value = 'key') String key)
    void deleteByUser(User user)
}
