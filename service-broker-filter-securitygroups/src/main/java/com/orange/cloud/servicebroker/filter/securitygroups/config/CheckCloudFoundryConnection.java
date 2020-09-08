package com.orange.cloud.servicebroker.filter.securitygroups.config;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.info.GetInfoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * This code forces a connection to CloudFoundry with provided credentials on app startup.
 * When bad credentials exception, app fails to start.
 *
 */
@Component
@Profile("!offline-test-without-cf")
public class CheckCloudFoundryConnection implements CommandLineRunner {

    @Autowired
    CloudFoundryClient cloudFoundryClient;

    @Override
    public void run(String... strings) throws Exception {
        //basic get on info endpoint so as to perform a CloudFoundry CC API connection and thus assert credentials are valid
        cloudFoundryClient.info().get(GetInfoRequest.builder().build()).block(Duration.ofMinutes(3));
    }
}
