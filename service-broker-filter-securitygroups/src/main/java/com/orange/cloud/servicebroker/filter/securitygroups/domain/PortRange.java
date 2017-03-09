package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.immutables.value.Value;

import java.util.Optional;

/**
 * @author Sebastien Bortolussi
 */
@Value.Immutable
public abstract class PortRange implements Range<Port> {

    public abstract Port from();

    public abstract Port to();

    @Override
    public boolean isInRange(Port candidate) {
        return Optional.ofNullable(candidate)
                .map(port -> port.greaterOrEqualsTo(from()) && port.lessOrEqualsTo(to()))
                .orElse(Boolean.FALSE);
    }
}
