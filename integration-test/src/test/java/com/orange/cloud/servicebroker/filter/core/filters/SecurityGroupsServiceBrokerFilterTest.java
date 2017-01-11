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

import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastien Bortolussi
 */
public class SecurityGroupsServiceBrokerFilterTest extends AbstractServiceBrokerFilterTest {

    //service broker filter security groups related properties
    @Value("${test.apiHost}")
    private String cloudfoundryHost;
    @Value("${test.username}")
    private String cloudfoundryUser;
    @Value("${test.password}")
    private String cloudfoundryPassword;

    protected String serviceBrokerAppPath() {
        return "service-broker-filter-securitygroups-2.1.2.RELEASE.jar";
    }

    protected String getFilteredServiceBrokerOffering() {
        return String.format("mysql-mocked-broker%s", getServiceBrokerOfferingSuffix());
    }

    protected String getFilteredServiceBrokerPlan() {
        return String.format("default%s", getServiceBrokerOfferingSuffix());
    }

    private final String getServiceBrokerOfferingSuffix() {
        return "-asuffix";
    }

    protected Map<String, String> serviceBrokerAppEnvironmentVariables() {
        Map<String, String> envs = new HashMap<>();
        envs.put("BROKER_FILTER_SERVICEOFFERING_SUFFIX", getServiceBrokerOfferingSuffix());
        envs.put("BROKER_FILTER_URL", getBrokerFilterUrl());
        envs.put("BROKER_FILTER_USER", getBrokerFilterUser());
        envs.put("BROKER_FILTER_PASSWORD", getBrokerFilterPassword());
        envs.put("CLOUDFOUNDRY_HOST", cloudfoundryHost);
        envs.put("CLOUDFOUNDRY_USER", cloudfoundryUser);
        envs.put("CLOUDFOUNDRY_PASSWORD", cloudfoundryPassword);
        return envs;
    }

}
