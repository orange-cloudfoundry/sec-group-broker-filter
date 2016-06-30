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

package com.orange.servicebroker.secgroupfilter.filter.binding;

import org.cloudfoundry.client.v2.applications.ApplicationsV2;
import org.cloudfoundry.client.v2.applications.GetApplicationResponse;
import org.cloudfoundry.client.v2.securitygroups.CreateSecurityGroupResponse;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroups;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Mono;

@RunWith(MockitoJUnitRunner.class)
public class CreateSecurityGroupTest {

    public static final String VALID_JDBC_URL = "jdbc:mysql://127.0.0.1:3306/mydb?user=2106password=Uq3YCioVsO3Dbcp4";
    public static final String INVALID_JDBC_URL_NO_HOST = "jdbc:mysql:///mydb?user=2106password=Uq3YCioVsO3Dbcp4";
    public static final String INVALID_JDBC_URL_NO_PORT = "jdbc:mysql://127.0.0.1/mydb?user=2106password=Uq3YCioVsO3Dbcp4";
    @Mock
    SecurityGroups securityGroups;

    @Mock
    ApplicationsV2 applicationsV2;

    @Test
    public void valid() throws Exception {
        Mockito.when(securityGroups.create(Mockito.any())).thenReturn(new Mono<CreateSecurityGroupResponse>() {
            @Override
            public void subscribe(Subscriber<? super CreateSecurityGroupResponse> s) {

            }
        });

        Mockito.when(applicationsV2.get(Mockito.any())).thenReturn(new Mono<GetApplicationResponse>() {
            @Override
            public void subscribe(Subscriber<? super GetApplicationResponse> s) {

            }
        });
        CreateSecurityGroup actionDelegate = new CreateSecurityGroup(securityGroups, applicationsV2);
        actionDelegate.invoke(CreateServiceInstanceBindingRequest.builder()
                        .appGuid("app_guid")
                        .build(),
                CreateServiceInstanceBindingResponse.builder()
                        .credential("hostname", "localhost")
                        .credential("jdbcUrl", VALID_JDBC_URL)
                        .build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void noHostname() throws Exception {
        CreateSecurityGroup actionDelegate = new CreateSecurityGroup(securityGroups, applicationsV2);
        actionDelegate.invoke(CreateServiceInstanceBindingRequest.builder()
                        .appGuid("app_guid")
                        .build(),
                CreateServiceInstanceBindingResponse.builder()
                        .credential("jdbcUrl", INVALID_JDBC_URL_NO_HOST)
                        .build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void noPort() throws Exception {
        CreateSecurityGroup actionDelegate = new CreateSecurityGroup(securityGroups, applicationsV2);
        actionDelegate.invoke(CreateServiceInstanceBindingRequest.builder()
                        .appGuid("app_guid")
                        .build(),
                CreateServiceInstanceBindingResponse.builder()
                        .credential("jdbcUrl", INVALID_JDBC_URL_NO_PORT)
                        .build());
    }

}