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
        return !candidate.getIPV4s()
                .map(ImmutableIPV4Address::of)
                .map(trustedDestination::isATrustedHost)
                .filter(Boolean.FALSE::equals)
                .findFirst()
                .isPresent()
                && trustedDestination.isATrustedPort(candidate.getPort());
    }

    public String toString() {
        return trustedDestination.toString();
    }
}
