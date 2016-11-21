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

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.Description;
import com.tngtech.jgiven.annotation.ScenarioStage;
import com.tngtech.jgiven.integration.spring.SpringScenarioExecutor;
import com.tngtech.jgiven.junit.ScenarioExecutionRule;
import com.tngtech.jgiven.junit.ScenarioReportRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastien Bortolussi
 */
public class PushAndStartSecurityGroupsServiceBrokerFilterAppTest extends AbstractServiceBrokerFilterTest implements BeanFactoryAware {

    @ClassRule
    public static final ScenarioReportRule writerRule = new ScenarioReportRule();
    public static final String SERVICE_BROKER_FILTER_SECURITY_GROUP_JAR = "service-broker-filter-securitygroups-2.2.0.BUILD-SNAPSHOT.jar";
    @Rule
    public final ScenarioExecutionRule scenarioRule = new ScenarioExecutionRule();
    @ScenarioStage
    GivenServiceBroker someState;
    @ScenarioStage
    WhenActionOnApp whenActionOnApp;
    @ScenarioStage
    ThenApp thenApp;

    @ScenarioStage
    ThenServiceBroker thenServiceBroker;

    //service broker filter security groups related properties
    @Value("${test.apiHost}")
    private String cloudfoundryHost;
    @Value("${test.username}")
    private String cloudfoundryUser;
    @Value("${test.password}")
    private String cloudfoundryPassword;
    @Value("${broker.filter.url}")
    private String brokerFilterUrl;
    @Value("${broker.filter.user}")
    private String brokerFilterUser;
    @Value("${broker.filter.password}")
    private String brokerFilterPassword;

    public void setBeanFactory(BeanFactory beanFactory) {
        scenarioRule.getScenario().setExecutor(beanFactory.getBean(SpringScenarioExecutor.class));
    }

    @Test
    @Description("As a PaaS ops,<br>"
            + " I want to push and start a sec group filter broker app<br>"
            + " In order to be able to register it as a service broker in the cloudfoundry service marketplace afterwards.")
    public void should_start_sec_group_filter_broker_app() {
        given().an_app_with_sec_group_filter_broker_binaries(SERVICE_BROKER_FILTER_SECURITY_GROUP_JAR);
        when().paas_ops_pushes_it()
                .and().paas_ops_sets_it_with_env_vars(serviceBrokerAppEnvironmentVariables())
                .and().paas_ops_starts_it();
        then().app_should_have_started();
    }

    @Test
    @As("As a PaaS ops,<br>" +
            "I want to create a private sec group filter service broker,<br>" +
            "In order to use it from the cloudfoundry service marketplace.")
    public void should_create_sec_group_filter_broker() {
        final String applicationName = getApplicationName();
        final String applicationUrl = getAppUrl(applicationName, domainTest);

        given().an_app_with_sec_group_filter_broker_binaries(SERVICE_BROKER_FILTER_SECURITY_GROUP_JAR);
        when().paas_ops_pushes_it()
                .and().paas_ops_sets_it_with_env_vars(serviceBrokerAppEnvironmentVariables())
                .and().paas_ops_starts_it();
        then().app_should_have_started();
        //
        given().a_random_broker_name().
                and().existing_service_broker_app_available_at_$(applicationUrl);
        when().broker_ops_create_sec_group_filter_service_broker(brokerFilterUser, brokerFilterPassword, applicationUrl);
        thenServiceBroker.then().service_broker_should_be_registered();
    }

    private ThenApp then() {
        return thenApp.then();
    }

    private WhenActionOnApp when() {
        return whenActionOnApp.when();
    }

    private GivenServiceBroker given() {
        return someState.given();
    }


    protected String serviceBrokerAppPath() {
        return SERVICE_BROKER_FILTER_SECURITY_GROUP_JAR;
    }

    @Override
    protected String getFilteredServiceBrokerOffering() {
        return null;
    }

    @Override
    protected String getFilteredServiceBrokerPlan() {
        return null;
    }

    protected Map<String, String> serviceBrokerAppEnvironmentVariables() {
        Map<String, String> envs = new HashMap<>();
        envs.put("BROKER_FILTER_URL", getBrokerFilterUrl());
        envs.put("BROKER_FILTER_USER", getBrokerFilterUser());
        envs.put("BROKER_FILTER_PASSWORD", getBrokerFilterPassword());
        envs.put("CLOUDFOUNDRY_HOST", cloudfoundryHost);
        envs.put("CLOUDFOUNDRY_USER", cloudfoundryUser);
        envs.put("CLOUDFOUNDRY_PASSWORD", cloudfoundryPassword);
        return envs;
    }


}
