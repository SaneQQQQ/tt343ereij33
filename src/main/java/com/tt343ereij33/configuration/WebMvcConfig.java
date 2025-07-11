package com.tt343ereij33.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Spring MVC for the web application.
 *
 * <p>This class enables Spring MVC using {@code @EnableWebMvc} and configures MVC-related
 * components such as view resolvers, message converters, resource handlers, and CORS
 * configurations.
 *
 * <p>It also uses {@code @ComponentScan} to scan for controllers and other Spring-managed
 * components within the specified base package.
 *
 * <p>This configuration is loaded in the servlet-specific application context via
 * {@code getServletConfigClasses()} in {@code WebApplicationInitializer}, ensuring
 * it applies only to the DispatcherServlet and its child context.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.tt343ereij33")
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedOrigins(System.getenv("CORS_ALLOWED_ORIGINS"));
    }
}


