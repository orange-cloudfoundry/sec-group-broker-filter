package com.orange.cloud.servicebroker.filter.securitygroups.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * This complements application.yml reading user/password from env vars and disable crsf.
 * See https://docs.spring.io/spring-cloud-open-service-broker/docs/current/reference/html5/#example-configuration
 * and https://github.com/spring-cloud/spring-cloud-open-service-broker/pull/273
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig  extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.authorizeRequests()
			.antMatchers("/v2/**").authenticated()
			.and()
			.httpBasic();
	}

}
