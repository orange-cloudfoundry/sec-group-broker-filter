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

package com.orange.cloud.servicebroker.filter.securitygroups;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationResource;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingResponse;
import org.cloudfoundry.client.v2.serviceinstances.UnionServiceInstanceResource;
import org.cloudfoundry.client.v2.spaces.ListSpaceApplicationsRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceServiceInstancesRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.serviceadmin.CreateServiceBrokerRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.util.ExceptionUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

/**
 * credits to <a href="https://github.com/cloudfoundry/cf-java-client/tree/master/integration-test">cf-java-client IT</a>
 */

public class CloudFoundryServiceUtils {

    private static Mono<Void> setEnvironmentVariable(CloudFoundryOperations cloudFoundryOperations, String applicationName, String variableName, String variableValue) {
        return cloudFoundryOperations.applications()
                .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                        .name(applicationName)
                        .variableName(variableName)
                        .variableValue(variableValue)
                        .build());
    }

    public static Mono<Void> registerServiceBroker(CloudFoundryOperations cloudFoundryOperations, String brokerName, String userName, String password, String url) {
        return cloudFoundryOperations.serviceAdmin()
                .create(CreateServiceBrokerRequest.builder()
                        .name(brokerName)
                        .username(userName)
                        .password(password)
                        .spaceScoped(true)
                        .url(url)
                        .build());
    }

    public static Mono<Void> createServiceInstance(CloudFoundryOperations cloudFoundryOperations, String serviceName, String planName, String serviceInstanceName) {
        return cloudFoundryOperations.services()
                .createInstance(CreateServiceInstanceRequest.builder()
                        .serviceName(serviceName)
                        .planName(planName)
                        .serviceInstanceName(serviceInstanceName)
                        .build());
    }

    public static Mono<ApplicationDetail> deployServiceBrokerApp(CloudFoundryOperations cloudFoundryOperations, String applicationName, Path applicationPath, String domain, Map<String, String> envs) {
        return cloudFoundryOperations.applications()
                .push(PushApplicationRequest.builder()
                        .application(applicationPath)
                        .diskQuota(512)
                        .memory(512)
                        .name(applicationName)
                        .domain(domain)
                        .buildpack("java_buildpack")
                        .noStart(true)
                        .build())
                .then(Optional.ofNullable(envs)
                        .map(Map::entrySet)
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(env -> setEnvironmentVariable(cloudFoundryOperations, applicationName, env.getKey(), env.getValue()))
                        .reduce(Mono.empty().then(), (x, y) -> x.then(y)))
                .then(cloudFoundryOperations.applications()
                        .start(StartApplicationRequest.builder()
                                .name(applicationName)
                                .build()))
                .then(cloudFoundryOperations.applications()
                        .get(org.cloudfoundry.operations.applications.GetApplicationRequest.builder()
                                .name(applicationName)
                                .build()));
    }

    public static Mono<CreateServiceBindingResponse> createServiceBinding(CloudFoundryClient cloudFoundryClient, String serviceInstanceName, String applicationName, String spaceId) {
        return Mono.when(
                getApplicationId(cloudFoundryClient, applicationName, spaceId),
                getSpaceServiceInstanceId(cloudFoundryClient, serviceInstanceName, spaceId)
        )
                .then(function((applicationId, serviceInstanceId) -> requestCreateServiceBinding(cloudFoundryClient, applicationId, serviceInstanceId)));
    }

    private static Mono<CreateServiceBindingResponse> requestCreateServiceBinding(CloudFoundryClient cloudFoundryClient, String applicationId, String serviceInstanceId) {
        return cloudFoundryClient.serviceBindingsV2()
                .create(CreateServiceBindingRequest.builder()
                        .applicationId(applicationId)
                        .serviceInstanceId(serviceInstanceId)
                        .build());
    }

    private static Flux<UnionServiceInstanceResource> requestListServiceInstances(CloudFoundryClient cloudFoundryClient, String spaceId, String serviceInstanceName) {
        return PaginationUtils
                .requestClientV2Resources(page -> cloudFoundryClient.spaces()
                        .listServiceInstances(ListSpaceServiceInstancesRequest.builder()
                                .page(page)
                                .returnUserProvidedServiceInstances(true)
                                .name(serviceInstanceName)
                                .spaceId(spaceId)
                                .build()));
    }

    private static Mono<UnionServiceInstanceResource> getSpaceServiceInstance(CloudFoundryClient cloudFoundryClient, String serviceInstanceName, String spaceId) {
        return requestListServiceInstances(cloudFoundryClient, spaceId, serviceInstanceName)
                .single()
                .otherwise(NoSuchElementException.class, t -> ExceptionUtils.illegalArgument("Service instance %s does not exist", serviceInstanceName));
    }

    public static Mono<String> getSpaceServiceInstanceId(CloudFoundryClient cloudFoundryClient, String serviceInstanceName, String spaceId) {
        return getSpaceServiceInstance(cloudFoundryClient, serviceInstanceName, spaceId)
                .map(ResourceUtils::getId);
    }

    private static Mono<ApplicationResource> getApplication(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        return requestListApplications(cloudFoundryClient, applicationName, spaceId)
                .single()
                .otherwise(NoSuchElementException.class, t -> ExceptionUtils.illegalArgument("Application %s does not exist", applicationName));
    }

    public static Mono<String> getApplicationId(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        return getApplication(cloudFoundryClient, applicationName, spaceId)
                .map(ResourceUtils::getId);
    }

    private static Flux<ApplicationResource> requestListApplications(CloudFoundryClient cloudFoundryClient, String application, String spaceId) {
        return PaginationUtils
                .requestClientV2Resources(page -> cloudFoundryClient.spaces()
                        .listApplications(ListSpaceApplicationsRequest.builder()
                                .name(application)
                                .spaceId(spaceId)
                                .page(page)
                                .build()));
    }

    public static String randomServiceBrokerName() {
        return String.format("%s%s", "test-broker-", new BigInteger(25, new SecureRandom()).toString(32));
    }

    public static String randomAppName() {
        return String.format("%s%s", "test-application-", new BigInteger(25, new SecureRandom()).toString(32));
    }

    protected static String getAppUrl(String url) {
        return String.format("https://%s", url);
    }

    public static String randomServiceInstanceName() {
        return String.format("%s%s", "test-service-instance-", new BigInteger(25, new SecureRandom()).toString(32));
    }
}
