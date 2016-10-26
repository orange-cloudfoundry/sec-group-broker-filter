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

package com.orange.cloud.servicebroker.filter.core.filters;

import com.orange.cloud.servicebroker.filter.core.RandomNameFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.*;
import org.cloudfoundry.operations.serviceadmin.CreateServiceBrokerRequest;
import org.cloudfoundry.operations.serviceadmin.ServiceBroker;
import org.cloudfoundry.operations.services.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * credits to <a href="https://github.com/cloudfoundry/cf-java-client/tree/master/integration-test">cf-java-client IT</a>
 */
public abstract class AbstractServiceBrokerFilterTest extends AbstractIntegrationTest {

    //service broker filter related properties
    @Value("${test.domain}")
    public String domainTest;
    @Autowired
    Mono<String> organizationId;
    @Autowired
    String userName;
    @Autowired
    RandomNameFactory nameFactory;
    @Autowired
    Mono<String> spaceId;
    @Value("${broker.filter.url}")
    private String brokerFilterUrl;
    @Value("${broker.filter.user}")
    private String brokerFilterUser;
    @Value("${broker.filter.password}")
    private String brokerFilterPassword;
    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    private static Mono<Void> pushApp(CloudFoundryOperations cloudFoundryOperations, Path applicationPath, String applicationName, String domain, Boolean noStart) {
        logger.debug("pushing app {} ...", applicationName);
        return cloudFoundryOperations.applications()
                .push(PushApplicationRequest.builder()
                        .application(applicationPath)
                        .diskQuota(512)
                        .memory(512)
                        .name(applicationName)
                        .domain(domain)
                        .noStart(noStart)
                        .build());
    }

    private static Mono<Void> startApplication(CloudFoundryOperations cloudFoundryOperations, String applicationName) {
        logger.debug("starting app {} ...", applicationName);
        return cloudFoundryOperations.applications()
                .start(StartApplicationRequest.builder()
                        .name(applicationName)
                        .build());
    }

