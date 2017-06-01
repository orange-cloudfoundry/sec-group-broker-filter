package com.orange.cloud.servicebroker.filter.securitygroups.config;

import com.orange.cloud.servicebroker.filter.securitygroups.domain.*;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;

/**
 * @author Sebastien Bortolussi
 */
public class SpecificationConfigTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
            if (this.context.getParent() != null) {
                ((ConfigurableApplicationContext) this.context.getParent()).close();
            }
        }
    }

    @Test
    public void default_trusted_destination_config() {
        MockEnvironment env = new MockEnvironment();
        this.context = new AnnotationConfigApplicationContext();
        this.context.setEnvironment(env);
        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class, TrustedDestinationConfig.class);
        this.context.refresh();
        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);

        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder().hosts(ImmutableCIDR.of("0.0.0.0/0")).build());
    }

    @Test
    public void trusted_destination_config_with_cidr() {
        MockEnvironment env = new MockEnvironment();
        this.context = new AnnotationConfigApplicationContext();
        env.setProperty("broker.filter.trusted.destination.hosts", "192.168.0.1/29");
        this.context.setEnvironment(env);
        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class);
        this.context.refresh();
        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);

        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder()
                .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                .build());
    }

    @Test
    public void trusted_destination_config_with_single_ip() {
        MockEnvironment env = new MockEnvironment();
        this.context = new AnnotationConfigApplicationContext();
        env.setProperty("broker.filter.trusted.destination.hosts", "192.168.0.1");
        this.context.setEnvironment(env);
        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class);
        this.context.refresh();
        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);

        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder()
                .hosts(ImmutableIPV4Address.of("192.168.0.1"))
                .build());
    }

    @Test
    public void trusted_destination_config_with_ip_range() {
        MockEnvironment env = new MockEnvironment();
        this.context = new AnnotationConfigApplicationContext();
        env.setProperty("broker.filter.trusted.destination.hosts", "192.168.0.1-192.168.0.9");
        this.context.setEnvironment(env);
        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class);
        this.context.refresh();
        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);

        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder()
                .hosts(ImmutableIPRange.builder().from(ImmutableIPV4Address.of("192.168.0.1")).to(ImmutableIPV4Address.of("192.168.0.9")).build())
                .build());
    }

    @Test
    public void trusted_destination_config_with_cidr_and_port_range() {
        MockEnvironment env = new MockEnvironment();
        this.context = new AnnotationConfigApplicationContext();
        env.setProperty("broker.filter.trusted.destination.hosts", "192.168.0.1/29");
        env.setProperty("broker.filter.trusted.destination.ports", "3306-3310");
        this.context.setEnvironment(env);
        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class);
        this.context.refresh();
        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);

        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder()
                .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                .ports(ImmutablePortRange.builder().from(ImmutablePort.of(3306)).to(ImmutablePort.of(3310)).build())
                .build());
    }

    @Test
    public void trusted_destination_config_with_cidr_and_port_list() {
        MockEnvironment env = new MockEnvironment();
        this.context = new AnnotationConfigApplicationContext();
        env.setProperty("broker.filter.trusted.destination.hosts", "192.168.0.1/29");
        env.setProperty("broker.filter.trusted.destination.ports", "3306,3310");
        this.context.setEnvironment(env);
        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class);
        this.context.refresh();
        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);

        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder()
                .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                .ports(ImmutablePorts.builder().addValue(3306).addValue(3310).build())
                .build());
    }

    @Test
    public void trusted_destination_config_with_cidr_and_single_port() {
        MockEnvironment env = new MockEnvironment();
        this.context = new AnnotationConfigApplicationContext();
        env.setProperty("broker.filter.trusted.destination.hosts", "192.168.0.1/29");
        env.setProperty("broker.filter.trusted.destination.ports", "3306");
        this.context.setEnvironment(env);
        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class);
        this.context.refresh();
        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);

        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder()
                .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                .ports(ImmutablePorts.builder().addValue(3306).build())
                .build());
    }

}