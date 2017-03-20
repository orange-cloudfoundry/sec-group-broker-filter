package com.orange.cloud.servicebroker.filter.securitygroups.domain;

/**
 * @author Sebastien Bortolussi
 */
public interface Range<T> {

    boolean isInRange(T candidate);

}
