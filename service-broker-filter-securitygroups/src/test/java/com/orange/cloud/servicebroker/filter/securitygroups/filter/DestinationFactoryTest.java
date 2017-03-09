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

import com.orange.cloud.servicebroker.filter.securitygroups.domain.Destination;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.ImmutablePort;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.Port;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastien Bortolussi
 */
public class DestinationFactoryTest {

    @Test
    public void uri_from_credentials() throws Exception {
        String uri = "mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1:3306/mydb?reconnect=true";
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("uri", uri);

        Assert.assertEquals(new Destination(uri), ConnectionInfoFactory.fromCredentials(credentials));
    }

    @Test
    public void hostname_and_port_from_credentials() throws Exception {
        String hostname = "127.0.0.1";
        Port port = ImmutablePort.of(6379);
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("hostname", hostname);
        credentials.put("port", port.value());

        Assert.assertEquals(new Destination(hostname, port), ConnectionInfoFactory.fromCredentials(credentials));
    }

    @Test(expected = Exception.class)
    public void fail_if_no_uri_no_hostname_from_credentials() throws Exception {
        int port = 6379;
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("port", port);

        ConnectionInfoFactory.fromCredentials(credentials);
    }

    @Test(expected = Exception.class)
    public void fail_if_no_uri_no_port_from_credentials() throws Exception {
        String hostname = "127.0.0.1";
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("hostname", hostname);

        ConnectionInfoFactory.fromCredentials(credentials);
    }

}