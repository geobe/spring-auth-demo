package de.geobe.spring.demo.controller

import org.eclipse.jetty.http.HttpMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Created by georg beier on 18.04.2017.
 */
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

    @RequestMapping(method = RequestMethod.POST, path = "/login")
    String login() {
        return 'logged in now'
    }
}
