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

package com.orange.cloud.servicebroker.filter.securitygroups.filter;

import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * allows expressing connection info (host and port) to remote host from alternative forms: individual fields or a URI string
 */
public class ConnectionInfo {

    private String host;
    private int port;

    public ConnectionInfo(String host, int port) {
        setHost(host);
        setPort(port);
    }

    public ConnectionInfo(String uriString) {
        try {
            URI uri = new URI(uriString);
            setHost(uri.getHost());
            if (noPort(uri)) {
                setDefaultPort(uri.getScheme());
            } else {
                setPort(uri.getPort());
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

    public String getHost() {
        return host;
    }

    private void setHost(String host) {
        Assert.hasText(host, String.format("Cannot create connection info. Invalid host : <%s>", host));
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    private void setPort(int port) {
        Assert.isTrue(port > -1, String.format("Cannot create connection info. Invalid port : <%d>", port));
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionInfo that = (ConnectionInfo) o;

        if (port != that.port) return false;
        return host.equals(that.host);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

    private static enum Scheme {
        http(80),
        https(443);

        private int defaultPort;

        Scheme(int defaultPort) {
            this.defaultPort = defaultPort;
        }

        public int defaultPort() {
            return defaultPort;
        }
    }
}
