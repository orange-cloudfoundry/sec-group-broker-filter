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
        return trustedDestination.isATrustedHost(ImmutableIPAddress.of(candidate.getHost()))
                && trustedDestination.isATrustedPort(candidate.getPort());
    }

    public String toString() {
        return trustedDestination.toString();
    }
}
