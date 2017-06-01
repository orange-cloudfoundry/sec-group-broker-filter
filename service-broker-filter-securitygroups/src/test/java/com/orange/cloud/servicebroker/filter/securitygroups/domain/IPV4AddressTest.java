package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Sebastien Bortolussi
 */
public class IPV4AddressTest {

    @Test
    public void invalid_ip_address() throws Exception {
        Throwable thrown = catchThrowable(() -> {
                    ImmutableIPV4Address.of("1234");
                }
        );
        assertThat(thrown).hasMessageContaining("Invalid IP address : 1234");
    }

    @Test
    public void valid_ip_address() throws Exception {
        ImmutableIPV4Address.of("127.0.0.1");
    }

    @Test
    public void greater_than() throws Exception {
        assertThat(ImmutableIPV4Address.of("127.0.0.1").greaterOrEqualsTo(ImmutableIPV4Address.of("127.0.0.1"))).isTrue();
        assertThat(ImmutableIPV4Address.of("127.0.0.1").greaterOrEqualsTo(ImmutableIPV4Address.of("127.0.0.0"))).isTrue();

        assertThat(ImmutableIPV4Address.of("127.0.0.1").greaterOrEqualsTo(ImmutableIPV4Address.of("127.0.0.2"))).isFalse();
    }

    @Test
    public void less_than() throws Exception {
        assertThat(ImmutableIPV4Address.of("127.0.0.1").lessOrEqualsTo(ImmutableIPV4Address.of("127.0.0.1"))).isTrue();
        assertThat(ImmutableIPV4Address.of("127.0.0.1").lessOrEqualsTo(ImmutableIPV4Address.of("127.0.0.2"))).isTrue();

        assertThat(ImmutableIPV4Address.of("127.0.0.1").lessOrEqualsTo(ImmutableIPV4Address.of("127.0.0.0"))).isFalse();
    }


    @Test
    public void in_range() throws Exception {
        final Range<IPV4Address> range = ImmutableIPV4Address.of("127.0.0.1");
        Assertions.assertThat(range.isInRange(ImmutableIPV4Address.of("127.0.0.1"))).isTrue();
        Assertions.assertThat(range.isInRange(ImmutableIPV4Address.of("127.0.0.2"))).isFalse();
        Assertions.assertThat(range.isInRange(ImmutableIPV4Address.of("127.0.0.3"))).isFalse();
    }

}