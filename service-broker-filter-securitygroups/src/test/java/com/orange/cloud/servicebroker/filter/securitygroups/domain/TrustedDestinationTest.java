package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Sebastien Bortolussi
 */
public class TrustedDestinationTest {

    @Test
    public void hosts_is_mandatory() throws Exception {
        Throwable thrown = catchThrowable(() -> {
                    ImmutableTrustedDestination.builder().build();
                }
        );
        assertThat(thrown).hasMessageContaining("Cannot build TrustedDestination, some of required attributes are not set [hosts]");
    }

    @Test
    public void with_cidr_for_host() throws Exception {
        final ImmutableTrustedDestination destination = ImmutableTrustedDestination.builder().hosts(ImmutableCIDR.of("192.168.0.1/29")).build();
        Assertions.assertThat(destination.hosts()).isEqualTo(ImmutableCIDR.of("192.168.0.1/29"));
    }

    @Test
    public void with_single_ip_for_host() throws Exception {
        final ImmutableTrustedDestination destination = ImmutableTrustedDestination.builder().hosts(ImmutableIPV4Address.of("192.168.0.1")).build();
        Assertions.assertThat(destination.hosts()).isEqualTo(ImmutableIPV4Address.of("192.168.0.1"));
    }

    @Test
    public void with_ip_range_for_host() throws Exception {
        final ImmutableTrustedDestination destination = ImmutableTrustedDestination.builder()
                .hosts(ImmutableIPRange.builder()
                        .from(ImmutableIPV4Address.of("192.168.0.1"))
                        .to(ImmutableIPV4Address.of("192.168.0.7"))
                        .build())
                .build();
        Assertions.assertThat(destination.hosts()).isEqualTo(ImmutableIPRange.builder()
                .from(ImmutableIPV4Address.of("192.168.0.1"))
                .to(ImmutableIPV4Address.of("192.168.0.7"))
                .build());
    }

    @Test
    public void with_ports() throws Exception {
        ImmutableTrustedDestination.builder()
                .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                .ports(ImmutablePorts.builder()
                        .addValue(ImmutablePort.of(3306))
                        .build())
                .build();
    }

    @Test
    public void with_port_range() throws Exception {
        ImmutableTrustedDestination.builder()
                .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                .ports(ImmutablePortRange.builder().from(ImmutablePort.of(3306)).to(ImmutablePort.of(3310)).build())
                .build();
    }

}