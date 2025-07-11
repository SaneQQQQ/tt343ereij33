package com.tt343ereij33.configuration;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * SecurityWebApplicationInitializer initializes Spring Security for the web application
 * by registering the springSecurityFilterChain with the Servlet container.
 * <p>
 * By extending {@link org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer},
 * this class automatically integrates Spring Security's filter chain without requiring explicit
 * filter registration in web.xml or additional configuration.
 * <p>
 */
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {}