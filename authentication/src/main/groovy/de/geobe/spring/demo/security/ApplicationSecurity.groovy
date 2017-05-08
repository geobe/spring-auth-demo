package de.geobe.spring.demo.security

import de.geobe.spring.demo.filter.JWTAuthenticationFilter
import de.geobe.spring.demo.filter.JWTLoginFilter
import de.geobe.spring.demo.filter.JWTLogoutSuccessHandler
import de.geobe.spring.demo.repository.TokenRepository
import de.geobe.spring.demo.service.TokenService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.access.vote.RoleVoter
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Created by georg beier on 17.04.2017.
 */
@Slf4j
@Configuration
@EnableGlobalAuthentication
@EnableWebSecurity(debug = false)
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsManager userDetailsManager;
    @Autowired
    private TokenRepository tokenRepository
    @Autowired
    TokenService tokenService
    @Autowired
    private BCryptPasswordEncoder passwordEncoder
    @Autowired
    ApplicationContext context


    @Bean
    @Autowired
    public TextEncryptor textEncryptor(@Value('${geobe.jwt.sharedkey}') String pw,
                                       @Value('${geobe.jwt.sharedsalt}') String salt) {
        return Encryptors.delux(pw, salt)
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsManager).passwordEncoder(passwordEncoder);
        if (!userDetailsManager.userExists("admin")) {
            log.info("creating manager")
            User.UserBuilder builder = User.withUsername("admin");
            builder.password("admin");
            builder.roles("ADMIN", "USER", "DEFAULT");
            userDetailsManager.createUser(builder.build());
        }
//        tokenRepository.deleteAll()
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        def logoutSuccessHandler = new JWTLogoutSuccessHandler(tokenRepository, tokenService)
        http.csrf().disable().authorizeRequests()
                .antMatchers("/info").permitAll()
                .antMatchers('/logout').authenticated()
                .antMatchers('/r**').hasAnyRole('ADMIN')
                .antMatchers('/admin/jwts/changepassword').fullyAuthenticated()
                .antMatchers('/admin/**').hasAnyRole('ADMIN')
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
        // We filter the api/login requests
                .addFilterBefore(new JWTLoginFilter("/login", authenticationManager()),
                UsernamePasswordAuthenticationFilter.class)
        // And filter other requests to check the presence of JWT in header
                .addFilterBefore(new JWTAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class);
//        http.anonymous().disable()
//        def autman = authenticationManager()
    }

}
