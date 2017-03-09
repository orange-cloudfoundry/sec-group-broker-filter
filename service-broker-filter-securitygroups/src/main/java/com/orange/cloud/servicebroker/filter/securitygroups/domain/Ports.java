package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

/**
 * @author Sebastien Bortolussi
 */
@Value.Immutable
@Value.Style(deepImmutablesDetection = true, depluralize = true)
public abstract class Ports implements Range<Port> {

    @Value.Parameter
    public abstract List<Port> values();

    @Value.Check
    protected void validate() {
        if (values().isEmpty())
            throw new IllegalArgumentException(String.format("Invalid ports. One port should at least be defined.", values()));
    }

    @Override
    public boolean isInRange(Port candidate) {
        return Optional.ofNullable(candidate)
                .map(Port::value)
                .map(port -> values().stream()
                        .map(Port::value)
                        .filter(port::equals)
                        .findFirst()
                        .map(p -> Boolean.TRUE)
                        .orElse(Boolean.FALSE))
                .orElse(Boolean.FALSE);
    }
}
