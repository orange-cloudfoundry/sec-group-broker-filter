/*
 * <!--
 *
 *     Copyright (C) 2015 Orange
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * -->
 */

package com.orange.cloud.servicebroker.filter.securitygroups.domain;

import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.stream.Stream;

/**
 * allows expressing connection info (host and port) to remote host from alternative forms: individual fields or a URI string
 */
public class Destination {

    private String host;
    private Port port;

    public Destination(String host, Port port) {
        setHost(host);
        setPort(port);
    }

    public Destination(String uriString) {
        try {
            URI uri = new URI(uriString);
            setHost(uri.getHost());
            if (noPort(uri)) {
                setDefaultPort(uri.getScheme());
            } else {
                setPort(ImmutablePort.of(uri.getPort()));
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot create connection info. Invalid URI " + uriString, e);
        }
    }

    private void setDefaultPort(String scheme) {
        try {
            final Scheme schemeWithDefaultPort = Scheme.valueOf(scheme.toLowerCase());
            setPort(schemeWithDefaultPort.defaultPort());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Cannot create connection info. No port has been defined."));
        }
    }

    private boolean noPort(URI uri) {
        return uri.getPort() == -1;
    }

    public boolean noPort() {
        return getPort().empty();
    }

    public String getHost() {
        return host;
    }

    private void setHost(String host) {
        Assert.hasText(host, String.format("Cannot create connection info. Invalid host : <%s>", host));
        this.host = host;
    }

    public Stream<String> getIPs() {
        try {
            return Stream.of(InetAddress.getAllByName(host))
                    .map(InetAddress::getHostAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public Port getPort() {
        return port;
    }

    private void setPort(Port port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Destination that = (Destination) o;

        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        return port != null ? port.equals(that.port) : that.port == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (port != null ? port.hashCode() : 0);
        return result;
    }

    private static enum Scheme {
        http(80),
        https(443);

        private Port defaultPort;

        Scheme(int defaultPort) {
            this.defaultPort = ImmutablePort.of(defaultPort);
        }

        public Port defaultPort() {
            return defaultPort;
        }
    }
}
