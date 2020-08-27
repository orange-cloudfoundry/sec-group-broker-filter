package com.orange.cloud.servicebroker.filter.securitygroups.config;

import com.orange.cloud.servicebroker.filter.securitygroups.domain.ImmutableCIDR;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.ImmutableIPAddress;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.ImmutableIPRange;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.ImmutablePort;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.ImmutablePortRange;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.ImmutablePorts;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.ImmutableTrustedDestination;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.TrustedDestination;
import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastien Bortolussi
 */
public class SpecificationConfigTest {

//    private AnnotationConfigApplicationContext context;
//See https://docs.spring.io/spring-boot/docs/2.1.12.RELEASE/reference/html/boot-features-developing-auto-configuration.html#boot-features-test-autoconfig
ConditionEvaluationReportLoggingListener conditionEvaluationReportLoggingListener = new ConditionEvaluationReportLoggingListener(
    LogLevel.INFO);

    @Test
    public void default_trusted_destination_config() {
        //noinspection unused
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class, TrustedDestinationConfig.class))
            .run((context) -> {
                assertThat(context)
                    .getBean(TrustedDestination.class)
                    .isEqualTo(ImmutableTrustedDestination.builder().hosts(ImmutableCIDR.of("0.0.0.0/0")).build());
            });
    }

    @Test
    public void trusted_destination_config_with_cidr() {
        //noinspection unused
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(conditionEvaluationReportLoggingListener)
            .withPropertyValues("broker.filter.trusted.destination.hosts=192.168.0.1/29")
            .withConfiguration(AutoConfigurations.of(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class))
            .run((context) -> {
                assertThat(context)
                    .getBean(TrustedDestination.class)
                    .isEqualTo(ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .build());
            });
    }

    @Test
    public void trusted_destination_config_with_single_ip() {
        //noinspection unused
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(conditionEvaluationReportLoggingListener)
            .withPropertyValues("broker.filter.trusted.destination.hosts=192.168.0.1")
            .withConfiguration(AutoConfigurations.of(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class))
            .run((context) -> {
                assertThat(context)
                    .getBean(TrustedDestination.class)
                    .isEqualTo(ImmutableTrustedDestination.builder()
                        .hosts(ImmutableIPAddress.of("192.168.0.1"))
                        .build());
            });
    }

    @Test
    public void trusted_destination_config_with_ip_range() {
        //noinspection unused
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(conditionEvaluationReportLoggingListener)
            .withPropertyValues("broker.filter.trusted.destination.hosts=192.168.0.1-192.168.0.9")
            .withConfiguration(AutoConfigurations.of(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class))
            .run((context) -> {
                assertThat(context)
                    .getBean(TrustedDestination.class)
                    .isEqualTo(ImmutableTrustedDestination.builder()
                        .hosts(ImmutableIPRange.builder().from(ImmutableIPAddress.of("192.168.0.1")).to(ImmutableIPAddress.of("192.168.0.9")).build())
                        .build());
            });
    }

    @Test
    public void trusted_destination_config_with_cidr_and_port_range() {
        //noinspection unused
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(conditionEvaluationReportLoggingListener)
            .withPropertyValues(
                "broker.filter.trusted.destination.hosts=192.168.0.1/29",
                "broker.filter.trusted.destination.ports=3306-3310")
            .withConfiguration(AutoConfigurations.of(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class))
            .run((context) -> {
                assertThat(context)
                    .getBean(TrustedDestination.class)
                    .isEqualTo(ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePortRange.builder().from(ImmutablePort.of(3306)).to(ImmutablePort.of(3310)).build())
                        .build());
            });
    }

    @Test
    public void trusted_destination_config_with_cidr_and_port_list() {
        //noinspection unused
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(conditionEvaluationReportLoggingListener)
            .withPropertyValues(
                "broker.filter.trusted.destination.hosts=192.168.0.1/29",
                "broker.filter.trusted.destination.ports=3306,3310")
            .withConfiguration(AutoConfigurations.of(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class))
            .run((context) -> {
                assertThat(context)
                    .getBean(TrustedDestination.class)
                    .isEqualTo(ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                            .ports(ImmutablePorts.builder().addValue(3306).addValue(3310).build())
                            .build());
            });


//        MockEnvironment env = new MockEnvironment();
//        this.context = new AnnotationConfigApplicationContext();
//        env.setProperty("broker.filter.trusted.destination.hosts", "192.168.0.1/29");
//        env.setProperty("broker.filter.trusted.destination.ports", "3306,3310");
//        this.context.setEnvironment(env);
//        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class);
//        this.context.refresh();
//        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);
//
//        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder()
//                .hosts(ImmutableCIDR.of("192.168.0.1/29"))
//                .ports(ImmutablePorts.builder().addValue(3306).addValue(3310).build())
//                .build());
    }

    @Test
    public void trusted_destination_config_with_cidr_and_single_port() {
        //noinspection unused
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(conditionEvaluationReportLoggingListener)
            .withPropertyValues(
                "broker.filter.trusted.destination.hosts=192.168.0.1/29",
                "broker.filter.trusted.destination.ports=3306")
            .withConfiguration(AutoConfigurations.of(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class))
            .run((context) -> {
                assertThat(context)
                    .getBean(TrustedDestination.class)
                    .isEqualTo(ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePorts.builder().addValue(3306).build())
                        .build());
            });

//        MockEnvironment env = new MockEnvironment();
//        this.context = new AnnotationConfigApplicationContext();
//        env.setProperty("broker.filter.trusted.destination.hosts", "192.168.0.1/29");
//        env.setProperty("broker.filter.trusted.destination.ports", "3306");
//        this.context.setEnvironment(env);
//        this.context.register(SpecificationConfig.DefaultSpecificationConfig.class, SpecificationConfig.AllowedDestinationSpecificationConfig.class);
//        this.context.refresh();
//        final TrustedDestination trustedDestination = this.context.getBean(TrustedDestination.class);
//
//        Assertions.assertThat(trustedDestination).isEqualTo(ImmutableTrustedDestination.builder()
//                .hosts(ImmutableCIDR.of("192.168.0.1/29"))
//                .ports(ImmutablePorts.builder().addValue(3306).build())
//                .build());
    }

}