package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.immutables.value.Value;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * @author Sebastien Bortolussi
 */
@Value.Immutable
public abstract class PortRange implements Range<Port> {

    public abstract Port from();

    public abstract Port to();

    @Value.Check
    protected void validate() {
        Assert.isTrue(to().greaterOrEqualsTo(from()), String.format("Invalid range. %s should be greater or equals to %s", to(), from()));
    }

    @Override
    public boolean isInRange(Port candidate) {
        return Optional.ofNullable(candidate)
                .map(port -> port.greaterOrEqualsTo(from()) && port.lessOrEqualsTo(to()))
                .orElse(Boolean.FALSE);
    }
}
