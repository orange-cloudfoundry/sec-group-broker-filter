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

package com.orange.cloud.servicebroker.filter.core.config;

import feign.Feign;
import feign.Logger;
import feign.auth.BasicAuthRequestInterceptor;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Overrides  {@link <a href="http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign">Feign</a>} Defaults
 * for filtered broker web client.
 *
 * @author Sebastien Bortolussi
 */
@Configuration
public class FilteredBrokerFeignConfig {

    @Autowired
    BrokerFilterSettings brokerFilterSettings;

    @Autowired
    com.squareup.okhttp.OkHttpClient customOkHttpClient;

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(brokerFilterSettings.getUser(), brokerFilterSettings.getPassword());
    }

    @Bean
    Logger.Level customFeignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    Logger customFeignLogger() {
        return new Slf4jLogger();
    }

    @Bean
    Feign.Builder customFeignBuilder() {
        return Feign.builder().client(new OkHttpClient(customOkHttpClient));
    }

}
