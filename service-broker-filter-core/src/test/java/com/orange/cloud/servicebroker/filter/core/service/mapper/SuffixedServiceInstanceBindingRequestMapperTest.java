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

import org.springframework.cloud.servicebroker.model.binding.BindResource;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;

/**
 * @author Sebastien Bortolussi
 */
public class SuffixedServiceInstanceBindingRequestMapperTest {

    @Test
    public void should_map_create_service_instance_binding_request() {

        SuffixedServiceInstanceBindingRequestMapper mapper = new SuffixedServiceInstanceBindingRequestMapper("-suffix");

        Map<String, Object> bindResource = new HashMap<>();
        bindResource.put("resource1", "value1");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("parameter1", "value1");

        Assert.assertEquals(CreateServiceInstanceBindingRequest.builder()
                .serviceDefinitionId("serviceDefinitionId")
                .planId("planId")
                .bindResource(BindResource.builder()
                    .properties(bindResource)
                    .appGuid("appGuid")
                    .build())
                .parameters(parameters)
                .bindingId("bindingId")
                .serviceInstanceId("serviceInstanceId")
                .build(),

            mapper.map(
                CreateServiceInstanceBindingRequest.builder()
                    .serviceDefinitionId("serviceDefinitionId-suffix")
                    .planId("planId-suffix")
                    .bindResource(BindResource.builder()
                        .properties(bindResource)
                        .appGuid("appGuid")
                        .build())
                    .parameters(parameters)
                    .bindingId("bindingId")
                    .serviceInstanceId("serviceInstanceId")
                    .build()));
    }

    @Test
    public void should_map_delete_service_instance_binding_request() {
        SuffixedServiceInstanceBindingRequestMapper mapper = new SuffixedServiceInstanceBindingRequestMapper("-suffix");

        Assert.assertEquals(
            DeleteServiceInstanceBindingRequest.builder()
                .serviceInstanceId("serviceInstanceId")
                .bindingId("bindingId")
                .serviceDefinitionId("serviceDefinitionId")
                .planId("planId")
                .build(),
            mapper.map(
                DeleteServiceInstanceBindingRequest.builder()
                .serviceInstanceId("serviceInstanceId")
                .bindingId("bindingId")
                .serviceDefinitionId("serviceDefinitionId-suffix")
                .planId("planId-suffix")
                .build()));

    }

}