package com.tt343ereij33.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Initializes the Spring Web Application by configuring the root and servlet application contexts.
 *
 * <p>This class extends {@link AbstractAnnotationConfigDispatcherServletInitializer} to
 * programmatically configure the {@link org.springframework.web.servlet.DispatcherServlet} and the application contexts
 * without the need for a {@code web.xml} file.
 *
 * <p>It specifies:
 * <ul>
 *   <li>The root application context configuration classes via {@link #getRootConfigClasses()},
 *       which typically contain service, repository, and security beans.</li>
 *   <li>The servlet application context configuration classes via {@link #getServletConfigClasses()},
 *       which usually contain Spring MVC components such as controllers, view resolvers, and handler mappings.</li>
 *   <li>The servlet mapping patterns via {@link #getServletMappings()}, defining the URL
 *       patterns the {@code DispatcherServlet} will handle (in this case, {@code "/"})</li>
 * </ul>
 *
 * <p>By separating configuration in root and servlet contexts, this class helps organize
 * and modularize the Spring application architecture.
 */
public class WebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {ApplicationConfig.class, WebSecurityConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {WebMvcConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] {"/"};
    }
}