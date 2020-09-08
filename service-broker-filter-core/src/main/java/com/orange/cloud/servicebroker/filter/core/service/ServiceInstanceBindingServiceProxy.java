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
import com.orange.cloud.servicebroker.filter.core.service.mapper.ServiceInstanceBindingRequestMapper;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Basic implementation to proxy requests to create and delete service instance bindings.
 *
 * @author Sebastien Bortolussi
 */
@Profile("!offline-test-without-cf")
@Component
public class ServiceInstanceBindingServiceProxy implements ServiceInstanceBindingService {

    private final ServiceInstanceBindingServiceClient client;

    private final ServiceInstanceBindingFilterRunner filterRunner;

    private final ServiceInstanceBindingRequestMapper mapper;

    @Autowired
    public ServiceInstanceBindingServiceProxy(ServiceInstanceBindingServiceClient client, ServiceInstanceBindingFilterRunner filterRunner, ServiceInstanceBindingRequestMapper mapper) {
        this.client = client;
        this.filterRunner = filterRunner;
        this.mapper = mapper;
    }

    @Override
    public Mono<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
        preBinding(request);
        final CreateServiceInstanceBindingRequest req = mapper.map(request);
        final ResponseEntity<CreateServiceInstanceAppBindingResponse> response = client.createServiceInstanceBinding(req.getServiceInstanceId(), req.getBindingId(), OsbConstants.X_Broker_API_Version_Value,req);
        postBinding(request, response.getBody());
        return Mono.just(response.getBody());
    }


    @Override
    public Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
        preUnbinding(request);
        final DeleteServiceInstanceBindingRequest req = mapper.map(request);
        client.deleteServiceInstanceBinding(req.getServiceInstanceId(), req.getBindingId(), req.getServiceDefinitionId(), req.getPlanId(), OsbConstants.X_Broker_API_Version_Value);
        postUnbinding(request);
        return Mono.just(DeleteServiceInstanceBindingResponse.builder().build());
    }

    private void postBinding(CreateServiceInstanceBindingRequest request, CreateServiceInstanceAppBindingResponse response) {
        filterRunner.postBind(request, response);
    }

    private void preBinding(CreateServiceInstanceBindingRequest request) {
        filterRunner.preBind(request);
    }

    private void postUnbinding(DeleteServiceInstanceBindingRequest request) {
        filterRunner.postUnbind(request, null);
    }

    private void preUnbinding(DeleteServiceInstanceBindingRequest request) {
        filterRunner.preUnbind(request);
    }
}
