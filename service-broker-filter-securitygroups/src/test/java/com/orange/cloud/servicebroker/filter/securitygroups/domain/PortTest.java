package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Sebastien Bortolussi
 */
public class PortTest {

    @Test
    public void invalid_port() throws Exception {
        Throwable thrown = catchThrowable(() -> {
                    ImmutablePort.of(-2);
                }
        );
        assertThat(thrown).hasMessageContaining("Invalid port : -2");
    }

    @Test
    public void valid_port() throws Exception {
        ImmutablePort.of(8000);
    }

    @Test
    public void greater_than() throws Exception {
        assertThat(ImmutablePort.of(8000).greaterOrEqualsTo(ImmutablePort.of(8000))).isTrue();
        assertThat(ImmutablePort.of(8000).greaterOrEqualsTo(ImmutablePort.of(7999))).isTrue();

        assertThat(ImmutablePort.of(8000).greaterOrEqualsTo(ImmutablePort.of(8001))).isFalse();
    }

    @Test
    public void less_than() throws Exception {
        assertThat(ImmutablePort.of(8001).lessOrEqualsTo(ImmutablePort.of(8001))).isTrue();
        assertThat(ImmutablePort.of(8001).lessOrEqualsTo(ImmutablePort.of(8002))).isTrue();

        assertThat(ImmutablePort.of(8001).lessOrEqualsTo(ImmutablePort.of(8000))).isFalse();
    }


}