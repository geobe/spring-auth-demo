package de.geobe.spring.auth.filter

import de.geobe.spring.auth.repository.TokenRepository
import de.geobe.spring.auth.util.TokenService
import groovy.util.logging.Slf4j
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler


import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by georg beier on 03.05.2017.
 */
@Slf4j
class JWTLogoutSuccessHandler extends HttpStatusReturningLogoutSuccessHandler {

    private TokenRepository tokenRepository
    private TokenService tokenService

    JWTLogoutSuccessHandler(TokenRepository tokenRepository, TokenService tokenService) {
        this.tokenRepository = tokenRepository
        this.tokenService = tokenService
    }

    @Override
    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                         Authentication authentication) throws IOException, ServletException {
        def jwsToken = request.getHeader(TokenService.HEADER_STRING)
        if (jwsToken) {
            try {
                def content = tokenService.parseToken(jwsToken)
                def credentials = content.credentials
                def t = tokenRepository.findByKey(credentials)
                if(t)
                    tokenRepository.delete(t)
            } catch (Exception ex) {
                log.info("exception in LogoutSuccessHandler: $ex")
            }
        }
        super.onLogoutSuccess(request, response, authentication)
    }

}
