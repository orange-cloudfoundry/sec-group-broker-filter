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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Basic implementation to proxy requests to create and delete service instance bindings.
 *
 * @author Sebastien Bortolussi
 */
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
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
        preBinding(request);
        final CreateServiceInstanceBindingRequest req = mapper.map(request);
        final ResponseEntity<CreateServiceInstanceAppBindingResponse> response = client.createServiceInstanceBinding(req.getServiceInstanceId(), req.getBindingId(), OsbConstants.X_Broker_API_Version_Value,req);
        postBinding(request, response.getBody());
        return response.getBody();
    }


    @Override
    public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
        preUnbinding(request);
        final DeleteServiceInstanceBindingRequest req = mapper.map(request);
        client.deleteServiceInstanceBinding(req.getServiceInstanceId(), req.getBindingId(), req.getServiceDefinitionId(), req.getPlanId(), OsbConstants.X_Broker_API_Version_Value);
        postUnbinding(request);
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
