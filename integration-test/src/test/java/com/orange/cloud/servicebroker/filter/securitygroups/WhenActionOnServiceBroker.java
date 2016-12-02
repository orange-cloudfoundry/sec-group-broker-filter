package com.orange.cloud.servicebroker.filter.securitygroups;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingResponse;
import org.cloudfoundry.client.v2.servicebindings.ServiceBindingEntity;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author Sebastien Bortolussi
 */
@JGivenStage
public class WhenActionOnServiceBroker extends Stage<WhenActionOnServiceBroker> {

    public static final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    public static final long TIMEOUT_MINUTES = 5L;
    @Autowired
    NameFactory nameFactory;
    @Autowired
    Mono<String> spaceId;
    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;
    @Autowired
    private CloudFoundryClient cloudFoundryClient;

    @ExpectedScenarioState
    private String serviceBrokerUsername;

    @ExpectedScenarioState
    private String serviceBrokerPassword;

    @ExpectedScenarioState
    private String serviceBrokerUrl;

    @ExpectedScenarioState
    private String serviceInstanceName;

    @ExpectedScenarioState
    private String applicationName;

    @ProvidedScenarioState
    private String serviceBrokerName;

    @ProvidedScenarioState
    private ServiceBindingEntity serviceBinding;

    @ProvidedScenarioState
    private String serviceBindingId;


    public WhenActionOnServiceBroker paas_ops_registers_app_as_a_service_broker() {
        this.serviceBrokerName = CloudFoundryServiceUtils.randomServiceBrokerName();
        logger.debug("Registering service broker {} ...", serviceBrokerName);
        CloudFoundryServiceUtils.registerServiceBroker(cloudFoundryOperations, serviceBrokerName, serviceBrokerUsername, serviceBrokerPassword, serviceBrokerUrl)
                .doOnError(t -> logger.error("Cannot register service broker {} ", serviceBrokerName, t))
                .then()
                .block(Duration.ofMinutes(TIMEOUT_MINUTES));
        logger.debug("Service broker {} has been registered.", serviceBrokerName);
        return self();
    }

    public WhenActionOnServiceBroker service_instance_is_bound_to_app() {
        logger.debug("Binding service {} to app {} ...", serviceInstanceName, applicationName);
        CreateServiceBindingResponse bindingResponse = spaceId.then(spaceId -> CloudFoundryServiceUtils.createServiceBinding(this.cloudFoundryClient, serviceInstanceName, applicationName, spaceId))
                .doOnError(t -> logger.debug("Cannot bind service {} to app {}", serviceInstanceName, applicationName, t))
                .block(Duration.ofMinutes(TIMEOUT_MINUTES));
        serviceBinding = bindingResponse.getEntity();
        serviceBindingId = bindingResponse.getMetadata().getId();
        return self();
    }

}
