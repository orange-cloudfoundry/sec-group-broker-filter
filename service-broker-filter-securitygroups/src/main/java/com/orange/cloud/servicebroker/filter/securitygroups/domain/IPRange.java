package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.immutables.value.Value;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * @author Sebastien Bortolussi
 */
@Value.Immutable
public abstract class IPRange implements Range<IPAddress> {

    abstract IPAddress from();

    abstract IPAddress to();

    @Value.Check
    protected void validate() {
        Assert.isTrue(to().greaterOrEqualsTo(from()), String.format("Invalid range. %s should be greater or equals to %s", to(), from()));
    }

    @Override
    public boolean isInRange(IPAddress candidate) {
        return Optional.ofNullable(candidate)
                .map(ip -> from().lessOrEqualsTo(candidate) && to().greaterOrEqualsTo(candidate))
                .orElse(Boolean.FALSE);
    }

}
