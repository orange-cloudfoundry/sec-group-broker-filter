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
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Basic implementation to proxy requests related to provisioning, updating,
 * and deprovisioning service instances (using {@link <a href="http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign">Feign</a>})
 *
 * @author Sebastien Bortolussi
 */
@Profile("!offline-test-without-cf")
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
    public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
        final CreateServiceInstanceRequest req = mapper.map(request);
        final ResponseEntity<CreateServiceInstanceResponse> serviceInstanceResponse = serviceClient.createServiceInstance(req.getServiceInstanceId(), req, req.isAsyncAccepted(), OsbConstants.X_Broker_API_Version_Value);
        return Mono.just(serviceInstanceResponse.getBody());
    }

    @Override
    public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
        final GetLastServiceOperationRequest req = mapper.map(request);
        final ResponseEntity<GetLastServiceOperationResponse> response = serviceClient.getServiceInstanceLastOperation(req.getServiceInstanceId(), OsbConstants.X_Broker_API_Version_Value);
        return Mono.just(response.getBody());
    }

    @Override
    public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
        final DeleteServiceInstanceRequest req = mapper.map(request);
        final ResponseEntity<DeleteServiceInstanceResponse> response = serviceClient.deleteServiceInstance(req.getServiceInstanceId(), req.getServiceDefinitionId(), req.getPlanId(), req.isAsyncAccepted(), OsbConstants.X_Broker_API_Version_Value);
        return Mono.just(response.getBody());
    }

    @Override
    public Mono<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request) {
        final UpdateServiceInstanceRequest req = mapper.map(request);
        final ResponseEntity<UpdateServiceInstanceResponse> response = serviceClient.updateServiceInstance(req.getServiceInstanceId(), req, req.isAsyncAccepted(), OsbConstants.X_Broker_API_Version_Value);
        return Mono.just(response.getBody());

    }
}
