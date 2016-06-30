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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastien Bortolussi
 */
public class CloudFoundryCredentialsUtilsTest {

    @Test
    public void get_jdbc_url_from_credentials() throws Exception {

        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/mydb?user=2106password=Uq3YCioVsO3Dbcp4";
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("jdbcUrl", jdbcUrl);

        Assert.assertEquals(jdbcUrl, CloudFoundryCredentialsUtils.getUriFromCredentials(credentials));
    }

    @Test
    public void get_uri_from_credentials() throws Exception {

        String uri = "mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1:3306/mydb?reconnect=true";
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("uri", uri);

        Assert.assertEquals(uri, CloudFoundryCredentialsUtils.getUriFromCredentials(credentials));
    }

    @Test
    public void get_mysql_uri_from_credentials() throws Exception {

        String uri = "mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1:3306/mydb?reconnect=true";
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("mysqlUri", uri);

        Assert.assertEquals(uri, CloudFoundryCredentialsUtils.getUriFromCredentials(credentials));
    }

    @Test
    public void get_postgres_uri_from_credentials() throws Exception {

        String uri = "mysql://2106:Uq3YCioVsO3Dbcp4@127.0.0.1:3306/mydb?reconnect=true";
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("postgresUri", uri);

        Assert.assertEquals(uri, CloudFoundryCredentialsUtils.getUriFromCredentials(credentials));
    }


}