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

import com.tngtech.jgiven.integration.spring.EnableJGiven;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsRequest;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceDeveloperByUsernameRequest;
import org.cloudfoundry.client.v2.spaces.CreateSpaceRequest;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.Random;

import static org.cloudfoundry.util.OperationUtils.thenKeep;

/**
 * credits to <a href="https://github.com/cloudfoundry/cf-java-client/tree/master/integration-test">cf-java-client IT</a>
 */
@Configuration
//@EnableAutoConfiguration
@EnableJGiven
@ComponentScan
public class IntegrationTestConfiguration {

    private final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    @Bean(initMethod = "clean", destroyMethod = "clean")
    CloudFoundryCleaner cloudFoundryCleaner(CloudFoundryClient cloudFoundryClient) {
        return new CloudFoundryCleaner(cloudFoundryClient);
    }

    @Bean(initMethod = "checkCompatibility")
    ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }

    @Bean
    DefaultCloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient, String organizationName, String spaceName) {
        return DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .organization(organizationName)
                .space(spaceName)
                .build();
    }

    @Bean
    DefaultConnectionContext connectionContext(@Value("${test.apiHost}") String apiHost,
                                               @Value("${test.proxy.host:}") String proxyHost,
                                               @Value("${test.proxy.password:}") String proxyPassword,
                                               @Value("${test.proxy.port:8080}") Integer proxyPort,
                                               @Value("${test.proxy.username:}") String proxyUsername,
                                               @Value("${test.skipSslValidation:false}") Boolean skipSslValidation) {

        return DefaultConnectionContext.builder()
                .apiHost(apiHost)
                //.problemHandler(new FailingDeserializationProblemHandler())  // Test-only problem handler
                .skipSslValidation(skipSslValidation)
                .build();
    }


    @Bean
    RandomNameFactory nameFactory(Random random) {
        return new RandomNameFactory(random);
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> organizationId(CloudFoundryClient cloudFoundryClient, String organizationName) throws InterruptedException {
        return PaginationUtils
                .requestClientV2Resources(page -> cloudFoundryClient.organizations()
                        .list(ListOrganizationsRequest.builder()
                                .name(organizationName)
                                .page(page)
                                .build()))
                .map(ResourceUtils::getId)
                .single()
                .doOnSubscribe(s -> this.logger.debug(">> ORGANIZATION name({}) <<", organizationName))
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(id -> this.logger.debug("<< ORGANIZATION id({}) >>", id))
                .cache();
    }

    @Bean
    String organizationName(@Value("${test.org}") String organizationName) {
        return organizationName;
    }

    @Bean
    String spaceName(NameFactory nameFactory) {
        return nameFactory.getName("test-space-");
    }

    @Bean
    SecureRandom random() {
        return new SecureRandom();
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> spaceId(CloudFoundryClient cloudFoundryClient, Mono<String> organizationId, String spaceName, String userName) throws InterruptedException {
        return organizationId
                .then(orgId -> cloudFoundryClient.spaces()
                        .create(CreateSpaceRequest.builder()
                                .name(spaceName)
                                .organizationId(orgId)
                                .build()))
                .map(ResourceUtils::getId)
                .as(thenKeep(spaceId1 -> cloudFoundryClient.spaces()
                        .associateDeveloperByUsername(AssociateSpaceDeveloperByUsernameRequest.builder()
                                .username(userName)
                                .spaceId(spaceId1)
                                .build())))
                .doOnSubscribe(s -> this.logger.debug(">> SPACE name({}) <<", spaceName))
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(id -> this.logger.debug("<< SPACE id({}) >>", id))
                .cache();
    }

    @Bean
    PasswordGrantTokenProvider tokenProvider(@Value("${test.username}") String username,
                                             @Value("${test.password}") String password) {

        return PasswordGrantTokenProvider.builder()
                .password(password)
                .username(username)
                .build();
    }

    @Bean
    String userName(@Value("${test.username}") String username) {
        return username;
    }

}
