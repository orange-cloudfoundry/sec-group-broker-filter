package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.immutables.value.Value;
import org.springframework.util.Assert;

/**
 * @author Sebastien Bortolussi
 */
@Value.Immutable
public abstract class Port {

    @Value.Parameter
    public abstract int value();

    @Value.Lazy
    public boolean empty() {
        return value() == -1;
    }

    @Value.Lazy
    public boolean greaterOrEqualsTo(Port port) {
        return value() >= port.value();
    }

    @Value.Lazy
    public boolean lessOrEqualsTo(Port port) {
        return value() <= port.value();
    }

    @Value.Check
    protected void check() {
        Assert.isTrue(value() >= -1, String.format("Invalid port : %d", value()));
    }

}
