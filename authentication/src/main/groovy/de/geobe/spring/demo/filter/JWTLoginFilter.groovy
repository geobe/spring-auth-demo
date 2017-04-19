package de.geobe.spring.demo.filter

import com.fasterxml.jackson.databind.ObjectMapper
import de.geobe.spring.demo.service.TokenAuthenticationService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.SpringBeanAutowiringSupport
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.FilterChain
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by georg beier on 19.04.2017.
 * based on article
 * @see <a href="https://auth0.com/blog/securing-spring-boot-with-jwts/>
 * "Securing Spring Boot with JWTs" </a>
 */
@Slf4j
class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {
//    @Autowired
    TokenAuthenticationService tokenAuthenticationService

    public JWTLoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url))
        setAuthenticationManager(authManager)
    }

    @Override
    void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
//        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        if(tokenAuthenticationService==null){
            ServletContext servletContext = req.getServletContext();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            tokenAuthenticationService =
                    webApplicationContext.getBean(TokenAuthenticationService.class);
            log.info("tokenAuthenticationService loaded")
        }
        super.doFilter(req, res, chain)
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        try {
            AccountCredentials creds = new ObjectMapper()
                    .readValue(request.getInputStream(), AccountCredentials.class);
            log.info("Credentials: $creds")
            def authentication = getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.username,
                            creds.password,
                            Collections.emptyList()
                    )
            )
            log.info("Authentication: $authentication")
            return authentication
        } catch (Exception ex) {
            response.setStatus(HttpStatus.NOT_ACCEPTABLE.value())
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest req,
            HttpServletResponse res, FilterChain chain,
            Authentication auth) throws IOException, ServletException {
        tokenAuthenticationService
                .addAuthentication(res, auth);
    }
}

class AccountCredentials {
    String username;
    String password;
}
