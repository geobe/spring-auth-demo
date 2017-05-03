package de.geobe.spring.demo.service

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Created by georg beier on 01.05.2017.
 */
@Slf4j
@Service
class AdminAccessService {
    @Autowired
    TokenService tokenService

    def jwtsLogin(String uname, String pw, String url = 'http://localhost:8070/login') {
        RestTemplate template = tokenService.makeTemplate()
        def uri = new URI(url)
        String token = tokenService.makeToken([password: pw], uname)
        def jwsToken = tokenService.postForEntity(template, uri, token)?.
                headers?.get(tokenService.HEADER_STRING)?.get(0)
        if(jwsToken) {
            return tokenService.parseToken(jwsToken)
        } else {
            return [:]
        }
    }

    def createUser(String credentials,
                   String uname,
                   String pw,
                   List<String> roles,
                   String url = 'http://localhost:8070/admin/jwts/createuser') {
        if (credentials) {
            RestTemplate template = tokenService.makeTemplate(201)
            def uri = new URI(url)
            def claims = [user       : uname,
                          password   : pw,
                          roles      : roles,
                          credentials: credentials]
            String token = tokenService.makeToken(claims, tokenService.currentUser)
            def entity = tokenService.postForEntity(template, uri, token)
            return entity.statusCode == HttpStatus.CREATED
        }
    }

    def updateUser(String credentials,
                   String uname,
                   String pw,
                   List<String> roles,
                   String url = 'http://localhost:8070/admin/jwts/updateuser') {
        if (credentials) {
            RestTemplate template = tokenService.makeTemplate(200, 201)
            def uri = new URI(url)
            def claims = [user       : uname,
                          password   : pw,
                          roles      : roles,
                          credentials: credentials]
            String token = tokenService.makeToken(claims, tokenService.currentUser)
            def entity = tokenService.postForEntity(template, uri, token)
            return entity
        }
    }

    def deleteUser(String credentials,
                   String uname,
                   String url = 'http://localhost:8070/admin/jwts/deleteuser') {
        if (credentials) {
            RestTemplate template = tokenService.makeTemplate()
            def uri = new URI(url)
            def claims = [user       : uname,
                          credentials: credentials]
            String token = tokenService.makeToken(claims, tokenService.currentUser)
            def entity = tokenService.postForEntity(template, uri, token)
            return entity
        }
    }

    def changePassword(String credentials,
                       String oldpassword,
                       String newpassword,
                       String url = 'http://localhost:8070/admin/jwts/changepassword') {
        RestTemplate template = tokenService.makeTemplate(200, 403)
        def uri = new URI(url)
        def claims = [oldpassword: oldpassword,
                      newpassword: newpassword,
                      credentials: credentials]
        String token = tokenService.makeToken(claims, tokenService.currentUser)
        def entity = tokenService.postForEntity(template, uri, token)
        return entity?.statusCode == HttpStatus.OK
    }

    def logout(String credentials,
               String url = 'http://localhost:8070/logout') {
        RestTemplate template = tokenService.makeTemplate(200, 403)
        def uri = new URI(url)
        def claims = [credentials: credentials]
        String token = tokenService.makeToken(claims, tokenService.currentUser)
        def entity = tokenService.postForEntity(template, uri, token)
        return entity?.statusCode == HttpStatus.OK
    }

}
