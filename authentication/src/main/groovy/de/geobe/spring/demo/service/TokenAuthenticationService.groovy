package de.geobe.spring.demo.service

import de.geobe.spring.demo.domain.Token
import de.geobe.spring.demo.repository.TokenRepository
import de.geobe.spring.demo.repository.UserRepository
import de.geobe.spring.demo.security.TokenAuthentication
import groovy.util.logging.Slf4j
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacSigner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by georg beier on 18.04.2017.
 * based on article
 * @see <a href="https://auth0.com/blog/securing-spring-boot-with-jwts/>
 * "Securing Spring Boot with JWTs" </a> and using
 * @see <a href="https://github.com/jwtk/jjwt">io.jsonwebtoken library</a>.
 */
//@Slf4j
@Service
class TokenAuthenticationService {
    @Value('${geobe.jwt.expiration}')
    final long EXPIRATIONTIME
    @Value('${geobe.jwt.secret}')
    final String SECRET
    @Value('${geobe.jwt.algorithm}')
    final String ALGORITHM
    @Value('${geobe.jwt.tokenkey}')
    final String TOKENKEY
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";

    @Autowired
    UserRepository userRepository
    @Autowired
    TokenRepository tokenRepository
    @Autowired
    TextEncryptor textEncryptor

    private MacSigner authSigner

    /**
     * adds authentication information to a response header and stores authentication key
     * in the database. Plain text information on principal and authorities is for client use
     * only. The authentication service only uses the encrypted credentials.
     * @param response response to be decorated
     * @param username authentication information
     */
    void addAuthentication(HttpServletResponse response,
                           UsernamePasswordAuthenticationToken authentication) {
        if (!(authentication.principal instanceof User)) {
            return
        }
        String uname = ((User) authentication.principal).username
        def authorities = ((User) authentication.principal).authorities.collect {it.authority}
        def credentials = storeCredentials(uname, authorities.toArray(new String[0]))
        String JWT = Jwts.builder()
                .setClaims([principal  : uname,
                            authorities: authorities,
                            credentials: credentials])
                .setSubject(uname)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.forName(ALGORITHM), SECRET)
                .compact();
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
    }

    /**
     * encrypt and encode message containing principal information using shared secret
     * @param msg plain message
     * @return base64 encoded encrypted message
     */
    private String encryptPrincipal(String msg) {
        return Base64.getEncoder().encodeToString(textEncryptor.encrypt(msg).bytes)
    }

    /**
     * decode and decrypt message containing principal information using shared secret
     * @param msg64 base64 encoded encrzpted message
     * @return decrypted message
     */
    private String decryptCredentials(String msg64) {
        def eprip = new String(Base64.getDecoder().decode(msg64), 'UTF-8')
        return textEncryptor.decrypt(eprip)
    }

    @Transactional
    private String storeCredentials(String username, String... authorities) {
        StringBuffer sb = new StringBuffer(username)
        authorities.each { aut ->
            sb.append(',').append(aut)
        }
        def tokenCredentials = encryptPrincipal(sb.toString())
        def domainuser = userRepository.findByUsername(username)
        Token token = new Token(user: domainuser,
                key: tokenCredentials,
                generated: new Date(System.currentTimeMillis()))
        tokenRepository.saveAndFlush(token)
        return tokenCredentials
    }

    /**
     * retrieve authentication from request header
     * @param request incoming http request
     * @return authetication information
     */
    Authentication getAuthentication(HttpServletRequest request) {
        String jwsToken = request.getHeader(HEADER_STRING);
        if (jwsToken != null) {
            // parse the jwsToken.
            try {
                def body = Jwts.parser()
                        .setSigningKey(SECRET)
                        .parseClaimsJws(jwsToken.replace(TOKEN_PREFIX, ""))
                        .getBody()
                def user = body.getSubject();
                def credentials = body.get('credentials', String.class)
                if (credentials) {
                    def decryptedCredentials = decryptCredentials(credentials)
                    if (tokenRepository.keyExists(credentials)) {
                        def credinfo = decryptedCredentials.split(/,/)
                        def auths = credinfo[1..-1].collect {new SimpleGrantedAuthority(it)}
                        return new TokenAuthentication(
                                username: credinfo[0],
                                credentials: credentials,
                                authorities: auths)
                    }
                    log.info("invalid token")
                }
            } catch (Exception ex) {
                log.info("invalid token caused exception: $ex")
            }
        }
        return null;
    }
}
