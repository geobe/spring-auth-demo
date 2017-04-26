package de.geobe.spring.demo.security

import de.geobe.spring.demo.filter.JWTAuthenticationFilter
import de.geobe.spring.demo.filter.JWTLoginFilter
import de.geobe.spring.demo.repository.TokenRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ProviderManager
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RegexRequestMatcher

import java.security.SecureRandom

/**
 * Created by georg beier on 17.04.2017.
 */
@Configuration
@EnableGlobalAuthentication
@EnableWebSecurity(debug = false)
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsManager userDetailsManager;
    @Autowired
    private TokenRepository tokenRepository

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        SecureRandom random = new SecureRandom();
        return new BCryptPasswordEncoder(12, random);
    }

    @Bean
    @Autowired
    public TextEncryptor textEncryptor(@Value('${geobe.jwt.sharedkey}') String pw,
                                       @Value('${geobe.jwt.sharedsalt}') String salt) {
        return Encryptors.delux(pw, salt)
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsManager).passwordEncoder(bCryptPasswordEncoder());
        if (!userDetailsManager.userExists("admin")) {
            User.UserBuilder builder = User.withUsername("admin");
            builder.password("admin");
            builder.roles("ADMIN", "USER", "DEFAULT");
            userDetailsManager.createUser(builder.build());
        }
//        tokenRepository.deleteAll()
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers('/r**').hasAnyRole('ADMIN')
                .antMatchers('/admin/**').hasAnyRole('ADMIN')
                .antMatchers("/info").permitAll()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .anyRequest().authenticated()
                .and()
        // We filter the api/login requests
                .addFilterBefore(new JWTLoginFilter("/login", authenticationManager()),
                UsernamePasswordAuthenticationFilter.class)
        // And filter other requests to check the presence of JWT in header
                .addFilterBefore(new JWTAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class);
        def autman = authenticationManager()
        if (autman instanceof ProviderManager) {
            ProviderManager pm = (ProviderManager) autman;
            pm.providers.add(new TokenAuthenticationProvider())
        }
    }

}
