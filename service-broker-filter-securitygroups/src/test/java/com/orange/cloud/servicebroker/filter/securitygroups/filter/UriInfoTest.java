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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastien Bortolussi
 */
public class UriInfoTest {

    @Test
    public void host_from_jdbc_url() throws Exception {

        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/mydb?user=2106password=Uq3YCioVsO3Dbcp4";
        UriInfo uriInfo = new UriInfo(jdbcUrl);

        Assert.assertEquals("127.0.0.1", uriInfo.getHost());
    }

    @Test
    public void port_from_jdbc_url() throws Exception {

        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/mydb?user=2106password=Uq3YCioVsO3Dbcp4";
        UriInfo uriInfo = new UriInfo(jdbcUrl);

        Assert.assertEquals("3306", uriInfo.getPort());
    }

    @Test
    public void host_from_uri() throws Exception {

        String uri = "mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1:3306/mydb?reconnect=true";
        UriInfo uriInfo = new UriInfo(uri);

        Assert.assertEquals("127.0.0.1", uriInfo.getHost());
    }

    @Test
    public void port_from_uri() throws Exception {

        String uri = "mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1:3306/mydb?reconnect=true";
        UriInfo uriInfo = new UriInfo(uri);

        Assert.assertEquals("3306", uriInfo.getPort());
    }

    @Test
    public void uri_no_port() throws Exception {

        String uri = "mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1/mydb?reconnect=true";
        UriInfo uriInfo = new UriInfo(uri);

        Assert.assertEquals("", uriInfo.getPort());
    }

}