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

import com.orange.cloud.servicebroker.filter.core.config.FilteredBrokerFeignConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Declarative web client of ServiceInstanceBindingService using {@link <a href="http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign">Feign</a>}
 *
 * @author Sebastien Bortolussi
 */
@FeignClient(name = "bindings", url = "${broker.filter.url}", configuration = FilteredBrokerFeignConfig.class)
public interface ServiceInstanceBindingServiceClient {

    @RequestMapping(value = "/v2/service_instances/{instanceId}/service_bindings/{bindingId}", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<CreateServiceInstanceAppBindingResponse> createServiceInstanceBinding(@PathVariable("instanceId") String serviceInstanceId,
                                                                                         @PathVariable("bindingId") String bindingId,
                                                                                         @Valid @RequestBody CreateServiceInstanceBindingRequest request);

    @RequestMapping(value = "/v2/service_instances/{instanceId}/service_bindings/{bindingId}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<String> deleteServiceInstanceBinding(@PathVariable("instanceId") String serviceInstanceId,
                                                        @PathVariable("bindingId") String bindingId,
                                                        @RequestParam("service_id") String serviceDefinitionId,
                                                        @RequestParam("plan_id") String planId);
}