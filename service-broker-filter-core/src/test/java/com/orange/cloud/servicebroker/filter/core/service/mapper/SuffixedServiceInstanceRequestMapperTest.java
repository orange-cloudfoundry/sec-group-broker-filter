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

package com.orange.cloud.servicebroker.filter.core.service.mapper;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.CloudFoundryContext;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;

/**
 * @author Sebastien Bortolussi
 */
public class SuffixedServiceInstanceRequestMapperTest {

    @Test
    public void withoutSuffix() {
        Assert.assertEquals("plan-one-id", SuffixedServiceInstanceRequestMapper.withoutSuffix("plan-one-id-suffix", "-suffix"));
    }

    @Test
    public void should_map_create_service_instance_request() {
        SuffixedServiceInstanceRequestMapper mapper = new SuffixedServiceInstanceRequestMapper("-suffixed");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");

        Assert.assertEquals(
            CreateServiceInstanceRequest.builder()
                .serviceDefinitionId("serviceDefinitionId")
                .planId("planId")
                .context(CloudFoundryContext.builder()
                    .organizationGuid("organizationGuid")
                    .spaceGuid("spaceGuid")
                    .build())
                .parameters(parameters)
                .serviceInstanceId("serviceInstanceId")
                .asyncAccepted(true)
                .build()
            , mapper.map(
            CreateServiceInstanceRequest.builder()
                .serviceDefinitionId("serviceDefinitionId-suffixed")
                .planId("planId-suffixed")
                .context(CloudFoundryContext.builder()
                    .organizationGuid("organizationGuid")
                    .spaceGuid("spaceGuid")
                    .build())
                .parameters(parameters)
                .serviceInstanceId("serviceInstanceId")
                .asyncAccepted(true).build()));
    }

    @Test
    public void should_map_delete_service_instance_request() throws Exception {
        SuffixedServiceInstanceRequestMapper mapper = new SuffixedServiceInstanceRequestMapper("-suffixed");

        Assert.assertEquals(DeleteServiceInstanceRequest.builder()
                .serviceInstanceId("serviceInstanceId")
                .serviceDefinitionId("serviceDefinitionId")
                .planId("planId")
                .asyncAccepted(true)
                .build(),
        mapper.map(DeleteServiceInstanceRequest.builder()
            .serviceInstanceId("serviceInstanceId")
            .serviceDefinitionId("serviceDefinitionId-suffixed")
            .planId("planId-suffixed")
            .asyncAccepted(true)
            .build()));
    }

    @Test
    public void should_map_update_service_instance_request() throws Exception {
        SuffixedServiceInstanceRequestMapper mapper = new SuffixedServiceInstanceRequestMapper("-suffixed");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");

        Assert.assertEquals(UpdateServiceInstanceRequest.builder()
                .serviceDefinitionId("serviceDefinitionId")
                .planId("planId")
                .parameters(parameters)
                .serviceInstanceId("serviceInstanceId")
                .asyncAccepted(true)
                .build(),
            mapper.map(UpdateServiceInstanceRequest.builder()
                .serviceDefinitionId("serviceDefinitionId-suffixed")
                .planId("planId-suffixed")
                .parameters(parameters)
                .serviceInstanceId("serviceInstanceId")
                .asyncAccepted(true)
                .build()));
    }

    @Test
    public void should_map_get_last_service_operation_request() throws Exception {
        SuffixedServiceInstanceRequestMapper mapper = new SuffixedServiceInstanceRequestMapper("-suffixed");

        Assert.assertEquals(GetLastServiceOperationRequest.builder()
                .serviceInstanceId("serviceInstanceId")
                .build(),
            mapper.map(GetLastServiceOperationRequest.builder()
                .serviceInstanceId("serviceInstanceId")
                .build()));
    }

}