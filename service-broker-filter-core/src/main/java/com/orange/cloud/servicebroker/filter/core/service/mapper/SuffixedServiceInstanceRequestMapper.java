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

import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;

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
        return new CreateServiceInstanceRequest(withoutSuffix(request.getServiceDefinitionId(), serviceOfferingSuffix),
                withoutSuffix(request.getPlanId(), serviceOfferingSuffix),
                request.getOrganizationGuid(),
                request.getSpaceGuid(),
                request.getParameters())
                .withServiceInstanceId(request.getServiceInstanceId())
                .withAsyncAccepted(request.isAsyncAccepted());
    }

    @Override
    public DeleteServiceInstanceRequest map(DeleteServiceInstanceRequest request) {
        return new DeleteServiceInstanceRequest(request.getServiceInstanceId(),
                withoutSuffix(request.getServiceDefinitionId(), serviceOfferingSuffix),
                withoutSuffix(request.getPlanId(), serviceOfferingSuffix),
                null)
                .withAsyncAccepted(request.isAsyncAccepted());
    }

    @Override
    public UpdateServiceInstanceRequest map(UpdateServiceInstanceRequest request) {
        return new UpdateServiceInstanceRequest(withoutSuffix(request.getServiceDefinitionId(), serviceOfferingSuffix),
                withoutSuffix(request.getPlanId(), serviceOfferingSuffix), request.getParameters())
                .withServiceInstanceId(request.getServiceInstanceId())
                .withAsyncAccepted(request.isAsyncAccepted());
    }

    @Override
    public GetLastServiceOperationRequest map(GetLastServiceOperationRequest request) {
        return new GetLastServiceOperationRequest(request.getServiceInstanceId());
    }
}
