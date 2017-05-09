package de.geobe.spring.auth.util

/**
 * Created by georg beier on 09.05.2017.
 *
 * Interface specifying access methods to the authentication microservice
 */
interface AdminAccess {

    /**
     * login with username and password stored in a secure JSON Web Token (JWTS)
     * @param uname username
     * @param pw password
     * @param url REST URL
     * @return map with credentials from returned JWTS or empty map on failure.
     *  The credentials token is used as authentication token for all furter requests.
     */
    def jwtsLogin(String uname, String pw, String url)

    /**
     * create user with all params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param uname username for new user
     * @param pw password for new user
     * @param roles roles (without prefix) for new user
     * @param url REST URL
     * @return true on success
     */
    def createUser(String credentials, String uname, String pw, List<String> roles, String url)

    /**
     * update existing user with all params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param uname username for of existing user
     * @param pw new password for user
     * @param new roles roles (without prefix) for user
     * @param url REST URL
     * @return true on success
     */
    def updateUser(String credentials, String uname, String pw, List<String> roles, String url)

    /**
     * delete existing user with params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param uname username of existing user
     * @param url REST URL
     * @return true on success
     */
    def deleteUser(String credentials, String uname, String url)

    /**
     * get list of users with credentials stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param url REST URL
     * @return list of existing role names
     */
    def getUsers(String credentials, String url)

    /**
     * create new role with all params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param rolename without prefix for new role
     * @param url REST URL
     * @return true on success
     */
    def createRole(String credentials, String rolename, String url)

    /**
     * delete existing role with params stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param rolename of existing role without prefix
     * @param url REST URL
     * @return true on success
     */
    def deleteRole(String credentials, String rolename, String url)

    /**
     * get list of roles with credentials stored in a secure JSON Web Token (JWTS) (admin only)
     * @param credentials token from login used for authentication
     * @param url REST URL
     * @return list of existing role names
     */
    def getRoles(String credentials, String url)

    /**
     * change password for current user with params stored in a secure JSON Web Token (JWTS)
     * (any authenticated users may change their password)
     * @param oldpassword as name says
     * @param newpassword as name says
     * @param url REST URL
     * @return true on success
     */
    def changePassword(String credentials, String oldpassword, String newpassword, String url)

    /**
     * current user logout with params stored in a secure JSON Web Token (JWTS)
     * (any authenticated user)
     * @param url REST URL
     * @return true on success
     */
    def logout(String credentials, String url)

}