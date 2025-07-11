package com.tt343ereij33.configuration;

import com.tt343ereij33.security.jwt.JwtTokenFilter;
import com.tt343ereij33.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;

/**
 * Configures Spring Security for the web application.
 *
 * <p>This class enables Spring Security by using the {@code @EnableWebSecurity} annotation
 * and defines security-related configurations, such as authentication, authorization,
 * password encoding, and security filters for HTTP requests.
 *
 * <p>It is loaded in the root application context via {@code getRootConfigClasses()} in
 * {@code WebApplicationInitializer} to ensure that security configurations apply globally
 * across the application, including the DispatcherServlet and static resources.
 *
 * <p>Key responsibilities:
 * <ul>
 *     <li>Disables CSRF and HTTP Basic as JWT is used for authentication.</li>
 *     <li>Enforces stateless session management.</li>
 *     <li>Defines public and secured endpoints with role-based access control.</li>
 *     <li>Adds a JWT filter to validate tokens on incoming requests.</li>
 *     <li>Configures a password encoder (BCrypt) for user password handling.</li>
 *     <li>Exposes an {@link AuthenticationManager} for authentication workflows.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan("com.tt343ereij33")
public class WebSecurityConfig {
    private JwtTokenProvider jwtTokenProvider;
    private static final Set<String> PERMIT_ALL_ENDPOINTS = Set.of(
            "/auth/**", "/public/**"
    );

    @Autowired
    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(PERMIT_ALL_ENDPOINTS.toArray(new String[0]))
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/private/user")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/private/admin")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }
}