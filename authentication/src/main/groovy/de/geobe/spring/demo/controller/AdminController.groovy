package de.geobe.spring.demo.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Created by georg beier on 25.04.2017.
 */
@Controller
@RequestMapping(value = '/admin')
class AdminController {

    @RequestMapping(path = '/user', method = RequestMethod.POST)
    public void create() {

    }
}
