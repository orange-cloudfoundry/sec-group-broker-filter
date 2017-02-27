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

import com.orange.cloud.servicebroker.filter.core.filters.CreateServiceInstanceBindingPostFilter;
import com.orange.cloud.servicebroker.filter.core.filters.ServiceBrokerPostFilter;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.GetApplicationRequest;
import org.cloudfoundry.client.v2.applications.GetApplicationResponse;
import org.cloudfoundry.client.v2.securitygroups.CreateSecurityGroupRequest;
import org.cloudfoundry.client.v2.securitygroups.Protocol;
import org.cloudfoundry.client.v2.securitygroups.RuleEntity;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroupEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class CreateSecurityGroup implements CreateServiceInstanceBindingPostFilter, ServiceBrokerPostFilter<CreateServiceInstanceBindingRequest, CreateServiceInstanceAppBindingResponse> {

    static final Protocol DEFAULT_PROTOCOL = Protocol.TCP;
    private CloudFoundryClient cloudFoundryClient;

    @Autowired
    public CreateSecurityGroup(CloudFoundryClient cloudFoundryClient) {
        this.cloudFoundryClient = cloudFoundryClient;
    }

    @Override
    public void run(CreateServiceInstanceBindingRequest request, CreateServiceInstanceAppBindingResponse response) {
        Assert.notNull(response);
        Assert.notNull(response.getCredentials());

        final ConnectionInfo connectionInfo = ConnectionInfoFactory.fromCredentials(response.getCredentials());

        log.debug("creating security group for credentials {}.", response.getCredentials());

        try {
            final List<RuleEntity> rules = Stream.of(InetAddress.getAllByName(connectionInfo.getHost()))
                    .map(InetAddress::getHostAddress)
                    .map(ip -> RuleEntity.builder()
                            .protocol(DEFAULT_PROTOCOL)
                            .destination(ip)
                            .ports(String.valueOf(connectionInfo.getPort()))
                            .build())
                    .collect(Collectors.toList());

            log.debug("creating Security Group rules : {}.", rules);

            final SecurityGroupEntity securityGroup = spaceId(request)
                    .then(spaceId -> cloudFoundryClient.securityGroups()
                            .create(CreateSecurityGroupRequest.builder()
                                    .name(getSecurityGroupName(request))
                                    .rules(rules)
                                    .spaceId(spaceId)
                                    .build()))
                    .doOnError(t -> log.error("Fail to create security group. Error details {}", t))
                    .block(Duration.ofSeconds(60))
                    .getEntity();
            log.debug("Security Group {} created", securityGroup.getName());
        } catch (Exception e) {
            log.error("Fail to create Security Group. Error details {}", e);
            ReflectionUtils.rethrowRuntimeException(e);
        }

    }

    private String getSecurityGroupName(CreateServiceInstanceBindingRequest request) {
        return request.getBindingId();
    }

    private Mono<String> spaceId(CreateServiceInstanceBindingRequest request) {
        return cloudFoundryClient.applicationsV2().get(GetApplicationRequest.builder()
                .applicationId(request.getAppGuid())
                .build())
                .map(GetApplicationResponse::getEntity)
                .map(ApplicationEntity::getSpaceId);
    }

}
