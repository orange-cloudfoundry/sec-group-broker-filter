package com.orange.cloud.servicebroker.filter.securitygroups.domain;

/**
 * @author Sebastien Bortolussi
 */
public class TrustedDestinationSpecification implements Specification<Destination> {

    private TrustedDestination trustedDestination;

    public TrustedDestinationSpecification(TrustedDestination trustedDestination) {
        this.trustedDestination = trustedDestination;
    }

    @Override
    public boolean isSatisfiedBy(Destination candidate) {
        return trustedDestination.isATrustedPort(candidate.getPort()) &&
            candidate.getIPs().allMatch(s -> trustedDestination.isATrustedHost(ImmutableIPAddress.of(s)));
    }

    public String toString() {
        return trustedDestination.toString();
    }
}
