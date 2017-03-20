package com.orange.cloud.servicebroker.filter.securitygroups.config;

import com.orange.cloud.servicebroker.filter.securitygroups.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sebastien Bortolussi
 */

public class SpecificationConfig {

    @Configuration
    protected static class DefaultSpecificationConfig {

        @Bean
        @ConditionalOnMissingBean
        TrustedDestination trustedDestination() {
            return ImmutableTrustedDestination.builder()
                    .hosts(ImmutableCIDR.builder().value("0.0.0.0/0").build())
                    .build();
        }

        @Bean
        @ConditionalOnMissingBean
        TrustedDestinationSpecification allowedDestinationSpecification(TrustedDestination trustedDestination) {
            return new TrustedDestinationSpecification(trustedDestination);
        }

    }

    @Configuration
    @ConditionalOnProperty(name = "broker.filter.trusted.destination.hosts")
    @EnableConfigurationProperties(TrustedDestinationConfig.class)
    protected static class AllowedDestinationSpecificationConfig {


        @Autowired
        TrustedDestinationConfig destinationConfig;

        @Bean
        TrustedDestination trustedDestination(TrustedDestinationConfig destinationConfig) {
            ImmutableTrustedDestination.Builder builder = ImmutableTrustedDestination.builder();
            if (destinationConfig.getHosts() != null && !destinationConfig.getHosts().isEmpty()) {
                final String[] range = destinationConfig.getHosts().split("-");
                if (range.length == 1) { // a cidr or a single ip address
                    if (range[0].contains("/")) { // a cidr
                        builder.hosts(ImmutableCIDR.builder().value(range[0]).build());
                    } else { // a single ip address
                        builder.hosts(ImmutableIPAddress.builder().value(range[0]).build());
                    }
                } else { // a range of ports
                    builder.hosts(ImmutableIPRange.builder()
                            .from(ImmutableIPAddress.of(range[0]))
                            .to(ImmutableIPAddress.of(range[1]))
                            .build());
                }

            }
            if (destinationConfig.getPorts() != null && !destinationConfig.getPorts().isEmpty()) {
                final String[] range = destinationConfig.getPorts().split("-");
                if (range.length == 1) { // a single port or multiple comma-separated ports
                    builder.ports(ImmutablePorts.of(
                            Stream.of(range[0].split(","))
                                    .map(Integer::parseInt)
                                    .map(ImmutablePort::of)
                                    .collect(Collectors.toList())));
                } else { // a range of ports
                    builder.ports(ImmutablePortRange.builder()
                            .from(ImmutablePort.of(Integer.parseInt(range[0])))
                            .to(ImmutablePort.of(Integer.parseInt(range[1])))
                            .build());
                }

            }
            return builder.build();
        }


        @Bean
        TrustedDestinationSpecification allowedDestinationSpecification(TrustedDestination trustedDestination) {
            return new TrustedDestinationSpecification(trustedDestination);
        }
    }


}
