package com.orange.cloud.servicebroker.filter.core.config;

import com.orange.cloud.servicebroker.filter.core.service.mapper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sebastien Bortolussi
 */
public class ServiceOfferingMapperConfig {

    @Configuration
    protected static class DefaultServiceOfferingMapperConfig {

        @Bean
        @ConditionalOnMissingBean
        CatalogMapper catalogMapper() {
            return new DefaultCatalogMapper();
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceInstanceRequestMapper serviceInstanceRequestMapper() {
            return new DefaultServiceInstanceRequestMapper();
        }

        @Bean
        @ConditionalOnMissingBean
        ServiceInstanceBindingRequestMapper serviceInstanceBindingRequestMapper() {
            return new DefaultServiceInstanceBindingRequestMapper();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "broker.filter.serviceoffering.suffix")
    protected static class SuffixedServiceOfferingMapperConfig {

        @Bean
        String suffix(@Value("${broker.filter.serviceoffering.suffix}") String suffix) {
            return suffix;
        }

        @Bean
        CatalogMapper catalogMapper(String suffix) {
            return new SuffixedCatalogMapper(suffix);
        }

        @Bean
        ServiceInstanceRequestMapper serviceInstanceRequestMapper(String suffix) {
            return new SuffixedServiceInstanceRequestMapper(suffix);
        }

        @Bean
        ServiceInstanceBindingRequestMapper serviceInstanceBindingRequestMapper(String suffix) {
            return new SuffixedServiceInstanceBindingRequestMapper(suffix);
        }
    }

}
