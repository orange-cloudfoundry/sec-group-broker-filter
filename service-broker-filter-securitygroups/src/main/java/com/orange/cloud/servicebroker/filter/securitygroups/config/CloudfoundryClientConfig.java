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

package com.orange.cloud.servicebroker.filter.securitygroups.config;

import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.ProxyConfiguration;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * @author Sebastien Bortolussi
 */
@Configuration
@Profile("!offline-test-without-cf")
public class CloudfoundryClientConfig {

    @Autowired
    CloudFoundryClientSettings cloudFoundryClientSettings;

    @Bean
    DefaultConnectionContext connectionContext(CloudFoundryClientSettings cloudFoundryClientSettings) {

        DefaultConnectionContext.Builder connectionContext = DefaultConnectionContext.builder()
                .apiHost(cloudFoundryClientSettings.getHost())
                .sslHandshakeTimeout(Duration.ofSeconds(30))
                .skipSslValidation(cloudFoundryClientSettings.getSkipSslValidation());

        if (StringUtils.hasText(cloudFoundryClientSettings.getProxyHost())) {
            ProxyConfiguration.Builder proxyConfiguration = ProxyConfiguration.builder()
                    .host(cloudFoundryClientSettings.getProxyHost())
                    .port(cloudFoundryClientSettings.getProxyPort());

            if (StringUtils.hasText(cloudFoundryClientSettings.getProxyUsername())) {
                proxyConfiguration
                        .password(cloudFoundryClientSettings.getProxyPassword())
                        .username(cloudFoundryClientSettings.getProxyUsername());
            }

            connectionContext.proxyConfiguration(proxyConfiguration.build());
        }
        return connectionContext.build();
    }

    @Bean
    PasswordGrantTokenProvider tokenProvider(CloudFoundryClientSettings cloudFoundryClientSettings) {
        return PasswordGrantTokenProvider.builder()
                .password(cloudFoundryClientSettings.getPassword())
                .username(cloudFoundryClientSettings.getUser())
                .build();
    }

    @Bean
    ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }


}
