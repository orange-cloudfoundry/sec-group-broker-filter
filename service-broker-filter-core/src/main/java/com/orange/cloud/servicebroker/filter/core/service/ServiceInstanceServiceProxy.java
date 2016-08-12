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

import com.orange.cloud.servicebroker.filter.core.service.mapper.ServiceInstanceRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Basic implementation to proxy requests related to provisioning, updating,
 * and deprovisioning service instances (using {@link <a href="http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign">Feign</a>})
 *
 * @author Sebastien Bortolussi
 */
@Service
public class ServiceInstanceServiceProxy implements ServiceInstanceService {

    private ServiceInstanceServiceClient serviceClient;

    private ServiceInstanceRequestMapper mapper;

    @Autowired
    public ServiceInstanceServiceProxy(ServiceInstanceServiceClient serviceClient, ServiceInstanceRequestMapper mapper) {
        this.serviceClient = serviceClient;
        this.mapper = mapper;
    }

    @Override
    public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
        final CreateServiceInstanceRequest req = mapper.map(request);
        final ResponseEntity<CreateServiceInstanceResponse> serviceInstanceResponse = serviceClient.createServiceInstance(req.getServiceInstanceId(), req, req.isAsyncAccepted());
        return serviceInstanceResponse.getBody();
    }

    @Override
    public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
        final GetLastServiceOperationRequest req = mapper.map(request);
        final ResponseEntity<GetLastServiceOperationResponse> response = serviceClient.getServiceInstanceLastOperation(req.getServiceInstanceId());
        return response.getBody();
    }

    @Override
    public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
        final DeleteServiceInstanceRequest req = mapper.map(request);
        final ResponseEntity<DeleteServiceInstanceResponse> response = serviceClient.deleteServiceInstance(req.getServiceInstanceId(), req.getServiceDefinitionId(), req.getPlanId(), req.isAsyncAccepted());
        return response.getBody();
    }

    @Override
    public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
        final UpdateServiceInstanceRequest req = mapper.map(request);
        final ResponseEntity<UpdateServiceInstanceResponse> response = serviceClient.updateServiceInstance(req.getServiceInstanceId(), req, req.isAsyncAccepted());
        return response.getBody();

    }
}
