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

package com.orange.cloud.servicebroker.filter.core.filters;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Scan and run all filters that should be processed around service instance instance binding.
 *
 * @author Sebastien Bortolussi
 */
@Component
@Setter
public class ServiceInstanceBindingFilterRunner {

    @Autowired(required = false)
    private List<CreateServiceInstanceBindingPreFilter> createServiceInstanceBindingPreFilters;

    @Autowired(required = false)
    private List<CreateServiceInstanceBindingPostFilter> createServiceInstanceBindingPostFilters;

    @Autowired(required = false)
    private List<DeleteServiceInstanceBindingPreFilter> deleteServiceInstanceBindingPreFilters;

    @Autowired(required = false)
    private List<DeleteServiceInstanceBindingPostFilter> deleteServiceInstanceBindingPostFilters;

    /**
     * Run all filters that should be processed before a service instance instance binding has been created.
     *
     * @param request details of a request to bind to a service instance binding.
     */
    public void preBind(CreateServiceInstanceBindingRequest request) {
        Optional.ofNullable(createServiceInstanceBindingPreFilters)
                .ifPresent(serviceBrokerFilters -> serviceBrokerFilters.forEach(filter -> filter.run(request)));
    }

    /**
     * Run all filters that should be processed after a service instance instance binding has been created.
     *
     * @param request details of a request to bind to a service instance binding.
     */
    public void postBind(CreateServiceInstanceBindingRequest request, CreateServiceInstanceAppBindingResponse response) {
        Optional.ofNullable(createServiceInstanceBindingPostFilters)
                .ifPresent(serviceBrokerFilters -> serviceBrokerFilters.forEach(filter -> filter.run(request, response)));
    }

    /**
     * Run all filters that should be processed before a service instance instance binding has been deleted.
     *
     * @param request details of a request to delete a service instance binding.
     */
    public void preUnbind(DeleteServiceInstanceBindingRequest request) {
        Optional.ofNullable(deleteServiceInstanceBindingPreFilters)
                .ifPresent(serviceBrokerFilters -> serviceBrokerFilters.forEach(filter -> filter.run(request)));
    }

    /**
     * Run all filters that should be processed after a service instance instance binding has been deleted.
     *
     * @param request details of a request to delete a service instance binding.
     */
    public void postUnbind(DeleteServiceInstanceBindingRequest request, Void response) {
        Optional.ofNullable(deleteServiceInstanceBindingPostFilters)
                .ifPresent(serviceBrokerFilters -> serviceBrokerFilters.forEach(filter -> filter.run(request, response)));
    }

}
