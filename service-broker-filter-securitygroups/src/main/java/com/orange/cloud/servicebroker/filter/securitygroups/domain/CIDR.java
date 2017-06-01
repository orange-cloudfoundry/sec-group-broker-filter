package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.apache.commons.net.util.SubnetUtils;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * @author Sebastien Bortolussi
 */
/*
CIDR IP addresses
 */
@Value.Immutable
public abstract class CIDR implements Range<IPV4Address> {

    @Value.Parameter
    public abstract String value();

    @Override
    public boolean isInRange(IPV4Address candidate) {
        return Optional.ofNullable(candidate)
                .map(IPV4Address::value)
                .map(ip -> {
                    final SubnetUtils cidr = new SubnetUtils(value());
                    return cidr.getInfo().isInRange(ip);
                })
                .orElse(Boolean.FALSE);

    }

    @Value.Check
    protected void validate() {
        try {
            new SubnetUtils(value());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid CIDR block : %s", value()));
        }
    }
}
