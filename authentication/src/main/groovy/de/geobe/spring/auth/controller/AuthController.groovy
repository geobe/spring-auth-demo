package de.geobe.spring.auth.controller

import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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
    String login(@RequestBody String cr) {
        log.info("login called with $cr")
        return [user: 'schummel',
                password: cr]
    }

}
