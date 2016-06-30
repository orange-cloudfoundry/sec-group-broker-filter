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

package com.orange.servicebroker.secgroupfilter;

import com.orange.servicebroker.secgroupfilter.filter.binding.CreateSecurityGroup;
import com.orange.servicebroker.secgroupfilter.filter.binding.CreateServiceInstanceBindingFilterActivationSpecification;
import com.orange.servicebroker.secgroupfilter.filter.binding.CreateServiceInstanceBindingPostFilter;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationsV2;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroups;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableZuulProxy
@EnableWebSecurity(debug = true)
public class BrokerFilterApplication {

    public static void main(String[] args) {
        if ("true".equals(System.getenv("SKIP_SSL_VALIDATION"))) {
            SSLValidationDisabler.disableSSLValidation();
        }
        SpringApplication.run(BrokerFilterApplication.class, args);
    }

    @Bean
    CloudFoundryClient cloudFoundryClient(@Value("${cloudfoundry.api.url}") String host,
                                          @Value("${cloudfoundry.credentials.user}") String username,
                                          @Value("${cloudfoundry.credentials.password}") String password) {
        return SpringCloudFoundryClient.builder()
                .host(host)
                .username(username)
                .password(password)
                .skipSslValidation(true)
                .build();
    }

    @Bean
    SecurityGroups securityGroups(CloudFoundryClient cloudFoundryClient) {
        return cloudFoundryClient.securityGroups();
    }

    @Bean
    ApplicationsV2 applicationsV2(CloudFoundryClient cloudFoundryClient) {
        return cloudFoundryClient.applicationsV2();
    }

    @Bean
    CreateServiceInstanceBindingPostFilter createServiceInstanceBindingPostFilter(SecurityGroups securityGroups, ApplicationsV2 applicationsV2) {
        return new CreateServiceInstanceBindingPostFilter(new CreateServiceInstanceBindingFilterActivationSpecification(), new CreateSecurityGroup(securityGroups, applicationsV2));
    }
}
