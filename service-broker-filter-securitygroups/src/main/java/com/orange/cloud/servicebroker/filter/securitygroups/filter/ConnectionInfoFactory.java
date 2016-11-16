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

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Stream.of;

/**
 * credits to https://github.com/spring-cloud/spring-cloud-connectors
 */
public class ConnectionInfoFactory {

    public static final String[] HOST_KEYS = {"hostname", "host"};
    public static final String PORT_KEYS = "port";
    public static final String URI_KEYS = "uri";


    public static ConnectionInfo fromCredentials(Map<String, Object> credentials) {
        final Optional<String> uri = ConnectionInfoFactory.getUriFromCredentials(credentials);
        final Optional<String> host = getHostFromCredentials(credentials);
        final Optional<Integer> port = getPortFromCredentials(credentials);

        if (uri.isPresent())
            return new ConnectionInfo(uri.get());
        if (host.isPresent() && port.isPresent())
            return new ConnectionInfo(host.get(), port.get());

        throw new IllegalStateException(String.format("Cannot extract host and port from credentials %s", credentials));
    }

    private static Optional<Integer> getPortFromCredentials(Map<String, Object> credentials) {
        return ConnectionInfoFactory.getIntFromCredentials(credentials, PORT_KEYS);
    }

    private static Optional<String> getHostFromCredentials(Map<String, Object> credentials) {
        return ConnectionInfoFactory.getStringFromCredentials(credentials, HOST_KEYS);
    }


    protected static Optional<String> getUriFromCredentials(Map<String, Object> credentials) {
        return getStringFromCredentials(credentials, URI_KEYS);
    }

    private static Optional<String> getStringFromCredentials(Map<String, Object> credentials, String... keys) {
        return of(keys)
                .filter(credentials::containsKey)
                .map(credentials::get)
                .map(String::valueOf)
                .findFirst();
    }

    private static Optional<Integer> getIntFromCredentials(Map<String, Object> credentials, String... keys) {
        return of(keys)
                .filter(credentials::containsKey)
                .map(credentials::get)
                .map(String::valueOf)
                .map(Integer::parseInt)
                .findFirst();
    }


}
