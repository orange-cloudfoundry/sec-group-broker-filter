package com.orange.cloud.servicebroker.filter.securitygroups;

import java.util.List;

/**
 * @author Sebastien Bortolussi
 */

public class ServiceOffering {

    private final String label;
    private final List<String> servicePlans;

    public ServiceOffering(String label, List<String> servicePlans) {
        this.label = label;
        this.servicePlans = servicePlans;
    }


    public String getLabel() {
        return label;
    }

    public List<String> getServicePlans() {
        return servicePlans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceOffering that = (ServiceOffering) o;

        if (!label.equals(that.label)) return false;
        return servicePlans.equals(that.servicePlans);

    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + servicePlans.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ServiceOffering{" +
                "label='" + label + '\'' +
                ", servicePlans=" + servicePlans +
                '}';
    }
}
