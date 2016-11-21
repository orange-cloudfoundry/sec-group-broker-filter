package com.orange.cloud.servicebroker.filter.core.filters;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.serviceadmin.ServiceBroker;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * @author Sebastien Bortolussi
 */
@JGivenStage
public class ThenServiceBroker extends Stage<ThenServiceBroker> {

    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    @ExpectedScenarioState
    private String serviceBrokerName;

    public ThenServiceBroker service_broker_should_be_registered() {
        Assert.assertNotNull("Cannot find service broker. No name has been specified.", serviceBrokerName);
        this.cloudFoundryOperations.serviceAdmin()
                .list()
                .filter(hasServiceBroker(serviceBrokerName))
                .blockFirst(Duration.ofMinutes(3));
        Assert.assertNotNull("No service broker of name {} found.", serviceBrokerName);
        return self();
    }

    private Predicate<ServiceBroker> hasServiceBroker(String serviceBrokerName) {
        return serviceBroker -> serviceBrokerName.equals(serviceBroker.getName());
    }

}
