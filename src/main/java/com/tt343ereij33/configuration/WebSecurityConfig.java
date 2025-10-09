package com.tt343ereij33.configuration;

import com.tt343ereij33.dto.ErrorResponse;
import com.tt343ereij33.security.jwt.JwtTokenFilter;
import com.tt343ereij33.security.jwt.JwtTokenProvider;
import com.tt343ereij33.security.oauth2.OAuth2FailureHandler;
import com.tt343ereij33.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GITHUB;
import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GOOGLE;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@ComponentScan("com.tt343ereij33")
public class WebSecurityConfig {
    private JwtTokenProvider jwtTokenProvider;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private static final List<CommonOAuth2Provider> OAUTH2_PROVIDERS = List.of(GOOGLE, GITHUB);
    private static final String CLIENT_PROPERTY_KEY_PREFIX = "OAUTH2_CLIENT_REGISTRATION_";
    private static final String CLIENT_ID_PROPERTY_KEY_SUFFIX = "_CLIENT_ID";
    private static final String CLIENT_SECRET_PROPERTY_KEY_SUFFIX = "_CLIENT_SECRET";
    private static final Set<String> PERMIT_ALL_ENDPOINTS = Set.of(
            "/auth/**", "/oauth2/**", "/login/**"
    );

    // TODO: Resolve circular dependency and remove setter
    @Autowired
    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(PERMIT_ALL_ENDPOINTS.toArray(new String[0]))
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth -> oauth
                        .clientRegistrationRepository(clientRegistrationRepository())
                        .authorizedClientService(authorizedClientService())
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.NOT_FOUND.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(ErrorResponse.buildResponse(HttpStatus.NOT_FOUND,
                                    "The requested endpoint does not exist.", request.getServletPath()));
                        })
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        } else {
            configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Refresh-Token", "User-Agent"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "X-Refresh-Token", "User-Agent"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(15);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = Stream.of(OAUTH2_PROVIDERS.toArray(new CommonOAuth2Provider[0]))
                .map(this::getRegistration)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private Optional<ClientRegistration> getRegistration(CommonOAuth2Provider provider) {
        if (isKnownClient(provider)) {
            return Optional.of(getClientBuilder(provider)
                    .clientId(System.getenv(CLIENT_PROPERTY_KEY_PREFIX + provider + CLIENT_ID_PROPERTY_KEY_SUFFIX))
                    .clientSecret(System.getenv(CLIENT_PROPERTY_KEY_PREFIX + provider + CLIENT_SECRET_PROPERTY_KEY_SUFFIX))
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .build());
        }
        return Optional.empty();
    }

    private boolean isKnownClient(CommonOAuth2Provider provider) {
        return OAUTH2_PROVIDERS.contains(provider);
    }

    private ClientRegistration.Builder getClientBuilder(CommonOAuth2Provider provider) {
        return switch (provider) {
            case GOOGLE -> CommonOAuth2Provider.GOOGLE.getBuilder(provider.name());
            case GITHUB -> CommonOAuth2Provider.GITHUB.getBuilder(provider.name());
            case FACEBOOK, OKTA -> throw new RuntimeException("[FACEBOOK, OKTA] OAuth2 clients not implemented yet");
        };
    }
}