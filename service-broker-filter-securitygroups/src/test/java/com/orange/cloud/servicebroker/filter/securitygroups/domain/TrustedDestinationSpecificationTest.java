package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastien Bortolussi
 */
public class TrustedDestinationSpecificationTest {

    @Test
    public void in_cidr_destination_host_should_satisfy_specification() {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8000)));
        assertThat(satisfiedBy).isTrue();
    }

    @Test
    public void in_cidr_destination_fqdn_host_should_satisfy_specification() throws UnknownHostException {
        //given localhost resolves only to 127.0.0.1
        assertThat(InetAddress.getAllByName("localhost")).containsExactly(
            InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("127.0.0.1/28"))
                        .build());

        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("localhost", ImmutablePort.of(8000)));
        assertThat(satisfiedBy).isTrue();
    }

    @Test
    public void in_range_destination_host_should_satisfy_specification() {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableIPRange.builder().from(ImmutableIPAddress.of("192.168.0.1")).to(ImmutableIPAddress.of("192.168.0.2")).build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8000)));
        assertThat(satisfiedBy).isTrue();
    }

    @Test
    public void out_of_cidr_destination_host_should_not_satisfy_specification() {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29")).build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.10", ImmutablePort.of(8000)));
        assertThat(satisfiedBy).isFalse();
    }

    @Test
    public void out_of_cidr_destination_host_fqdn_should_not_satisfy_specification() throws UnknownHostException {
        //given localhost resolves only to 127.0.0.1
        assertThat(InetAddress.getAllByName("localhost")).containsExactly(
            InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));

        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29")).build());
        //when
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("localhost",
            ImmutablePort.of(8000))); //assuming localhost resolves to 127.0.0.1
        //then '
        assertThat(satisfiedBy).isFalse();
    }

    @Test
    public void out_of_range_destination_host_should_not_satisfy_specification() {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableIPRange.builder().from(ImmutableIPAddress.of("192.168.0.1")).to(ImmutableIPAddress.of("192.168.0.2")).build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.10", ImmutablePort.of(8000)));
        assertThat(satisfiedBy).isFalse();
    }

    @Test
    public void in_range_destination_port_should_satisfy_specification() {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePortRange.builder()
                                .from(ImmutablePort.of(8000))
                                .to(ImmutablePort.of(8009))
                                .build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8000)));
        assertThat(satisfiedBy).isTrue();
    }


    @Test
    public void out_of_range_destination_port_should_not_satisfy_specification() {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePortRange.builder()
                                .from(ImmutablePort.of(8000))
                                .to(ImmutablePort.of(8009))
                                .build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8010)));
        assertThat(satisfiedBy).isFalse();
    }

    @Test
    public void in_list_destination_port_should_satisfy_specification() {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePorts.builder()
                                .addValue(ImmutablePort.of(8000))
                                .addValue(ImmutablePort.of(8001))
                                .build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8000)));
        assertThat(satisfiedBy).isTrue();
    }

    @Test
    public void out_of_list_destination_port_should_satisfy_specification() {
        final TrustedDestinationSpecification trustedDestinationSpecification = new TrustedDestinationSpecification(
                ImmutableTrustedDestination.builder()
                        .hosts(ImmutableCIDR.of("192.168.0.1/29"))
                        .ports(ImmutablePorts.builder()
                                .addValue(ImmutablePort.of(8000))
                                .addValue(ImmutablePort.of(8001))
                                .build())
                        .build());
        final boolean satisfiedBy = trustedDestinationSpecification.isSatisfiedBy(new Destination("192.168.0.1", ImmutablePort.of(8010)));
        assertThat(satisfiedBy).isFalse();
    }

}