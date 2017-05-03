package de.geobe.spring.demo.controller

import de.geobe.spring.demo.service.TokenService
import de.geobe.spring.demo.service.TokenUserDetailsManager
import groovy.util.logging.Slf4j
import org.springframework.http.HttpHeaders
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
    @Autowired
    TokenService tokenService

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

    @RequestMapping(path = '/jwts/createuser', method = RequestMethod.POST)
    @ResponseBody
    public String createFromJwts(@RequestHeader HttpHeaders headers, HttpServletResponse response) {
        Map<String, Object> content = tokenService.getTokenContent(headers)
        User.UserBuilder builder = User.withUsername(content.user);
        builder.password(content.password);
        builder.roles(content.roles as String[]);
        try {
            userDetailsManager.createUser(builder.build())
            response.setStatus(HttpStatus.CREATED.value())
            return 'created'
        } catch (Exception ex) {
            response.setStatus(HttpStatus.CONFLICT.value())
            return "Creation of ${content.user} failed"
        }
    }

    @RequestMapping(path = '/jwts/updateuser', method = RequestMethod.POST)
    @ResponseBody
    public String updateFromJwts(@RequestHeader HttpHeaders headers, HttpServletResponse response) {
        Map<String, Object> content = tokenService.getTokenContent(headers)
        User.UserBuilder builder = User.withUsername(content.user);
        builder.password(content.password);
        builder.roles(content.roles as String[]);
        try {
            userDetailsManager.updateUser(builder.build())
            response.setStatus(HttpStatus.OK.value())
            return 'updated'
        } catch (Exception ex) {
            response.setStatus(HttpStatus.CONFLICT.value())
            return "Creation of ${content.user} failed"
        }
    }

    @RequestMapping(path = '/jwts/changepassword', method = RequestMethod.POST)
    @ResponseBody
    public String updatedFromJwts(@RequestHeader HttpHeaders headers, HttpServletResponse response) {
        Map<String, Object> content = tokenService.getTokenContent(headers)
        try {
            if (userDetailsManager.changePassword(
                    tokenService.currentUser,
                    content.oldpassword,
                    content.newpassword)) {
                response.setStatus(HttpStatus.OK.value())
                return 'password updated'
            } else {
                response.setStatus(HttpStatus.FORBIDDEN.value())
                return 'password update not allowed'
            }
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
            return "password update exception: $ex"
        }
    }

    @RequestMapping(path = '/jwts/deleteuser', method = RequestMethod.POST)
    @ResponseBody
    public String deleteFromJwts(@RequestHeader HttpHeaders headers, HttpServletResponse response) {
        Map<String, Object> content = tokenService.getTokenContent(headers)
        userDetailsManager.deleteUser(content.user)
        response.setStatus(HttpStatus.OK.value())
        return "${content.user} deleted"
    }
}

class UserInfo {
    String user
    String pw
    String[] roles
}
