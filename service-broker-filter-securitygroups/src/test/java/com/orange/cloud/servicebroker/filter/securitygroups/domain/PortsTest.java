package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Sebastien Bortolussi
 */
public class PortsTest {

    @Test
    public void invalid_ports() throws Exception {
        Throwable thrown = catchThrowable(() -> {
                    ImmutablePorts.builder().build();
                }
        );
        assertThat(thrown).hasMessageContaining("Invalid ports. One port should at least be defined.");
    }

    @Test
    public void valid_ports() throws Exception {
        ImmutablePorts.builder().addValue(ImmutablePort.of(8000)).build();
    }

    @Test
    public void in_range() throws Exception {
        final Range<Port> range = ImmutablePorts.builder()
                .addValue(ImmutablePort.of(8000))
                .addValue(ImmutablePort.of(8001))
                .build();
        Assertions.assertThat(range.isInRange(ImmutablePort.of(8000))).isTrue();
        Assertions.assertThat(range.isInRange(ImmutablePort.of(8001))).isTrue();
        Assertions.assertThat(range.isInRange(ImmutablePort.of(9000))).isFalse();
    }
}