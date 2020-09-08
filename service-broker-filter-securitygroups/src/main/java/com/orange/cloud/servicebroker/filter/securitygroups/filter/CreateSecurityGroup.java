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
import com.orange.cloud.servicebroker.filter.securitygroups.domain.Destination;
import com.orange.cloud.servicebroker.filter.securitygroups.domain.TrustedDestinationSpecification;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.GetApplicationRequest;
import org.cloudfoundry.client.v2.applications.GetApplicationResponse;
import org.cloudfoundry.client.v2.securitygroups.CreateSecurityGroupRequest;
import org.cloudfoundry.client.v2.securitygroups.Protocol;
import org.cloudfoundry.client.v2.securitygroups.RuleEntity;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroupEntity;
import org.cloudfoundry.client.v2.servicebrokers.GetServiceBrokerRequest;
import org.cloudfoundry.client.v2.servicebrokers.ServiceBrokerEntity;
import org.cloudfoundry.client.v2.serviceinstances.GetServiceInstanceRequest;
import org.cloudfoundry.client.v2.serviceinstances.ServiceInstanceEntity;
import org.cloudfoundry.client.v2.serviceplans.GetServicePlanRequest;
import org.cloudfoundry.client.v2.serviceplans.ServicePlanEntity;
import org.cloudfoundry.client.v2.services.GetServiceRequest;
import org.cloudfoundry.client.v2.services.ServiceEntity;
import org.cloudfoundry.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.CloudFoundryContext;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.stream.Collectors;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

@Component
public class CreateSecurityGroup implements CreateServiceInstanceBindingPostFilter, ServiceBrokerPostFilter<CreateServiceInstanceBindingRequest, CreateServiceInstanceAppBindingResponse> {

    private static final Logger log = LoggerFactory.getLogger(CreateSecurityGroup.class);

    static final Protocol DEFAULT_PROTOCOL = Protocol.TCP;
    private CloudFoundryClient cloudFoundryClient;
    private TrustedDestinationSpecification trustedDestinationSpecification;

    @Autowired
    public CreateSecurityGroup(CloudFoundryClient cloudFoundryClient, TrustedDestinationSpecification trustedDestinationSpecification) {
        this.cloudFoundryClient = cloudFoundryClient;
        this.trustedDestinationSpecification = trustedDestinationSpecification;
    }

    private static Mono<ServiceEntity> getService(CloudFoundryClient cloudFoundryClient, String serviceId) {
        return cloudFoundryClient.services()
                .get(GetServiceRequest.builder()
                        .serviceId(serviceId)
                        .build()
                )
                .map(ResourceUtils::getEntity);
    }

    private static Mono<ServiceBrokerEntity> getServiceBroker(CloudFoundryClient cloudFoundryClient, String serviceBrokerId) {
        return cloudFoundryClient.serviceBrokers().get(GetServiceBrokerRequest.builder()
                .serviceBrokerId(serviceBrokerId).build())
                .map(ResourceUtils::getEntity);
    }

    private static Mono<ServiceInstanceEntity> getServiceInstance(CloudFoundryClient cloudFoundryClient, String serviceInstanceId) {
        return cloudFoundryClient.serviceInstances()
                .get(GetServiceInstanceRequest.builder()
                        .serviceInstanceId(serviceInstanceId)
                        .build()
                )
                .map(ResourceUtils::getEntity);
    }

    private static Mono<String> getRuleDescription(CloudFoundryClient cloudFoundryClient, String bindingId, String serviceInstanceId) {
        return getServiceInstance(cloudFoundryClient, serviceInstanceId)
                .flatMap(serviceInstance -> Mono.zip(
                        Mono.just(bindingId),
                        Mono.just(serviceInstance),
                        getPlan(cloudFoundryClient, serviceInstance.getServicePlanId())
                            .map(ServicePlanEntity::getServiceId)
                ))
                .flatMap(function((serviceBindingId, serviceInstance, serviceId) -> Mono.zip(
                    Mono.just(serviceBindingId),
                    Mono.just(serviceInstance),
                    getService(cloudFoundryClient, serviceId)
                        .map(ServiceEntity::getServiceBrokerId))))
                .flatMap(function((serviceBindingId, serviceInstance, serviceBrokerId) -> Mono.zip(
                    Mono.just(serviceBindingId),
                    Mono.just(serviceInstance),
                    getServiceBrokerName(cloudFoundryClient, serviceBrokerId))))
                .map(function((serviceBindingId, serviceInstance, serviceBrokerName) -> ImmutableRuleDescription.builder()
                        .servicebindingId(serviceBindingId)
                        .serviceInstanceName(serviceInstance.getName())
                        .serviceBrokerName(serviceBrokerName).build()
                        .value()));
    }

