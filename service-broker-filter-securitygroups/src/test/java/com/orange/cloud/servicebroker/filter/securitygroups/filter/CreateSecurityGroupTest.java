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
import org.cloudfoundry.client.v2.CloudFoundryException;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.ApplicationsV2;
import org.cloudfoundry.client.v2.applications.GetApplicationRequest;
import org.cloudfoundry.client.v2.applications.GetApplicationResponse;
import org.cloudfoundry.client.v2.securitygroups.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class CreateSecurityGroupTest {

    static final String TEST_URI = "mysql://127.0.0.1:3306/mydb?user=2106password=Uq3YCioVsO3Dbcp4";
    static final String NO_HOST_URI = "mysql:///mydb?user=2106password=Uq3YCioVsO3Dbcp4";
    static final String NO_PORT_URI = "mysql://127.0.0.1/mydb?user=2106password=Uq3YCioVsO3Dbcp4";
    @Rule
    public OutputCapture capture = new OutputCapture();
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
        credentials.put("uri", TEST_URI);
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


    @Test(expected = CloudFoundryException.class)
    public void fail_to_create_create_security_group() throws Exception {
        givenBoundedAppExists(this.cloudFoundryClient, "app_guid");
        givenCreateSecurityGroupsFails(this.cloudFoundryClient, "test-securitygroup-name");

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("uri", TEST_URI);
        createSecurityGroup.run(new CreateServiceInstanceBindingRequest(null, null, "app_guid", null, null).withBindingId("test-securitygroup-name"),
                new CreateServiceInstanceAppBindingResponse().withCredentials(credentials));

    }

    @Test(expected = CloudFoundryException.class)
    public void should_block_until_create_security_group_returns() throws Exception {
        givenBoundedAppExists(this.cloudFoundryClient, "app_guid");
        givenCreateSecurityGroupsFailsWithDelay(this.cloudFoundryClient, "test-securitygroup-name");

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("uri", TEST_URI);
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

    private void givenCreateSecurityGroupsFails(CloudFoundryClient cloudFoundryClient, String securityGroupName) {
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
                .willReturn(Mono.error(new CloudFoundryException(999, "test-exception-description", "test-exception-errorCode")));
    }

    private void givenCreateSecurityGroupsFailsWithDelay(CloudFoundryClient cloudFoundryClient, String securityGroupName) {
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
                .willReturn(Mono
                        .delay(Duration.ofSeconds(2))
                        .then(Mono.error(new CloudFoundryException(999, "test-exception-description", "test-exception-errorCode"))));
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
        credentials.put("uri", NO_HOST_URI);
        CreateServiceInstanceAppBindingResponse response = new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);

        createSecurityGroup.run(request, response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noPort() throws Exception {
        CreateServiceInstanceBindingRequest request = new CreateServiceInstanceBindingRequest(null, null, "app_guid", null, null);
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("uri", NO_PORT_URI);
        CreateServiceInstanceAppBindingResponse response = new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);

        createSecurityGroup.run(request, response);
    }

}