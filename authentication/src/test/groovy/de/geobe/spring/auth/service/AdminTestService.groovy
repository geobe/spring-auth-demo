package de.geobe.spring.auth.service

import de.geobe.spring.auth.util.TokenService
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
class AdminTestService {
    @Autowired
    TokenService tokenService

    def jwtsLogin(String uname, String pw, String url = 'http://localhost:8070/login') {
        RestTemplate template = tokenService.makeTemplate()
        def uri = new URI(url)
        String token = tokenService.makeToken([password: pw], uname)
        return tokenService.postForEntity(template, uri, token)?.
                headers?.get(tokenService.HEADER_STRING)?.get(0)
    }

    def createUser(String uname,
                   String pw,
                   List<String> roles,
                   String url = 'http://localhost:8070/admin/jwts/createuser') {
        def authToken = jwtsLogin('admin', 'admin')
        def credentials = tokenService.parseToken(authToken)?.get('credentials')
        if (credentials) {
            RestTemplate template = tokenService.makeTemplate(201)
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

    def updateUser(String uname,
                   String pw,
                   List<String> roles,
                   String url = 'http://localhost:8070/admin/jwts/updateuser') {
        def authToken = jwtsLogin('admin', 'admin')
        def credentials = tokenService.parseToken(authToken)?.get('credentials')
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

    def deleteUser(String uname,
                   String url = 'http://localhost:8070/admin/jwts/deleteuser') {
        def authToken = jwtsLogin('admin', 'admin')
        def credentials = tokenService.parseToken(authToken)?.get('credentials')
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

}