    private static Mono<Void> setEnvironmentVariable(CloudFoundryOperations cloudFoundryOperations, String applicationName, String variableName, String variableValue) {
        logger.debug("setting env {} with value {} for app {} ...", variableName, variableValue, applicationName);
        return cloudFoundryOperations.applications()
                .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                        .name(applicationName)
                        .variableName(variableName)
                        .variableValue(variableValue)
                        .build());
    }

    private static Mono<Void> createPrivateServiceBroker(CloudFoundryOperations cloudFoundryOperations, String brokerName, String userName, String password, String url) {
        logger.debug("creating service broker {} ...", brokerName);
        return cloudFoundryOperations.serviceAdmin()
                .create(CreateServiceBrokerRequest.builder()
                        .name(brokerName)
                        .username(userName)
                        .password(password)
                        .spaceScoped(true)
                        .url(url)
                        .build());
    }

    private static Mono<Void> createServiceInstance(CloudFoundryOperations cloudFoundryOperations, String serviceName, String planName, String serviceInstanceName) {
        logger.debug("creating service instance {} ...", serviceInstanceName);
        return cloudFoundryOperations.services()
                .createInstance(CreateServiceInstanceRequest.builder()
                        .serviceName(serviceName)
                        .planName(planName)
                        .serviceInstanceName(serviceInstanceName)
                        .build());
    }

    protected static String buildHttpsUrl(String host, String domain) {
        return String.format("https://%s.%s", host, domain);
    }

    private static Mono<Void> setEnvs(CloudFoundryOperations cloudFoundryOperations, String applicationName, Map<String, String> envs) {
        return Optional.ofNullable(envs)
                .map(Map::entrySet)
                .orElse(Collections.emptySet())
                .stream()
                .map(env -> setEnvironmentVariable(cloudFoundryOperations, applicationName, env.getKey(), env.getValue()))
                .reduce(Mono.empty().then(), (x, y) -> x.then(y));

    }

    public String getBrokerFilterUrl() {
        return brokerFilterUrl;
    }

    public String getBrokerFilterUser() {
        return brokerFilterUser;
    }

    public String getBrokerFilterPassword() {
        return brokerFilterPassword;
    }

    private Path getApplicationPath() {
        try {
            return Paths.get(new ClassPathResource(serviceBrokerAppPath()).getURI());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected abstract String serviceBrokerAppPath();

    protected abstract String getFilteredServiceBrokerOffering();

    protected abstract String getFilteredServiceBrokerPlan();

    protected abstract Map<String, String> serviceBrokerAppEnvironmentVariables();

    @Test
    public void push_service_broker_app() {
        final String applicationName = getApplicationName();
        final Map<String, String> environmentVariables = serviceBrokerAppEnvironmentVariables();
        givenSpace()
                .then(pushThenStartServiceBrokerApp(this.cloudFoundryOperations, applicationName, getApplicationPath(), domainTest, environmentVariables))
                .then(this.cloudFoundryOperations.applications()
                        .get(GetApplicationRequest.builder()
                                .name(applicationName)
                                .build()))
                .map(ApplicationDetail::getName)
                .subscribe(testSubscriber()
                        .expectEquals(applicationName));
    }

    private Mono<String> givenSpace() {
        return spaceId;
    }

    @Test
    public void create_service_broker() {

        final String applicationName = getApplicationName();
        final String brokerName = getBrokerName();
        final String applicationUrl = getAppUrl(applicationName, domainTest);
        final Map<String, String> environmentVariables = serviceBrokerAppEnvironmentVariables();

        givenSpace()
                .then(pushThenStartServiceBrokerApp(this.cloudFoundryOperations, applicationName, getApplicationPath(), domainTest, environmentVariables))
                .then(createPrivateServiceBroker(this.cloudFoundryOperations, brokerName, brokerFilterUser, brokerFilterPassword, applicationUrl))
                .thenMany(this.cloudFoundryOperations.serviceAdmin()
                        .list())
                .filter(hasServiceBroker(brokerName))
                .subscribe(testSubscriber()
                        .expectCount(1));
    }

    @Test
    public void service_offering() {

        final String applicationName = getApplicationName();
        final String brokerName = getBrokerName();
        final String applicationUrl = getAppUrl(applicationName, domainTest);
        final Map<String, String> environmentVariables = serviceBrokerAppEnvironmentVariables();


        givenSpace()
                .then(pushThenStartServiceBrokerApp(this.cloudFoundryOperations, applicationName, getApplicationPath(), domainTest, environmentVariables))
                .then(createPrivateServiceBroker(this.cloudFoundryOperations, brokerName, brokerFilterUser, brokerFilterPassword, applicationUrl))
                .thenMany(this.cloudFoundryOperations.services()
                        .listServiceOfferings(ListServiceOfferingsRequest.builder()
                                .serviceName(getFilteredServiceBrokerOffering())
                                .build()))
                .filter(hasService(getFilteredServiceBrokerOffering()))
                .subscribe(testSubscriber()
                        .expectCount(1));
    }

    @Test
    public void create_service_instance() {

        final String applicationName = getApplicationName();
        final String brokerName = getBrokerName();
        final String applicationUrl = getAppUrl(applicationName, domainTest);
        final Map<String, String> environmentVariables = serviceBrokerAppEnvironmentVariables();
        String serviceInstanceName = getServiceInstanceName();
        final String serviceName = getFilteredServiceBrokerOffering();
        final String planName = getFilteredServiceBrokerPlan();

        givenSpace()
                .then(pushThenStartServiceBrokerApp(this.cloudFoundryOperations, applicationName, getApplicationPath(), domainTest, environmentVariables))
                .then(createPrivateServiceBroker(this.cloudFoundryOperations, brokerName, brokerFilterUser, brokerFilterPassword, applicationUrl))
                .then(createServiceInstance(this.cloudFoundryOperations, serviceName, planName, serviceInstanceName))
                .then(this.cloudFoundryOperations.services()
                        .getInstance(GetServiceInstanceRequest.builder()
                                .name(serviceInstanceName)
                                .build()))
                .map(ServiceInstance::getName)
                .subscribe(testSubscriber()
                        .expectEquals(serviceInstanceName));
    }

    private Predicate<? super ServiceOffering> hasService(String serviceName) {
        return serviceOffering -> serviceName.equals(serviceOffering.getLabel());
    }

    private Predicate<ServiceBroker> hasServiceBroker(String brokerName) {
        return serviceBroker -> brokerName.equals(serviceBroker.getName());
    }

    private Mono<Void> pushThenStartServiceBrokerApp(CloudFoundryOperations cloudFoundryOperations, String applicationName, Path applicationPath, String domain, Map<String, String> envs) {
        return pushApp(cloudFoundryOperations, applicationPath, applicationName, domain, true)
                .then(setEnvs(cloudFoundryOperations, applicationName, envs))
                .then(startApplication(cloudFoundryOperations, applicationName));
    }

    protected final String getAppUrl(String host, String domain) {
        return buildHttpsUrl(host, domain);
    }
}
