package com.orange.cloud.servicebroker.filter.core.config;

import com.orange.cloud.servicebroker.filter.core.service.mapper.*;
import org.junit.After;
import org.junit.Test;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastien Bortolussi
 */

public class ServiceOfferingMapperConfigTest {

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();


    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void default_service_offering_mapper() {
        registerAndRefresh(ServiceOfferingMapperConfig.DefaultServiceOfferingMapperConfig.class, ServiceOfferingMapperConfig.SuffixedServiceOfferingMapperConfig.class);
        assertThat(this.context.getBeanNamesForType(DefaultCatalogMapper.class).length).isEqualTo(1);
        assertThat(this.context.getBeanNamesForType(DefaultServiceInstanceRequestMapper.class).length).isEqualTo(1);
        assertThat(this.context.getBeanNamesForType(DefaultServiceInstanceBindingRequestMapper.class).length).isEqualTo(1);
    }

    @Test
    public void suffixed_service_offering_mapper() {
        TestPropertyValues
            .of("broker.filter.serviceoffering.suffix", "-sec")
            .applyTo(this.context);
        registerAndRefresh(ServiceOfferingMapperConfig.DefaultServiceOfferingMapperConfig.class, ServiceOfferingMapperConfig.SuffixedServiceOfferingMapperConfig.class);
        assertThat(this.context.getBeanNamesForType(SuffixedCatalogMapper.class).length).isEqualTo(1);
        assertThat(this.context.getBeanNamesForType(SuffixedServiceInstanceRequestMapper.class).length).isEqualTo(1);
        assertThat(this.context.getBeanNamesForType(SuffixedServiceInstanceBindingRequestMapper.class).length).isEqualTo(1);
    }


    private void registerAndRefresh(Class<?>... annotatedClasses) {
        this.context.register(annotatedClasses);
        this.context.refresh();

    }
}
