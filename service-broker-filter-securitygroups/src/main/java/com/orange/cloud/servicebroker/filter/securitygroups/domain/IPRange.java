package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.immutables.value.Value;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * @author Sebastien Bortolussi
 */
@Value.Immutable
public abstract class IPRange implements Range<IPV4Address> {

    abstract IPV4Address from();

    abstract IPV4Address to();

    @Value.Check
    protected void validate() {
        Assert.isTrue(to().greaterOrEqualsTo(from()), String.format("Invalid range. %s should be greater or equals to %s", to(), from()));
    }

    @Override
    public boolean isInRange(IPV4Address candidate) {
        return Optional.ofNullable(candidate)
                .map(ip -> from().lessOrEqualsTo(candidate) && to().greaterOrEqualsTo(candidate))
                .orElse(Boolean.FALSE);
    }

}
