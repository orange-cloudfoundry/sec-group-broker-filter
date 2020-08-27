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

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Sebastien Bortolussi
 */
@FeignClient(name = "instances", url = "${broker.filter.url}", configuration = FilteredBrokerFeignConfig.class)
public interface ServiceInstanceServiceClient {

    @RequestMapping(value = "/v2/service_instances/{instanceId}", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(@PathVariable("instanceId") String serviceInstanceId,
                                                                        @Valid @RequestBody CreateServiceInstanceRequest request,
                                                                        @RequestParam(value = "accepts_incomplete", required = false) boolean acceptsIncomplete,
                                                                        @RequestHeader(value = OsbConstants.X_Broker_API_Version, defaultValue = OsbConstants.X_Broker_API_Version_Value) String apiVersion);

    @RequestMapping(value = "/v2/service_instances/{instanceId}/last_operation", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<GetLastServiceOperationResponse> getServiceInstanceLastOperation(@PathVariable("instanceId") String serviceInstanceId,
                                                                                    @RequestHeader(value = OsbConstants.X_Broker_API_Version, defaultValue = OsbConstants.X_Broker_API_Version_Value) String apiVersion);

    @RequestMapping(value = "/v2/service_instances/{instanceId}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<DeleteServiceInstanceResponse> deleteServiceInstance(@PathVariable("instanceId") String serviceInstanceId,
                                                                        @RequestParam("service_id") String serviceDefinitionId,
                                                                        @RequestParam("plan_id") String planId,
                                                                        @RequestParam(value = "accepts_incomplete", required = false) boolean acceptsIncomplete,
                                                                        @RequestHeader(value = OsbConstants.X_Broker_API_Version, defaultValue = OsbConstants.X_Broker_API_Version_Value) String apiVersion);

    @RequestMapping(value = "/v2/service_instances/{instanceId}", method = RequestMethod.PATCH, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<UpdateServiceInstanceResponse> updateServiceInstance(@PathVariable("instanceId") String serviceInstanceId,
                                                                        @Valid @RequestBody UpdateServiceInstanceRequest request,
                                                                        @RequestParam(value = "accepts_incomplete", required = false) boolean acceptsIncomplete,
                                                                        @RequestHeader(value = OsbConstants.X_Broker_API_Version, defaultValue = OsbConstants.X_Broker_API_Version_Value) String apiVersion);
}
