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
 * Because filter broker" catalog and "target broker" catalog may be different,
 * we need to map incomming request (to "filter broker") to target broker request.
 * This may be required for field such as plandId or ServiceDefinitionId.
 *
 * @author Sebastien Bortolussi
 */
public interface ServiceInstanceRequestMapper {

    CreateServiceInstanceRequest map(CreateServiceInstanceRequest request);

    DeleteServiceInstanceRequest map(DeleteServiceInstanceRequest request);

    UpdateServiceInstanceRequest map(UpdateServiceInstanceRequest request);

    GetLastServiceOperationRequest map(GetLastServiceOperationRequest request);
}
