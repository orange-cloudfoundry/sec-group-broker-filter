package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author Sebastien Bortolussi
 */
public class TrustedDestinationSpecificationTest {

    @Test
    public void destination_in_plain_host_format_should_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableIPV4Address.of("127.0.0.1"))
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("http://localhost:3128"));
        Assertions.assertThat(satisfiedBy).isTrue();
    }

    @Test
    public void in_cidr_destination_host_should_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8000)));
        Assertions.assertThat(satisfiedBy).isTrue();
    }

    @Test
    public void in_range_destination_host_should_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableIPRange.builder().from(ImmutableIPV4Address.of("192.168.0.1")).to(ImmutableIPV4Address.of("192.168.0.2")).build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8000)));
        Assertions.assertThat(satisfiedBy).isTrue();
    }

    @Test
    public void out_of_cidr_destination_host_should_not_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29")).build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.10", ImmutablePort.of(8000)));
        Assertions.assertThat(satisfiedBy).isFalse();
    }

    @Test
    public void out_of_range_destination_host_should_not_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableIPRange.builder().from(ImmutableIPV4Address.of("192.168.0.1")).to(ImmutableIPV4Address.of("192.168.0.2")).build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.10", ImmutablePort.of(8000)));
        Assertions.assertThat(satisfiedBy).isFalse();
    }

    @Test
    public void in_range_destination_port_should_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePortRange.builder()
                                .from(ImmutablePort.of(8000))
                                .to(ImmutablePort.of(8009))
                                .build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8000)));
        Assertions.assertThat(satisfiedBy).isTrue();
    }


    @Test
    public void out_of_range_destination_port_should_not_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePortRange.builder()
                                .from(ImmutablePort.of(8000))
                                .to(ImmutablePort.of(8009))
                                .build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8010)));
        Assertions.assertThat(satisfiedBy).isFalse();
    }

    @Test
    public void in_list_destination_port_should_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePorts.builder()
                                .addValue(ImmutablePort.of(8000))
                                .addValue(ImmutablePort.of(8001))
                                .build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8000)));
        Assertions.assertThat(satisfiedBy).isTrue();
    }

    @Test
    public void out_of_list_destination_port_should_satisfy_specification() throws Exception {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePorts.builder()
                                .addValue(ImmutablePort.of(8000))
                                .addValue(ImmutablePort.of(8001))
                                .build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8010)));
        Assertions.assertThat(satisfiedBy).isFalse();
    }

}