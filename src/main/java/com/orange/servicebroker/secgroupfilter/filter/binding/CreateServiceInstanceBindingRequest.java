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

package com.orange.servicebroker.secgroupfilter.filter.binding;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;

/**
 * Details of a request to bind to a service instance binding.
 *
 * @author sgreenberg@pivotal.io
 * @author Scott Frederick
 */
@Getter
@ToString
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreateServiceInstanceBindingRequest {
    /**
     * The ID of the service being bound, from the broker catalog.
     */
    @NotEmpty
    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceDefinitionId;

    /**
     * The ID of the plan being bound within the service, from the broker catalog.
     */
    @NotEmpty
    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    /**
     * The Cloud Controller GUID of the application the service instance will be bound to. Will be provided when
     * users bind applications to service instances, or <code>null</code> if an application is not being bound.
     *
     * @deprecated The <code>bindResource</code> field will contain references to the resource being bound, and should
     * be used instead of this field.
     */
    @JsonSerialize
    @JsonProperty("app_guid")
    private String appGuid;

    /**
     * The resource being bound to the service instance.
     */
    @JsonSerialize
    @JsonProperty("bind_resource")
    private Map<String, Object> bindResource;

    /**
     * Parameters passed by the user in the form of a JSON structure. The service broker is responsible
     * for validating the contents of the parameters for correctness or applicability.
     */
    @JsonSerialize
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    /**
     * The Cloud Controller GUID of the service instance to being bound.
     */
    @JsonIgnore
    private transient String serviceInstanceId;

    /**
     * The Cloud Controller GUID of the service binding being created. This ID will be used for future
     * requests for the same service instance binding, so the broker must use it to correlate any resource it creates.
     */
    @JsonIgnore
    private transient String bindingId;


}
