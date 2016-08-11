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

package com.orange.cloud.servicebroker.filter.core.service;

import com.orange.cloud.servicebroker.filter.core.filters.ServiceInstanceBindingFilterRunner;
import com.orange.cloud.servicebroker.filter.core.service.mapper.DefaultServiceInstanceBindingRequestMapper;
import com.orange.cloud.servicebroker.filter.core.service.mapper.ServiceInstanceBindingRequestMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author Sebastien Bortolussi
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceInstanceBindingServiceProxyTest {

    @Mock
    private ServiceInstanceBindingServiceClient client;

    @Mock
    private ServiceInstanceBindingFilterRunner filterRunner;

    @Spy
    DefaultServiceInstanceBindingRequestMapper mapper;

    @InjectMocks
    private ServiceInstanceBindingServiceProxy serviceInstanceBindingServiceProxy;

    @Test
    public void should_proxy_create_service_instance_binding_request_to_filtered_broker() throws Exception {
        CreateServiceInstanceBindingRequest request = new CreateServiceInstanceBindingRequest()
                .withServiceInstanceId("instance_id")
                .withBindingId("binding_id");
        ResponseEntity<CreateServiceInstanceAppBindingResponse> response = new ResponseEntity<CreateServiceInstanceAppBindingResponse>(new CreateServiceInstanceAppBindingResponse(), HttpStatus.CREATED);
        Mockito.when(client.createServiceInstanceBinding("instance_id", "binding_id", request)).thenReturn(response);

        serviceInstanceBindingServiceProxy.createServiceInstanceBinding(request);

        Mockito.verify(client).createServiceInstanceBinding("instance_id", "binding_id", request);

    }

    @Test
    public void should_proxy_delete_service_instance_binding_request_to_filtered_broker() throws Exception {
        DeleteServiceInstanceBindingRequest request = new DeleteServiceInstanceBindingRequest("instance_id", "binding_id", "service_definition_id", "plan_id", null);

        serviceInstanceBindingServiceProxy.deleteServiceInstanceBinding(request);

        Mockito.verify(client).deleteServiceInstanceBinding("instance_id", "binding_id", "service_definition_id", "plan_id");
    }

}