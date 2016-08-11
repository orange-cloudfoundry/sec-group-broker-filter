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

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.ApplicationsV2;
import org.cloudfoundry.client.v2.applications.GetApplicationRequest;
import org.cloudfoundry.client.v2.applications.GetApplicationResponse;
import org.cloudfoundry.client.v2.securitygroups.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class CreateSecurityGroupTest {

    public static final String VALID_JDBC_URL = "jdbc:mysql://127.0.0.1:3306/mydb?user=2106password=Uq3YCioVsO3Dbcp4";
    public static final String INVALID_JDBC_URL_NO_HOST = "jdbc:mysql:///mydb?user=2106password=Uq3YCioVsO3Dbcp4";
    public static final String INVALID_JDBC_URL_NO_PORT = "jdbc:mysql://127.0.0.1/mydb?user=2106password=Uq3YCioVsO3Dbcp4";

    @Mock
    CloudFoundryClient cloudFoundryClient;

    private CreateSecurityGroup createSecurityGroup;

    @Before
    public void init() {
        given(cloudFoundryClient.securityGroups())
                .willReturn(Mockito.mock(SecurityGroups.class));
        given(cloudFoundryClient.applicationsV2())
                .willReturn(Mockito.mock(ApplicationsV2.class));
        createSecurityGroup = new CreateSecurityGroup(cloudFoundryClient);
    }

    @Test
    public void should_create_security_group() throws Exception {
        givenBoundedAppExists(this.cloudFoundryClient, "app_guid");
        givenCreateSecurityGroupsSucceeds(this.cloudFoundryClient, "test-securitygroup-name");

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("jdbcUrl", VALID_JDBC_URL);
        createSecurityGroup.run(new CreateServiceInstanceBindingRequest(null, null, "app_guid", null, null).withBindingId("test-securitygroup-name"),
                new CreateServiceInstanceAppBindingResponse().withCredentials(credentials));

        Mockito.verify(cloudFoundryClient.securityGroups())
                .create(CreateSecurityGroupRequest.builder()
                        .name("test-securitygroup-name")
                        .spaceId("space_id")
                        .rule(RuleEntity.builder()
                                .protocol("tcp")
                                .ports("3306")
                                .destination("127.0.0.1")
                                .build())
                        .build());
    }

    private void givenBoundedAppExists(CloudFoundryClient cloudFoundryClient, String appId) {
        given(cloudFoundryClient.applicationsV2()
                .get(GetApplicationRequest.builder()
                        .applicationId(appId)
                        .build()))
                .willReturn(Mono.just(GetApplicationResponse.builder()
                        .entity(ApplicationEntity.builder()
                                .spaceId("space_id")
                                .build())
                        .build()));
    }

    private void givenCreateSecurityGroupsSucceeds(CloudFoundryClient cloudFoundryClient, String securityGroupName) {
        given(cloudFoundryClient.securityGroups()
                .create(CreateSecurityGroupRequest.builder()
                        .name(securityGroupName)
                        .spaceId("space_id")
                        .rule(RuleEntity.builder()
                                .protocol("tcp")
                                .ports("3306")
                                .destination("127.0.0.1")
                                .build())
                        .build()))
                .willReturn(Mono.just(CreateSecurityGroupResponse.builder()
                        .entity(SecurityGroupEntity.builder()
                                .name(securityGroupName)
                                .rule(RuleEntity.builder()
                                        .protocol("tcp")
                                        .ports("3306")
                                        .destination("127.0.0.1")
                                        .build())
                                .build())
                        .build()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void noHostname() throws Exception {
        CreateServiceInstanceBindingRequest request = new CreateServiceInstanceBindingRequest(null, null, "app_guid", null, null);
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("jdbcUrl", INVALID_JDBC_URL_NO_HOST);
        CreateServiceInstanceAppBindingResponse response = new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);

        createSecurityGroup.run(request, response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noPort() throws Exception {
        CreateServiceInstanceBindingRequest request = new CreateServiceInstanceBindingRequest(null, null, "app_guid", null, null);
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("jdbcUrl", INVALID_JDBC_URL_NO_PORT);
        CreateServiceInstanceAppBindingResponse response = new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);

        createSecurityGroup.run(request, response);
    }

}