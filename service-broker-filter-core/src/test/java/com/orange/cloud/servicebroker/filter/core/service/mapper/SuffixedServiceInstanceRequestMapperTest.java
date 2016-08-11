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
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastien Bortolussi
 */
public class SuffixedServiceInstanceRequestMapperTest {

    @Test
    public void withoutSuffix() throws Exception {
        Assert.assertEquals("plan-one-id", SuffixedServiceInstanceRequestMapper.withoutSuffix("plan-one-id-suffix", "-suffix"));
    }

    @Test
    public void should_map_create_service_instance_request() throws Exception {
        SuffixedServiceInstanceRequestMapper mapper = new SuffixedServiceInstanceRequestMapper("-suffixed");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");

        Assert.assertEquals(new CreateServiceInstanceRequest("serviceDefinitionId",
                        "planId",
                        "organizationGuid",
                        "spaceGuid",
                        parameters)
                        .withServiceInstanceId("serviceInstanceId")
                        .withAsyncAccepted(true),
                mapper.map(new CreateServiceInstanceRequest("serviceDefinitionId-suffixed",
                        "planId-suffixed",
                        "organizationGuid",
                        "spaceGuid",
                        parameters)
                        .withServiceInstanceId("serviceInstanceId")
                        .withAsyncAccepted(true)));
    }

    @Test
    public void should_map_delete_service_instance_request() throws Exception {
        SuffixedServiceInstanceRequestMapper mapper = new SuffixedServiceInstanceRequestMapper("-suffixed");

        Assert.assertEquals(new DeleteServiceInstanceRequest("serviceInstanceId",
                        "serviceDefinitionId",
                        "planId",
                        null)
                        .withAsyncAccepted(true),
                mapper.map(new DeleteServiceInstanceRequest("serviceInstanceId",
                        "serviceDefinitionId-suffixed",
                        "planId-suffixed",
                        null)
                        .withAsyncAccepted(true)));
    }

    @Test
    public void should_map_update_service_instance_request() throws Exception {
        SuffixedServiceInstanceRequestMapper mapper = new SuffixedServiceInstanceRequestMapper("-suffixed");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key1", "value1");

        Assert.assertEquals(new UpdateServiceInstanceRequest("serviceDefinitionId",
                        "planId",
                        parameters)
                        .withServiceInstanceId("serviceInstanceId")
                        .withAsyncAccepted(true),
                mapper.map(new UpdateServiceInstanceRequest("serviceDefinitionId-suffixed",
                        "planId-suffixed",
                        parameters)
                        .withServiceInstanceId("serviceInstanceId")
                        .withAsyncAccepted(true)));
    }

    @Test
    public void should_map_get_last_service_operation_request() throws Exception {
        SuffixedServiceInstanceRequestMapper mapper = new SuffixedServiceInstanceRequestMapper("-suffixed");

        Assert.assertEquals(new GetLastServiceOperationRequest("serviceInstanceId"),
                mapper.map(new GetLastServiceOperationRequest("serviceInstanceId")));


    }

}