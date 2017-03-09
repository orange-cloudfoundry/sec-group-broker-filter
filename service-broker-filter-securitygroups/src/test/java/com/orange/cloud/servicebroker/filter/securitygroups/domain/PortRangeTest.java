package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author Sebastien Bortolussi
 */
public class PortRangeTest {

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