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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Collectors;

/**
 * @author Sebastien Bortolussi
 */
public class ConnectionInfoTest {

    @Test
    public void connection_info_from_uri() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo("mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1:3306/mydb?reconnect=true");

        Assert.assertEquals("127.0.0.1", connectionInfo.getHost());
        Assert.assertEquals(3306, connectionInfo.getPort());
    }

    @Test
    public void connection_info_from_host_and_port() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo("127.0.0.1", 3306);

        Assert.assertEquals("127.0.0.1", connectionInfo.getHost());
        Assert.assertEquals(3306, connectionInfo.getPort());
    }

    @Test
    public void get_ips_from_uri() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo("mysql://2106:Uq3YCioVsO3Dbcp4@localhost:3306/mydb?reconnect=true");

        Assertions.assertThat(connectionInfo.getIPs().collect(Collectors.toList())).containsOnly("127.0.0.1", "0:0:0:0:0:0:0:1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_connection_info_from_uri_with_no_port() throws Exception {
        new ConnectionInfo("mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1/mydb?reconnect=true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_connection_info_from_host_no_port() throws Exception {
        new ConnectionInfo("127.0.0.1", -1);
    }

    @Test
    public void connection_info_with_default_http_port_if_http_uri_with_no_port() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo("http://mysite.org/path");

        Assert.assertEquals(80, connectionInfo.getPort());
    }

    @Test
    public void connection_info_with_default_https_port_if_https_uri_with_no_port() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo("https://mysite.org/path");

        Assert.assertEquals(443, connectionInfo.getPort());
    }

    @Test
    public void connection_info_with_custom_scheme_and_query_params() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo("redis://password@192.168.1.1:4040/database?timeout=10");

        Assert.assertEquals(4040, connectionInfo.getPort());
        Assert.assertEquals("192.168.1.1", connectionInfo.getHost());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_connection_info_from_uri_with_no_port_and_no_default_port_defined() throws Exception {
        new ConnectionInfo("scheme://mysite.org/path");
    }

}