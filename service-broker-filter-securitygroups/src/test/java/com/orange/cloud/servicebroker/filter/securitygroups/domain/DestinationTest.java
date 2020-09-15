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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Collectors;

/**
 * @author Sebastien Bortolussi
 */
public class DestinationTest {

    @Test
    public void connection_info_from_uri() throws Exception {
        Destination destination = new Destination("mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1:3306/mydb?reconnect=true");

        Assert.assertEquals("127.0.0.1", destination.getHost());
        Assert.assertEquals(ImmutablePort.of(3306), destination.getPort());
    }

    @Test
    public void connection_info_from_host_and_port() throws Exception {
        Destination destination = new Destination("127.0.0.1", ImmutablePort.of(3306));

        Assert.assertEquals("127.0.0.1", destination.getHost());
        Assert.assertEquals(ImmutablePort.of(3306), destination.getPort());
    }

    @Test
    public void get_ips_from_uri_with_fqdn() throws Exception {
        Destination destination = new Destination("mysql://2106:Uq3YCioVsO3Dbcp4@localhost:3306/mydb?reconnect=true");

        Assertions.assertThat(destination.getIPs().collect(Collectors.toList())).containsOnly("127.0.0.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_connection_info_from_uri_with_no_port() throws Exception {
        new Destination("mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1/mydb?reconnect=true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_connection_info_invalid_port() throws Exception {
        new Destination("127.0.0.1", ImmutablePort.of(-2));
    }

    @Test
    public void connection_info_with_default_http_port_if_http_uri_with_no_port() throws Exception {
        Destination destination = new Destination("http://mysite.org/path");

        Assert.assertEquals(ImmutablePort.of(80), destination.getPort());
    }

    @Test
    public void connection_info_with_default_https_port_if_https_uri_with_no_port() throws Exception {
        Destination destination = new Destination("https://mysite.org/path");

        Assert.assertEquals(ImmutablePort.of(443), destination.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_create_connection_info_from_uri_with_no_port_and_no_default_port_defined() throws Exception {
        new Destination("scheme://mysite.org/path");
    }

}