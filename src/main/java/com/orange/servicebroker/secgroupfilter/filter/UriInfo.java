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

package com.orange.servicebroker.secgroupfilter.filter;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class that allows expressing URIs in alternative forms: individual fields or a URI string
 */
public class UriInfo {

    public static final String JDBC_PREFIX = "jdbc:";

    private final String host;
    private final int port;

    public UriInfo(String uriString) {
        this.host = getUri(uriString).getHost();
        this.port = getUri(uriString).getPort();
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port == -1 ? "" : String.valueOf(port);
    }

    private URI getUri(String uriString) {
        try {
            if (uriString.startsWith(JDBC_PREFIX))
                return new URI(uriString.substring(JDBC_PREFIX.length()));
            else return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI " + uriString, e);
        }
    }

}
