package com.orange.cloud.servicebroker.filter.securitygroups.config;

import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

	/**
	 * HTTP tracing requires explicit repository since the default one is not ready for production
	 * See https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.2.0-M3-Release-Notes#actuator-http-trace-and-auditing-are-disabled-by-default
	 * We still enable it for troubleshooting and because OSB
	 */
	@Bean
	public HttpTraceRepository htttpTraceRepository() {
		InMemoryHttpTraceRepository inMemoryHttpTraceRepository = new InMemoryHttpTraceRepository();
		inMemoryHttpTraceRepository.setCapacity(10); //protect our memory by limiting to 10 requests hold in RAM
		return inMemoryHttpTraceRepository;
	}

}
