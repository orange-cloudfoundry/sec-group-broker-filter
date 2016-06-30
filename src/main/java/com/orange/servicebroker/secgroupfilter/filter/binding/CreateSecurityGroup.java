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

import com.orange.servicebroker.secgroupfilter.filter.Action;
import com.orange.servicebroker.secgroupfilter.filter.CloudFoundryCredentialsUtils;
import com.orange.servicebroker.secgroupfilter.filter.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.ApplicationsV2;
import org.cloudfoundry.client.v2.applications.GetApplicationRequest;
import org.cloudfoundry.client.v2.applications.GetApplicationResponse;
import org.cloudfoundry.client.v2.securitygroups.CreateSecurityGroupRequest;
import org.cloudfoundry.client.v2.securitygroups.RuleEntity;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroups;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CreateSecurityGroup implements Action<CreateServiceInstanceBindingRequest, CreateServiceInstanceBindingResponse> {

    private SecurityGroups securityGroups;

    private ApplicationsV2 applicationsV2;

    public CreateSecurityGroup(SecurityGroups securityGroups, ApplicationsV2 applicationsV2) {
        this.securityGroups = securityGroups;
        this.applicationsV2 = applicationsV2;
    }

    @Override
    public void invoke(CreateServiceInstanceBindingRequest request, CreateServiceInstanceBindingResponse response) {
        Assert.notNull(response);
        Assert.notNull(response.getCredentials());
        UriInfo uriInfo = new UriInfo(CloudFoundryCredentialsUtils.getUriFromCredentials(response.getCredentials()));

        Assert.hasText(uriInfo.getHost(), "cannot find hostname credential in unbinding response : " + response.getCredentials());
        Assert.hasText(uriInfo.getPort(), "cannot find port credential in unbinding response" + response.getCredentials());

        log.debug("creating security group for : " + response.getCredentials());

        try {
            final List<RuleEntity> rules = Stream.of(InetAddress.getAllByName(uriInfo.getHost()))
                    .map(InetAddress::getHostAddress)
                    .map(ip -> RuleEntity.builder()
                            .protocol("tcp")
                            .destination(ip)
                            .ports(uriInfo.getPort())
                            .build())
                    .collect(Collectors.toList());

            log.debug("creating security group rules : %s.", rules);

            spaceId(request)
                    .then(spaceId -> securityGroups.create(CreateSecurityGroupRequest.builder()
                            .name(getSecurityGroupName(request))
                            .rules(rules)
                            .spaceId(spaceId)
                            .build()))
                    .toFuture()
                    .thenAccept(resp -> log.debug("security group created"));


        } catch (UnknownHostException e) {
            log.error("Failed to create security group. Error details {}", e);
            ReflectionUtils.rethrowRuntimeException(e);
        }
    }

    private String getSecurityGroupName(CreateServiceInstanceBindingRequest request) {
        return request.getBindingId();
    }

    private Mono<String> spaceId(CreateServiceInstanceBindingRequest request) {
        return applicationsV2.get(GetApplicationRequest.builder()
                .applicationId(request.getAppGuid())
                .build())
                .map(GetApplicationResponse::getEntity)
                .map(ApplicationEntity::getSpaceId);
    }

}
