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

import org.springframework.cloud.servicebroker.model.CloudFoundryContext;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;

/**
 * @author Sebastien Bortolussi
 */
public class SuffixedServiceInstanceRequestMapper implements ServiceInstanceRequestMapper {

    private String serviceOfferingSuffix;

    public SuffixedServiceInstanceRequestMapper(String serviceOfferingSuffix) {
        this.serviceOfferingSuffix = serviceOfferingSuffix;
    }

    protected static String withoutSuffix(String s, String suffix) {
        return (s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : s);
    }

    @Override
    public CreateServiceInstanceRequest map(CreateServiceInstanceRequest request) {
        return CreateServiceInstanceRequest.builder()
            .serviceDefinitionId(withoutSuffix(request.getServiceDefinitionId(), serviceOfferingSuffix))
            .planId(withoutSuffix(request.getPlanId(), serviceOfferingSuffix))
            .context(CloudFoundryContext.builder()
                .organizationGuid(request.getOrganizationGuid())
                .spaceGuid(request.getSpaceGuid())
                .build())
            .parameters(request.getParameters())
            .serviceInstanceId(request.getServiceInstanceId())
            .asyncAccepted(request.isAsyncAccepted())
            .build();
    }

    @Override
    public DeleteServiceInstanceRequest map(DeleteServiceInstanceRequest request) {
        return DeleteServiceInstanceRequest.builder()
            .serviceInstanceId(request.getServiceInstanceId())
            .serviceDefinitionId(withoutSuffix(request.getServiceDefinitionId(), serviceOfferingSuffix))
            .planId(withoutSuffix(request.getPlanId(), serviceOfferingSuffix))
            .asyncAccepted(request.isAsyncAccepted())
            .build();
    }

    @Override
    public UpdateServiceInstanceRequest map(UpdateServiceInstanceRequest request) {
        return UpdateServiceInstanceRequest.builder()
            .serviceDefinitionId(withoutSuffix(request.getServiceDefinitionId(), serviceOfferingSuffix))
            .planId(withoutSuffix(request.getPlanId(), serviceOfferingSuffix))
            .parameters(request.getParameters())
            .serviceInstanceId(request.getServiceInstanceId())
            .asyncAccepted(request.isAsyncAccepted())
            .build();
    }

    @Override
    public GetLastServiceOperationRequest map(GetLastServiceOperationRequest request) {
        return GetLastServiceOperationRequest.builder()
            .serviceInstanceId(request.getServiceInstanceId())
            .build();
    }
}
