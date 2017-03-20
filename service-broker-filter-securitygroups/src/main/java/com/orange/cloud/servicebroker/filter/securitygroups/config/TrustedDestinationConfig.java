package com.orange.cloud.servicebroker.filter.securitygroups.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Sebastien Bortolussi
 */
@ConfigurationProperties(prefix = "broker.filter.trusted.destination")
public class TrustedDestinationConfig {

    /*
    A single IP address, an IP address range like 192.0.2.0-192.0.2.50, or a CIDR block to allow network access to.
     */
    private String hosts;

    /*
    A single port, multiple comma-separated ports, or a single range of ports that can receive traffic.
    Examples: 443, 80,8080,8081, 8080-8081
     */
    private String ports;

    public TrustedDestinationConfig() {
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }
}
