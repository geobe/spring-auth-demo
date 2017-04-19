package de.geobe.spring.demo.security

import de.geobe.spring.demo.filter.JWTAuthenticationFilter
import de.geobe.spring.demo.filter.JWTLoginFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import java.security.SecureRandom

/**
 * Created by georg beier on 17.04.2017.
 */
@Configuration
@EnableGlobalAuthentication
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsManager userDetailsManager;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        SecureRandom random = new SecureRandom();
        return new BCryptPasswordEncoder(12, random);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsManager).passwordEncoder(bCryptPasswordEncoder());
        if (! userDetailsManager.userExists("admin")) {
            User.UserBuilder builder = User.withUsername("admin");
            builder.password("admin");
            builder.roles("admin", "user", "default");
            userDetailsManager.createUser(builder.build());
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .antMatchers("/info").authenticated()
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
            ProviderManager pm  = (ProviderManager) autman;
            pm.providers.add(new TokenAuthenticationProvider())
        }
    }

}