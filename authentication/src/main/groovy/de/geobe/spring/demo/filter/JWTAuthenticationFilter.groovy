package de.geobe.spring.demo.filter

import de.geobe.spring.demo.service.TokenAuthenticationService
import groovy.util.logging.Slf4j
import org.eclipse.jetty.client.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.SpringBeanAutowiringSupport
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by georg beier on 19.04.2017.
 */
@Slf4j
public class JWTAuthenticationFilter extends GenericFilterBean {
//    @Autowired
    TokenAuthenticationService tokenAuthenticationService

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain)
            throws IOException, ServletException {
            log.info("JWTAuthenticationFilter executing")
        if(tokenAuthenticationService==null) {
            ServletContext servletContext = request.getServletContext();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.
                    getWebApplicationContext(servletContext);
            tokenAuthenticationService =
                    webApplicationContext.getBean(TokenAuthenticationService.class);
        }
        Authentication authentication =
                tokenAuthenticationService.getAuthentication((HttpServletRequest) request)
        if (authentication?.authenticated) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            ((HttpServletResponse) response).setStatus(HttpStatus.OK.value())
        } else {
            ((HttpServletResponse) response).setStatus(HttpStatus.UNAUTHORIZED.value())
        }
        filterChain.doFilter(request, response);
    }
}
