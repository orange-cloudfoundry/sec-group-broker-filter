package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Sebastien Bortolussi
 */
public class PortRangeTest {

    @Test
    public void invalid_range() throws Exception {
        Throwable thrown = catchThrowable(() -> {
                    ImmutablePortRange.builder().from(ImmutablePort.of(3306)).to(ImmutablePort.of(3301)).build();
                }
        );
        assertThat(thrown).hasMessageContaining("Invalid range. Port{value=3301} should be greater or equals to Port{value=3306}");
    }


    @Test
    public void isInRange() throws Exception {
        final PortRange portRange = ImmutablePortRange.builder().from(ImmutablePort.of(8000)).to(ImmutablePort.of(8002)).build();
        Assertions.assertThat(portRange.isInRange(ImmutablePort.of(8000))).isTrue();
        Assertions.assertThat(portRange.isInRange(ImmutablePort.of(8001))).isTrue();
        Assertions.assertThat(portRange.isInRange(ImmutablePort.of(8002))).isTrue();

        Assertions.assertThat(portRange.isInRange(ImmutablePort.of(8003))).isFalse();
        Assertions.assertThat(portRange.isInRange(ImmutablePort.of(9000))).isFalse();

    }


}