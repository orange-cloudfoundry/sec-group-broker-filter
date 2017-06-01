package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.immutables.value.Value;

import java.util.Optional;

/**
 * @author Sebastien Bortolussi
 */
@Value.Immutable
@Value.Style(deepImmutablesDetection = true, depluralize = true)
public abstract class TrustedDestination {

    /*
    A single IP address, an IP address range like 192.0.2.0-192.0.2.50, or a CIDR block to allow network access to.
    */
    public abstract Range<IPV4Address> hosts();

    /*
    A single port, multiple comma-separated ports, or a single range of ports that can receive traffic.
    Examples: 443, 80,8080,8081, 8080-8081
   */
    public abstract Optional<Range<Port>> ports();

    public boolean isATrustedHost(IPV4Address candidate) {
        return hosts().isInRange(candidate);
    }

    public boolean isATrustedPort(Port port) {
        return ports()
                .map(ports -> ports.isInRange(port))
                .orElse(Boolean.TRUE);
    }

}