    private static Mono<ServicePlanEntity> getPlan(CloudFoundryClient cloudFoundryClient, String planId) {
        return cloudFoundryClient.servicePlans()
                .get(GetServicePlanRequest.builder()
                        .servicePlanId(planId)
                        .build()
                )
                .map(ResourceUtils::getEntity);
    }

    private static Mono<String> getServiceBrokerName(CloudFoundryClient cloudFoundryClient, String serviceBrokerId) {
        return getServiceBroker(cloudFoundryClient, serviceBrokerId)
                .map(ServiceBrokerEntity::getName);
    }

    @Override
    public void run(CreateServiceInstanceBindingRequest request, CreateServiceInstanceAppBindingResponse response) {
        Assert.notNull(response, "expecting a non-null response");
        Assert.notNull(response.getCredentials(), "expecting a non-null response credentials");
        Assert.notNull(request.getContext(), "expecting a non-null OSB context");
        Assert.isInstanceOf(CloudFoundryContext.class, request.getContext(), "only supporting CloudFoundry " +
            "clients, but received unsupported context in OSB request");

        final Destination destination = ConnectionInfoFactory.fromCredentials(response.getCredentials());

        if (!trustedDestinationSpecification.isSatisfiedBy(destination)) {
            log.warn("Cannot open security group for destination {}. Destination is out of allowed range [{}].", destination, trustedDestinationSpecification);
            throw new NotAllowedDestination(destination);
        }
        log.debug("creating security group for credentials {}.", response.getCredentials());
        try {
            CloudFoundryContext cloudFoundryContext = (CloudFoundryContext) request.getContext();
            final SecurityGroupEntity securityGroup = Mono.zip(
                    getRuleDescription(cloudFoundryClient, request.getBindingId(), request.getServiceInstanceId()),
                    Mono.just(cloudFoundryContext.getSpaceGuid())
            ).flatMap(function((description, spaceId) -> create(getSecurityGroupName(request), destination, description
                , spaceId)))
                    .doOnError(t -> log.error("Fail to create security group. Error details {}", t.toString(), t))
                    .block();

            assert securityGroup != null: "mono should not return empty";
            log.debug("Security Group {} created", securityGroup.getName());
        } catch (Exception e) {
            log.error("Fail to create Security Group. Error details {}", e.toString(), e);
            ReflectionUtils.rethrowRuntimeException(e);
        }

    }

    private Mono<SecurityGroupEntity> create(String securityGroupName, Destination destination, String description, String spaceId) {
        return getRules(destination, description)
                .flatMap(rules ->
                        cloudFoundryClient.securityGroups()
                                .create(CreateSecurityGroupRequest.builder()
                                        .name(securityGroupName)
                                        .rules(rules)
                                        .spaceId(spaceId)
                                        .build()))
                .map(ResourceUtils::getEntity)
                .checkpoint();
    }

    private Mono<List<RuleEntity>> getRules(Destination destination, String description) {
        return Mono.justOrEmpty(destination.getIPs()
                .map(ip -> RuleEntity.builder()
                        .protocol(DEFAULT_PROTOCOL)
                        .destination(ip)
                        .description(description)
                        .ports(String.valueOf(destination.getPort().value()))
                        .build())
                .collect(Collectors.toList()))
                .checkpoint();
    }

    private String getSecurityGroupName(CreateServiceInstanceBindingRequest request) {
        return request.getBindingId();
    }

    public class NotAllowedDestination extends RuntimeException {

        public NotAllowedDestination(Destination destination) {
            super(String.format("Cannot open security group for destination %s. Destination is out of allowed range.", destination));
        }
    }
}
