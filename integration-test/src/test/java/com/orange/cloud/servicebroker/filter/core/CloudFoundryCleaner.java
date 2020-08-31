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

package com.orange.cloud.servicebroker.filter.core;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.DeleteApplicationRequest;
import org.cloudfoundry.client.v2.applications.ListApplicationServiceBindingsRequest;
import org.cloudfoundry.client.v2.applications.ListApplicationsRequest;
import org.cloudfoundry.client.v2.applications.RemoveApplicationServiceBindingRequest;
import org.cloudfoundry.client.v2.domains.GetDomainRequest;
import org.cloudfoundry.client.v2.jobs.JobEntity;
import org.cloudfoundry.client.v2.routes.DeleteRouteRequest;
import org.cloudfoundry.client.v2.routes.ListRoutesRequest;
import org.cloudfoundry.client.v2.servicebrokers.DeleteServiceBrokerRequest;
import org.cloudfoundry.client.v2.servicebrokers.ListServiceBrokersRequest;
import org.cloudfoundry.client.v2.serviceinstances.DeleteServiceInstanceRequest;
import org.cloudfoundry.client.v2.serviceinstances.GetServiceInstanceRequest;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesRequest;
import org.cloudfoundry.client.v2.spaces.DeleteSpaceRequest;
import org.cloudfoundry.client.v2.spaces.ListSpacesRequest;
import org.cloudfoundry.util.JobUtils;
import org.cloudfoundry.util.LastOperationUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLException;
import java.time.Duration;

import static org.cloudfoundry.util.tuple.TupleUtils.function;
import static org.cloudfoundry.util.tuple.TupleUtils.predicate;

/**
 * credits to <a href="https://github.com/cloudfoundry/cf-java-client/tree/master/integration-test">cf-java-client IT</a>
 */
final class CloudFoundryCleaner {

    private final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    private final CloudFoundryClient cloudFoundryClient;

    CloudFoundryCleaner(CloudFoundryClient cloudFoundryClient) {
        this.cloudFoundryClient = cloudFoundryClient;
    }

    private static Flux<Void> cleanApplicationsV2(CloudFoundryClient cloudFoundryClient) {
        return PaginationUtils.
                requestClientV2Resources(page -> cloudFoundryClient.applicationsV2()
                        .list(ListApplicationsRequest.builder()
                                .page(page)
                                .build()))
                .filter(application -> ResourceUtils.getEntity(application).getName().startsWith("test-application-"))
                .map(ResourceUtils::getId)
                .flatMap(applicationId -> removeServiceBindings(cloudFoundryClient, applicationId)
                        .thenMany(Flux.just(applicationId)))
                .flatMap(applicationId -> cloudFoundryClient.applicationsV2()
                        .delete(DeleteApplicationRequest.builder()
                                .applicationId(applicationId)
                                .build()));
    }

    private static Flux<Void> cleanRoutes(CloudFoundryClient cloudFoundryClient) {
        return PaginationUtils.
                requestClientV2Resources(page -> cloudFoundryClient.routes()
                        .list(ListRoutesRequest.builder()
                                .page(page)
                                .build()))
                .flatMap(route -> Mono.zip(
                        Mono.just(route),
                        cloudFoundryClient.domains()
                                .get(GetDomainRequest.builder()
                                        .domainId(ResourceUtils.getEntity(route).getDomainId())
                                        .build())
                ))
                .filter(predicate((route, domain) ->
                        ResourceUtils.getEntity(domain).getName().startsWith("test.domain.") ||
                        ResourceUtils.getEntity(route).getHost().startsWith("test-application-") ||
                        ResourceUtils.getEntity(route).getHost().startsWith("test-host-")))
                .map(function((route, domain) -> ResourceUtils.getId(route)))
                .flatMap(routeId -> cloudFoundryClient.routes()
                        .delete(DeleteRouteRequest.builder()
                                .async(true)
                                .routeId(routeId)
                                .build()))
                .map(ResourceUtils::getEntity)
                .flatMap(job -> JobUtils.waitForCompletion(cloudFoundryClient, Duration.ofMinutes(1), job));
    }

