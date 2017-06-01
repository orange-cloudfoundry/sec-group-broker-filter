package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Sebastien Bortolussi
 */
public class CIDRTest {

    @Test
    public void invalid_cidr() throws Exception {
        Throwable thrown = catchThrowable(() -> {
                    ImmutableCIDR.builder()
                            .value("1234")
                            .build();
                }
        );
        assertThat(thrown).hasMessageContaining("Invalid CIDR block : 1234");
    }

    @Test
    public void valid_cidr() throws Exception {
        ImmutableCIDR.builder()
                .value("192.168.0.1/24")
                .build();
    }

    @Test
    public void in_range() throws Exception {
        final Range<IPV4Address> range = ImmutableCIDR.builder().value("192.168.0.1/29").build();
        Assertions.assertThat(range.isInRange(ImmutableIPV4Address.of("192.168.0.1"))).isTrue();
        Assertions.assertThat(range.isInRange(ImmutableIPV4Address.of("192.168.0.6"))).isTrue();
        Assertions.assertThat(range.isInRange(ImmutableIPV4Address.of("192.168.1.0"))).isFalse();
        Assertions.assertThat(range.isInRange(ImmutableIPV4Address.of("127.0.0.3"))).isFalse();
    }

}