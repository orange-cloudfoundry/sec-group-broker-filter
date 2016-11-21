package com.orange.cloud.servicebroker.filter.core.filters;

import com.orange.cloud.servicebroker.filter.core.NameFactory;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Sebastien Bortolussi
 */

@JGivenStage
public class GivenServiceBroker extends Stage<GivenServiceBroker> {

    @Autowired
    NameFactory nameFactory;

    @ProvidedScenarioState
    private String brokerUrl;

    @ProvidedScenarioState
    private String applicationName;

    @ProvidedScenarioState
    private Path applicationBinaries;

    @ProvidedScenarioState
    private String serviceBrokerName;

    public GivenServiceBroker existing_service_broker_app_available_at_$(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        return self();
    }

    public GivenServiceBroker a_broker_user() {
        return self();
    }

    public GivenServiceBroker an_app_with_sec_group_filter_broker_binaries(String applicationBinaries) {
        this.applicationName =  nameFactory.getName("test-application-");
        this.applicationBinaries = getApplicationPath(applicationBinaries);
        return self();
    }

    public GivenServiceBroker a_random_broker_name() {
        this.serviceBrokerName =  nameFactory.getName("test-service-broker");
        return self();
    }

    private Path getApplicationPath(String applicationBinaries) {
        try {
            return Paths.get(new ClassPathResource(applicationBinaries).getURI());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}