    private static Flux<Void> cleanSpaces(CloudFoundryClient cloudFoundryClient) {
        return PaginationUtils.
                requestClientV2Resources(page -> cloudFoundryClient.spaces()
                        .list(ListSpacesRequest.builder()
                                .page(page)
                                .build()))
                .filter(space -> ResourceUtils.getEntity(space).getName().startsWith("test-space-"))
                .map(ResourceUtils::getId)
                .flatMap(spaceId -> cloudFoundryClient.spaces()
                        .delete(DeleteSpaceRequest.builder()
                                .async(true)
                                .spaceId(spaceId)
                                .build()))
                .map(ResourceUtils::getEntity)
                .flatMap(job -> JobUtils.waitForCompletion(cloudFoundryClient, Duration.ofMinutes(1), job));
    }

    private static Flux<Void> cleanServiceBrokers(CloudFoundryClient cloudFoundryClient) {
        return PaginationUtils.
                requestClientV2Resources(page -> cloudFoundryClient.serviceBrokers()
                        .list(ListServiceBrokersRequest.builder()
                                .page(page)
                                .build()))
                .filter(serviceBroker -> ResourceUtils.getEntity(serviceBroker).getName().startsWith("test-broker-"))
                .map(ResourceUtils::getId)
                .flatMap(serviceBrokerId -> cloudFoundryClient.serviceBrokers()
                        .delete(DeleteServiceBrokerRequest.builder()
                                .serviceBrokerId(serviceBrokerId)
                                .build()));
    }

    private static Flux<Void> removeServiceBindings(CloudFoundryClient cloudFoundryClient, String applicationId) {
        return PaginationUtils.
                requestClientV2Resources(page -> cloudFoundryClient.applicationsV2()
                        .listServiceBindings(ListApplicationServiceBindingsRequest.builder()
                                .page(page)
                                .applicationId(applicationId)
                                .build()))
                .map(ResourceUtils::getId)
                .flatMap(serviceBindingId -> cloudFoundryClient.applicationsV2()
                        .removeServiceBinding(RemoveApplicationServiceBindingRequest.builder()
                                .applicationId(applicationId)
                                .serviceBindingId(serviceBindingId)
                                .build()));
    }

    private static Flux<Void> cleanServiceInstances(CloudFoundryClient cloudFoundryClient) {
        return PaginationUtils.
            requestClientV2Resources(page -> cloudFoundryClient.serviceInstances()
                .list(ListServiceInstancesRequest.builder()
                    .page(page)
                    .build()))
            .filter(serviceInstance -> ResourceUtils.getEntity(serviceInstance).getName()
                .startsWith("test-service-instance-"))
            .map(ResourceUtils::getId)
            .flatMap(serviceInstanceId -> cloudFoundryClient.serviceInstances()
                .delete(DeleteServiceInstanceRequest.builder()
                    .async(true)
                    .serviceInstanceId(serviceInstanceId)
                    .build()))
            .flatMap(response -> {
                Object entity = response.getEntity();
                // inspired from cf-java client org.cloudfoundry.operations.services
                //.DefaultServices#deleteServiceInstance(org.cloudfoundry.client.CloudFoundryClient, java.time.Duration, org.cloudfoundry.client.v2.serviceinstances.UnionServiceInstanceResource)
                if (entity instanceof JobEntity) {
                    return JobUtils
                        .waitForCompletion(cloudFoundryClient, Duration.ofMinutes(1), (JobEntity) response.getEntity());
                }
                else {
                    return LastOperationUtils.waitForCompletion(Duration.ofMinutes(1),
                        () -> cloudFoundryClient.serviceInstances().get(GetServiceInstanceRequest.builder()
                            .serviceInstanceId(response.getMetadata().getId())
                            .build())
                            .map(r -> ResourceUtils.getEntity(r).getLastOperation())
                    );
                }
            });
    }

    void clean() {
        Flux.empty()
                .thenMany(cleanServiceInstances(this.cloudFoundryClient))
                .thenMany(cleanServiceBrokers(this.cloudFoundryClient))
                .thenMany(cleanApplicationsV2(this.cloudFoundryClient))
                .thenMany(cleanRoutes(this.cloudFoundryClient))
                .thenMany(cleanSpaces(this.cloudFoundryClient))
                .retryWhen(Retry.max(5).filter(t -> t instanceof SSLException))
                .doOnSubscribe(s -> this.logger.debug(">> CLEANUP <<"))
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> this.logger.debug("<< CLEANUP >>"))
                .then()
                .block(Duration.ofMinutes(30));
    }

}
