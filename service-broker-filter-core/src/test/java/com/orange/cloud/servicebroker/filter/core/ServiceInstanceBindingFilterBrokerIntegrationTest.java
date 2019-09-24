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

import com.orange.cloud.servicebroker.filter.core.config.CatalogConfig;
import com.orange.cloud.servicebroker.filter.core.service.OsbConstants;
import com.orange.cloud.servicebroker.filter.core.service.ServiceInstanceBindingServiceClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.fixture.CatalogFixture;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.BDDMockito.given;

/**
 * @author Sebastien Bortolussi
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"broker.filter.url=http://localhost", "security.user.name=user", "security.user.password=pass", "logging.level.ROOT=debug"})
@Ignore
public class ServiceInstanceBindingFilterBrokerIntegrationTest {

    @MockBean
    ServiceInstanceBindingServiceClient serviceInstanceBindingServiceClient;

    private TestRestTemplate restTemplate = new TestRestTemplate("user", "pass");

    @Test
    public void should_proxy_create_service_instance_binding_request_to_filtered_broker() throws Exception {
        CreateServiceInstanceBindingRequest request = new CreateServiceInstanceBindingRequest()
                .withServiceInstanceId("instance_id")
                .withBindingId("binding_id");
        ResponseEntity<CreateServiceInstanceAppBindingResponse> response = new ResponseEntity<CreateServiceInstanceAppBindingResponse>(new CreateServiceInstanceAppBindingResponse(), HttpStatus.CREATED);

        given(this.serviceInstanceBindingServiceClient.createServiceInstanceBinding("instance_id", "binding_id", OsbConstants.X_Broker_API_Version_Value, request))
                .willReturn(response);

        final ResponseEntity<CreateServiceInstanceAppBindingResponse> forEntity = this.restTemplate.getForEntity("/v2/service_instances/{instance_id}/service_bindings/{binding_id}",
                CreateServiceInstanceAppBindingResponse.class, "instance_id", "binding_id");

        Assert.assertEquals(response.getBody(), forEntity);

    }

}


@SpringBootApplication(exclude = {CatalogConfig.class})
class BrokerFilterApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrokerFilterApplication.class, args);
    }

    @Bean
    public Catalog catalog() {
        return CatalogFixture.getCatalog();
    }


}
