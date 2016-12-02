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

import com.orange.cloud.servicebroker.filter.securitygroups.tags.ApplicationSecurityGroup;
import org.junit.Test;

/**
 * @author Sebastien Bortolussi
 * @
 */
public class ServiceBindingTest extends AbstractIntegrationTest {

    @Test
    @ApplicationSecurityGroup
    public void should_create_application_security_group() throws Exception {
        given().an_app()
                .and().a_resource_$_not_reachable_from_app("https://127.0.0.1:8080/service")
                .and().a_service_instance_created_from_sec_group_filter_broker_service_offering();
        when().service_instance_is_bound_to_app();
        then().app_should_have_access_to_resource("https://127.0.0.1:8080/service");
    }

}
