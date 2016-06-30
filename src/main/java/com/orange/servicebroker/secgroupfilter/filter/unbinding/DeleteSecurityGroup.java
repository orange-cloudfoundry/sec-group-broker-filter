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

import com.orange.servicebroker.secgroupfilter.filter.Action;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.client.v2.securitygroups.DeleteSecurityGroupRequest;
import org.cloudfoundry.client.v2.securitygroups.ListSecurityGroupsRequest;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroupResource;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroups;
import org.cloudfoundry.util.ExceptionUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
public class DeleteSecurityGroup implements Action<DeleteServiceInstanceBindingRequest, Void> {

    private static SecurityGroups securityGroups;

    public DeleteSecurityGroup(SecurityGroups securityGroups) {
        this.securityGroups = securityGroups;
    }

    private static Mono<SecurityGroupResource> getSecurityGroup(String securityGroup) {
        return requestSecurityGroups(securityGroup)
                .single()
                .otherwise(NoSuchElementException.class, t -> ExceptionUtils.illegalArgument("Security Group %s does not exist", securityGroup));
    }

    private static Flux<SecurityGroupResource> requestSecurityGroups(String securityGroup) {
        return PaginationUtils
                .requestResources(page -> securityGroups
                        .list(ListSecurityGroupsRequest.builder()
                                .name(securityGroup)
                                .page(page)
                                .build()));
    }

    @Override
    public void invoke(DeleteServiceInstanceBindingRequest request, Void response) {
        securityGroupId(request.getBindingId())
                .then(securityGroupId -> securityGroups.delete(DeleteSecurityGroupRequest.builder()
                        .securityGroupId(securityGroupId)
                        .build()))
                .toFuture().thenAccept(resp -> log.debug("security group deleted"));

    }

    Mono<String> securityGroupId(String securityGroup) {
        return Optional.ofNullable(securityGroup)
                .map(securityGroupName -> getSecurityGroup(securityGroupName)
                        .map(ResourceUtils::getId)
                        .cache())
                .orElse(Mono.error(new IllegalStateException("No security group to delete")));
    }

}
