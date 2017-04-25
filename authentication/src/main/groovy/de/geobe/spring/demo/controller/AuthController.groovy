package de.geobe.spring.demo.controller

import de.geobe.spring.demo.filter.AccountCredentials
import groovy.util.logging.Slf4j
import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Created by georg beier on 18.04.2017.
 */
@Slf4j
@RestController
class AuthController {

    @RequestMapping('/info')
    @ResponseBody
    String getInfo() {
        return [hurz : 42,
                burz : 26,
                demo : -7,
                quack: [message: 'never',
                        answer : 'perhaps']]
    }

    @RequestMapping("/rogin")
    @ResponseBody
    String login(@RequestBody AccountCredentials cr) {
        log.info("login called with $cr")
        return "logged in now as ${cr.username} with ${cr.password}"
    }

}
