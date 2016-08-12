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

import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;

/**
 * @author Sebastien Bortolussi
 */
public class SuffixedServiceInstanceBindingRequestMapper implements ServiceInstanceBindingRequestMapper {

    private String serviceOfferingSuffix;

    public SuffixedServiceInstanceBindingRequestMapper(String suffix) {
        this.serviceOfferingSuffix = suffix;
    }

    protected static String withoutSuffix(String s, String suffix) {
        return (s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : s);
    }

    @Override
    public CreateServiceInstanceBindingRequest map(CreateServiceInstanceBindingRequest request) {
        return new CreateServiceInstanceBindingRequest(withoutSuffix(request.getServiceDefinitionId(), serviceOfferingSuffix),
                withoutSuffix(request.getPlanId(), serviceOfferingSuffix),
                request.getAppGuid(),
                request.getBindResource(),
                request.getParameters())
                .withBindingId(request.getBindingId())
                .withServiceInstanceId(request.getServiceInstanceId());
    }

    @Override
    public DeleteServiceInstanceBindingRequest map(DeleteServiceInstanceBindingRequest request) {
        return new DeleteServiceInstanceBindingRequest(request.getServiceInstanceId(),
                request.getBindingId(),
                withoutSuffix(request.getServiceDefinitionId(), serviceOfferingSuffix),
                withoutSuffix(request.getPlanId(), serviceOfferingSuffix),
                null);
    }

}
