package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Sebastien Bortolussi
 */
public class IPRangeTest {

    @Test
    public void invalid_range() throws Exception {
        Throwable thrown = catchThrowable(() -> {
                    ImmutableIPRange.builder()
                            .from(ImmutableIPV4Address.of("127.0.0.10"))
                            .to(ImmutableIPV4Address.of("127.0.0.0"))
                            .build();
                }
        );
        assertThat(thrown).hasMessageContaining("Invalid range. IPV4Address{value=127.0.0.0} should be greater or equals to IPV4Address{value=127.0.0.10}");
    }

    @Test
    public void isInRange() throws Exception {
        final IPRange IPRange = ImmutableIPRange.builder()
                .from(ImmutableIPV4Address.of("127.0.0.1"))
                .to(ImmutableIPV4Address.of("127.0.0.3"))
                .build();
        Assertions.assertThat(IPRange.isInRange(ImmutableIPV4Address.of("127.0.0.1"))).isTrue();
        Assertions.assertThat(IPRange.isInRange(ImmutableIPV4Address.of("127.0.0.2"))).isTrue();
        Assertions.assertThat(IPRange.isInRange(ImmutableIPV4Address.of("127.0.0.3"))).isTrue();

        Assertions.assertThat(IPRange.isInRange(ImmutableIPV4Address.of("127.0.0.0"))).isFalse();
    }

}