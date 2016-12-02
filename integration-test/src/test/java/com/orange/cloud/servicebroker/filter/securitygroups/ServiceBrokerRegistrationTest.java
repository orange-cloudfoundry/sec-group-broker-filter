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

import com.orange.cloud.servicebroker.filter.securitygroups.tags.ServiceBrokerRegistration;
import org.junit.Test;

/**
 * @author Sebastien Bortolussi
 */
public class ServiceBrokerRegistrationTest extends AbstractIntegrationTest {

    @Test
    @ServiceBrokerRegistration
    public void should_register_service_broker() throws Exception {
        given().a_sec_group_filter_service_broker_app();
        when().paas_ops_registers_app_as_a_service_broker();
        then().service_broker_should_be_registered();
    }


}
