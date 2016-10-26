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
import org.cloudfoundry.client.v2.Metadata;
import org.cloudfoundry.client.v2.securitygroups.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DeleteSecurityGroupTest {

    @Mock
    private CloudFoundryClient cloudFoundryClient;

    private DeleteSecurityGroup deleteSecurityGroup;

    @Before
    public void init() {
        given(cloudFoundryClient.securityGroups())
                .willReturn(mock(SecurityGroups.class));
        deleteSecurityGroup = new DeleteSecurityGroup(cloudFoundryClient);
    }

    @Test
    public void should_delete_existing_security_group() throws Exception {
        givenSecurityGroupExists(this.cloudFoundryClient, "test-securitygroup-name");
        givenDeleteSecurityGroupsSucceeds(this.cloudFoundryClient, "test-securitygroup-id");

        DeleteServiceInstanceBindingRequest request = new DeleteServiceInstanceBindingRequest("serviceInstanceId", "test-securitygroup-name", "serviceDefinitionId", "planId", null);
        deleteSecurityGroup.run(request, null);

        Mockito.verify(cloudFoundryClient.securityGroups())
                .delete(DeleteSecurityGroupRequest.builder()
                        .securityGroupId("test-securitygroup-id")
                        .build());
    }

    @Test
    public void should_not_delete_if_security_group_does_not_exist() throws Exception {
        givenSecurityGroupDoesNotExist(this.cloudFoundryClient, "test-securitygroup-name");
        givenDeleteSecurityGroupsSucceeds(this.cloudFoundryClient, "test-securitygroup-id");

        DeleteServiceInstanceBindingRequest request = new DeleteServiceInstanceBindingRequest("serviceInstanceId", "test-securitygroup-name", "serviceDefinitionId", "planId", null);
        deleteSecurityGroup.run(request, null);

        Mockito.verify(cloudFoundryClient.securityGroups(), Mockito.never())
                .delete(any());
    }

    private void givenDeleteSecurityGroupsSucceeds(CloudFoundryClient cloudFoundryClient, String securityGroupId) {
        Mockito.when(cloudFoundryClient.securityGroups()
                .delete(DeleteSecurityGroupRequest.builder()
                        .securityGroupId(securityGroupId)
                        .build()))
                .thenReturn(Mono
                        .just(DeleteSecurityGroupResponse.builder().build()));
    }

    private void givenSecurityGroupExists(CloudFoundryClient cloudFoundryClient, String securityGroupName) {
        given(cloudFoundryClient.securityGroups()
                .list(ListSecurityGroupsRequest.builder()
                        .name(securityGroupName)
                        .page(1)
                        .build()))
                .willReturn(Mono
                        .just(ListSecurityGroupsResponse.builder()
                                .resource(SecurityGroupResource.builder()
                                        .metadata(Metadata.builder()
                                                .id("test-securitygroup-id")
                                                .build())
                                        .build())
                                .totalPages(1)
                                .build()));
    }

    private void givenSecurityGroupDoesNotExist(CloudFoundryClient cloudFoundryClient, String securityGroupName) {
        given(cloudFoundryClient.securityGroups()
                .list(ListSecurityGroupsRequest.builder()
                        .name(securityGroupName)
                        .page(1)
                        .build()))
                .willReturn(Mono
                        .just(ListSecurityGroupsResponse.builder()
                                .totalPages(1)
                                .totalResults(0)
                                .resources(Collections.emptyList())
                                .build()));
    }

}