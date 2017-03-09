package com.orange.cloud.servicebroker.filter.securitygroups.domain;

/**
 * @author Sebastien Bortolussi
 */
public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
}
