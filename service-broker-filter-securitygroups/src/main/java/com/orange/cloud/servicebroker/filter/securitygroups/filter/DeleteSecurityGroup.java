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

package com.orange.cloud.servicebroker.filter.securitygroups.filter;

import com.orange.cloud.servicebroker.filter.core.filters.DeleteServiceInstanceBindingPostFilter;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.securitygroups.DeleteSecurityGroupRequest;
import org.cloudfoundry.client.v2.securitygroups.ListSecurityGroupsRequest;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroupResource;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Component
public class DeleteSecurityGroup implements DeleteServiceInstanceBindingPostFilter {

    private CloudFoundryClient cloudFoundryClient;

    @Autowired
    public DeleteSecurityGroup(CloudFoundryClient cloudFoundryClient) {
        this.cloudFoundryClient = cloudFoundryClient;
    }

    private Mono<SecurityGroupResource> getSecurityGroupId(String securityGroup) {
        return requestSecurityGroups(securityGroup)
                .single()
                .otherwise(NoSuchElementException.class, t -> {
                    log.warn("Cannot find any security group with name {} to delete.", securityGroup);
                    return Mono.empty();
                });
    }

    private Flux<SecurityGroupResource> requestSecurityGroups(String securityGroup) {
        return PaginationUtils
                .requestClientV2Resources(page -> cloudFoundryClient.securityGroups()
                        .list(ListSecurityGroupsRequest.builder()
                                .name(securityGroup)
                                .page(page)
                                .build()));
    }

    @Override
    public void run(DeleteServiceInstanceBindingRequest request, Void response) {
        securityGroupId(request.getBindingId())
                .then(securityGroupId -> cloudFoundryClient.securityGroups()
                        .delete(DeleteSecurityGroupRequest.builder()
                                .securityGroupId(securityGroupId)
                                .build()))
                .doOnError(resp -> log.error("Fail to delete security group {}", request.getBindingId()))
                .timeout(Duration.ofSeconds(60))
                .subscribe(resp -> log.debug("Security group {} deleted", request.getBindingId()));

    }

    private Mono<String> securityGroupId(String securityGroup) {
        return Optional.ofNullable(securityGroup)
                .map(securityGroupName -> getSecurityGroupId(securityGroupName)
                        .map(ResourceUtils::getId))
                .orElse(Mono.error(new IllegalStateException("No security group to delete")));
    }

}
