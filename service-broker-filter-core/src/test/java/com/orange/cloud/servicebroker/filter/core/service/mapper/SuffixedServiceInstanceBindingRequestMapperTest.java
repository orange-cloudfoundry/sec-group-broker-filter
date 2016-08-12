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
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastien Bortolussi
 */
public class SuffixedServiceInstanceBindingRequestMapperTest {

    @Test
    public void should_map_create_service_instance_binding_request() throws Exception {

        SuffixedServiceInstanceBindingRequestMapper mapper = new SuffixedServiceInstanceBindingRequestMapper("-suffix");

        Map<String, Object> bindResource = new HashMap<>();
        bindResource.put("resource1", "value1");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("parameter1", "value1");

        Assert.assertEquals(new CreateServiceInstanceBindingRequest("serviceDefinitionId",
                        "planId",
                        "appGuid",
                        bindResource,
                        parameters)
                        .withBindingId("bindingId")
                        .withServiceInstanceId("serviceInstanceId"),
                mapper.map(new CreateServiceInstanceBindingRequest("serviceDefinitionId-suffix",
                        "planId-suffix",
                        "appGuid",
                        bindResource,
                        parameters)
                        .withBindingId("bindingId")
                        .withServiceInstanceId("serviceInstanceId")));
    }

    @Test
    public void should_map_delete_service_instance_binding_request() throws Exception {
        SuffixedServiceInstanceBindingRequestMapper mapper = new SuffixedServiceInstanceBindingRequestMapper("-suffix");

        Assert.assertEquals(new DeleteServiceInstanceBindingRequest("serviceInstanceId",
                        "bindingId",
                        "serviceDefinitionId",
                        "planId",
                        null),
                mapper.map(new DeleteServiceInstanceBindingRequest("serviceInstanceId",
                        "bindingId",
                        "serviceDefinitionId-suffix",
                        "planId-suffix",
                        null)));

    }

}