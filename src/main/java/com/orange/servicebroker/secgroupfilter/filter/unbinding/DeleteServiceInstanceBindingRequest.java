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

package com.orange.servicebroker.secgroupfilter.filter.unbinding;

import lombok.*;

/**
 * Details of a request to delete a service instance binding.
 *
 * @author krujos
 * @author Scott Frederick
 */
@Getter
@ToString
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DeleteServiceInstanceBindingRequest {

    /**
     * The Cloud Controller GUID of the service instance to being unbound.
     */
    private String serviceInstanceId;

    /**
     * The Cloud Controller GUID of the service binding being deleted.
     */
    private String bindingId;

}
