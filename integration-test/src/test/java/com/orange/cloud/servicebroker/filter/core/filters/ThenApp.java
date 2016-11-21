package com.orange.cloud.servicebroker.filter.core.filters;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @author Sebastien Bortolussi
 */
@JGivenStage
public class ThenApp extends Stage<ThenApp> {

    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    @ExpectedScenarioState
    private String applicationName;


    public ThenApp app_should_have_started() {
        final Integer actual = this.cloudFoundryOperations.applications()
                .get(GetApplicationRequest.builder()
                        .name(applicationName)
                        .build())
                .map(ApplicationDetail::getRunningInstances)
                .block(Duration.ofMinutes(3));
        Assert.assertTrue("No app instance started", actual > 0);
        return self();
    }

    public ThenApp should_get_catalog() {


        return self();
    }


}
