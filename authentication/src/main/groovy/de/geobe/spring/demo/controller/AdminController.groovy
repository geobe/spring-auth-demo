package de.geobe.spring.demo.controller

import de.geobe.spring.demo.service.TokenUserDetailsManager
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletResponse

/**
 * Created by georg beier on 25.04.2017.
 */
@Slf4j
@RestController
@RequestMapping(value = '/admin')
class AdminController {

    @Autowired
    TokenUserDetailsManager userDetailsManager

    @RequestMapping(path = '/user', method = RequestMethod.POST)
    @ResponseBody
    public String create(@RequestBody UserInfo ui, HttpServletResponse response) {
        log.info("user: ${ui.user}, pw: ${ui.pw}, roles: ${ui.roles}")
        log.info("Response: $response")
        User.UserBuilder builder = User.withUsername(ui.user);
        builder.password(ui.pw);
        builder.roles(ui.roles);
        try {
            userDetailsManager.createUser(builder.build())
            response.setStatus(HttpStatus.CREATED.value())
            return 'created'
        } catch (Exception ex) {
            response.setStatus(HttpStatus.CONFLICT.value())
            return "Creation of ${ui.user} failed"
        }
    }
}

class UserInfo {
    String user
    String pw
    String[] roles
}
