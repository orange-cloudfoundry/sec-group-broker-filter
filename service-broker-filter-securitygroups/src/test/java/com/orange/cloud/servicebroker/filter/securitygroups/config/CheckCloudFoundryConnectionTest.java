package com.orange.cloud.servicebroker.filter.securitygroups.config;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.info.GetInfoRequest;
import org.cloudfoundry.client.v2.info.Info;
import org.cloudfoundry.uaa.UaaException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.Matchers.isA;

public class CheckCloudFoundryConnectionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ConfigurableApplicationContext context;

    @After
    public void cleanUp() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void fail_to_start_app_when_cloudFoundry_cc_API_bad_credentials() throws Exception {
        //Should fail when Bad Credentials
        this.thrown.expectCause(isA(UaaException.class));
        this.context = new SpringApplicationBuilder(TestConfiguration.class).web(false).run();
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public CloudFoundryClient cloudFoundryClient() {
            final Info info = Mockito.mock(Info.class);
            //fail to connect to CloudFoundry CC API with bad credentials exception
            Mockito.when(info.get(GetInfoRequest.builder().build())).thenThrow(new UaaException(401, "unauthorized", "Bad credentials"));
            final CloudFoundryClient cloudFoundryClient = Mockito.mock(CloudFoundryClient.class);
            Mockito.when(cloudFoundryClient.info()).thenReturn(info);
            return cloudFoundryClient;
        }

        @Bean
        CheckCloudFoundryConnection checkCloudFoundryConnection() {
            return new CheckCloudFoundryConnection();
        }

    }

}