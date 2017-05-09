package de.geobe.spring.auth.util

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Created by georg beier on 01.05.2017.
 *
 * Service class for restful http access to the authentication microservice
 */
@Slf4j
@Service
class AdminAccessImpl implements AdminAccess {
    @Autowired
    TokenService tokenService

    /**
     * login with username and password stored in a secure JSON Web Token (JWTS)
     * @param uname username
     * @param pw password
     * @param url REST URL
     * @return map with credentials from returned JWTS or empty map on failure.
     *  The credentials token is used as authentication token for all furter requests.
     */
    @Override
    def jwtsLogin(String uname, String pw, String url = 'http://localhost:8070/login') {
        RestTemplate template = tokenService.makeTemplate()
        def uri = new URI(url)
        String token = tokenService.makeToken([password: pw], uname)
        def jwsToken = tokenService.postForEntity(template, uri, token)?.
                headers?.get(tokenService.HEADER_STRING)?.get(0)
        if (jwsToken) {
            return tokenService.parseToken(jwsToken)
        } else {
            return [:]
        }
    }

    /**
     * create user with all params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param uname username for new user
     * @param pw password for new user
     * @param roles roles (without prefix) for new user
     * @param url REST URL
     * @return true on success
     */
    @Override
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
            return entity?.statusCode == HttpStatus.CREATED
        }
    }

    /**
     * update existing user with all params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param uname username for of existing user
     * @param pw new password for user
     * @param new roles roles (without prefix) for user
     * @param url REST URL
     * @return true on success
     */
    @Override
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
            return entity?.statusCode == HttpStatus.OK
        }
    }

    /**
     * delete existing user with params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param uname username of existing user
     * @param url REST URL
     * @return true on success
     */
    @Override
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
            return entity?.statusCode == HttpStatus.OK
        }
    }

    /**
     * get list of users with credentials stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param url REST URL
     * @return list of existing role names
     */
    @Override
    def getUsers(String credentials,
                 String url = 'http://localhost:8070/admin/jwts/getusers') {
        if (credentials) {
            RestTemplate template = tokenService.makeTemplate(HttpStatus.OK.value())
            def uri = new URI(url)
            def claims = [credentials: credentials]
            String token = tokenService.makeToken(claims, tokenService.currentUser)
            def entity = tokenService.postForEntity(template, uri, token)
            def users = tokenService.parseToken(entity?.body).users
            return users
        }
    }

    /**
     * create new role with all params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param rolename without prefix for new role
     * @param url REST URL
     * @return true on success
     */
    @Override
    def createRole(String credentials,
                   String rolename,
                   String url = 'http://localhost:8070/admin/jwts/createrole') {
        if (credentials) {
            RestTemplate template = tokenService.makeTemplate(
                    HttpStatus.CREATED.value(), HttpStatus.CONFLICT.value())
            def uri = new URI(url)
            def claims = [rolename   : rolename,
                          credentials: credentials]
            String token = tokenService.makeToken(claims, tokenService.currentUser)
            def entity = tokenService.postForEntity(template, uri, token)
            return entity.statusCode == HttpStatus.CREATED
        }
    }

    /**
     * delete existing role with params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param rolename of existing role without prefix
     * @param url REST URL
     * @return true on success
     */
    @Override
    def deleteRole(String credentials,
                   String rolename,
                   String url = 'http://localhost:8070/admin/jwts/deleterole') {
        if (credentials) {
            RestTemplate template = tokenService.makeTemplate(
                    HttpStatus.OK.value(), HttpStatus.NOT_FOUND.value())
            def uri = new URI(url)
            def claims = [rolename       : rolename,
                          credentials: credentials]
            String token = tokenService.makeToken(claims, tokenService.currentUser)
            def entity = tokenService.postForEntity(template, uri, token)
            return entity.statusCode == HttpStatus.OK
        }
    }

    /**
     * get list of roles with credentials stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param url REST URL
     * @return list of existing role names
     */
    @Override
    def getRoles(String credentials,
                   String url = 'http://localhost:8070/admin/jwts/getroles') {
        if (credentials) {
            RestTemplate template = tokenService.makeTemplate(HttpStatus.OK.value())
            def uri = new URI(url)
            def claims = [credentials: credentials]
            String token = tokenService.makeToken(claims, tokenService.currentUser)
            def entity = tokenService.postForEntity(template, uri, token)
            def roles = tokenService.parseToken(entity?.body).roles
            return roles
        }
    }

    /**
     * change password for current user with params stored in a secure JSON Web Token (JWTS)
     * (any authenticated users may change their password)
     * @param oldpassword as name says
     * @param newpassword as name says
     * @param url REST URL
     * @return true on success
     */
    @Override
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

    /**
     * current user logout with params stored in a secure JSON Web Token (JWTS)
     * (any authenticated user)
     * @param url REST URL
     * @return true on success
     */
    @Override
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
