package com.orange.cloud.servicebroker.filter.securitygroups;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.Description;
import com.tngtech.jgiven.annotation.NestedSteps;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.assertj.core.api.Assertions;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastien Bortolussi
 */

@JGivenStage
public class GivenServiceBroker extends Stage<GivenServiceBroker> {

    public static final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    public static final long TIMEOUT_MINUTES = 10L;
    @Value("${test.domain}")
    public String domainTest;

    @Autowired
    NameFactory nameFactory;

    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    @Value("${broker.filter.binaries}")
    private String serviceBrokerBinaries;

    @ProvidedScenarioState
    private String serviceBrokerName;

    @ProvidedScenarioState
    @Value("${broker.filter.user}")
    private String serviceBrokerUsername;

    @ProvidedScenarioState
    @Value("${broker.filter.password}")
    private String serviceBrokerPassword;

    @Value("${broker.filter.url}")
    private String brokerFilterUrl;

    @ProvidedScenarioState
    private String serviceBrokerUrl;

    //service broker filter security groups related properties
    @Value("${test.apiHost}")
    private String cloudfoundryHost;
    @Value("${test.username}")
    private String cloudfoundryUser;
    @Value("${test.password}")
    private String cloudfoundryPassword;
    @ProvidedScenarioState
    private String serviceInstanceName;
    @ProvidedScenarioState
    private String applicationName;

    protected Map<String, String> serviceBrokerAppEnvironmentVariables() {
        Map<String, String> envs = new HashMap<>();
        envs.put("BROKER_FILTER_URL", brokerFilterUrl);
        envs.put("BROKER_FILTER_USER", serviceBrokerUsername);
        envs.put("BROKER_FILTER_PASSWORD", serviceBrokerPassword);
        envs.put("CLOUDFOUNDRY_HOST", cloudfoundryHost);
        envs.put("CLOUDFOUNDRY_USER", cloudfoundryUser);
        envs.put("CLOUDFOUNDRY_PASSWORD", cloudfoundryPassword);
        envs.put("JBP_CONFIG_CONTAINER_CERTIFICATE_TRUST_STORE", "{enabled: true}");
        return envs;
    }


    public GivenServiceBroker app_is_registered_as_a_service_broker() throws Exception {
        this.serviceBrokerName = CloudFoundryServiceUtils.randomServiceBrokerName();

        logger.debug("Registering service broker {} ...", serviceBrokerName);
        CloudFoundryServiceUtils.registerServiceBroker(cloudFoundryOperations, serviceBrokerName, serviceBrokerUsername, serviceBrokerPassword, serviceBrokerUrl)
                .doOnError(t -> logger.error("Cannot register service broker {} ", serviceBrokerName, t))
                .then()
                .block(Duration.ofMinutes(TIMEOUT_MINUTES));

        return self();
    }

    @NestedSteps
    public GivenServiceBroker a_service_instance_created_from_sec_group_filter_broker_service_offering() throws Exception {
        return a_registered_sec_group_filter_service_broker()
                .and().a_service_instance_created_from_marketplace();
    }

    public GivenServiceBroker a_service_instance_created_from_marketplace() {
        this.serviceInstanceName = CloudFoundryServiceUtils.randomServiceInstanceName();
        logger.debug("Creating service instance {} for service {} and plan {} ...", serviceInstanceName, "test-service", "default");
        CloudFoundryServiceUtils.createServiceInstance(cloudFoundryOperations, "test-service", "default", serviceInstanceName)
                .doOnError(t -> logger.debug("Cannot create service instance {} for service {} and plan {}", serviceInstanceName, "test-service", "default", t))
                .then()
                .block(Duration.ofMinutes(TIMEOUT_MINUTES));
        return self();
    }

    public GivenServiceBroker an_app() throws Exception {
        this.applicationName = CloudFoundryServiceUtils.randomAppName();
        logger.debug("Creating app  ...", applicationName);
        cloudFoundryOperations.applications()
                .push(PushApplicationRequest.builder()
                        .memory(64)
                        .application(new ClassPathResource("test-application.zip").getFile().toPath())
                        .name(applicationName)
                        .buildpack("staticfile_buildpack")
                        .build())
                .doOnError(t -> logger.debug("Cannot create app {}", applicationName, t))
                .block(Duration.ofMinutes(TIMEOUT_MINUTES));
        return self();
    }


    public GivenServiceBroker a_sec_group_filter_service_broker_app() throws Exception {

        final String serviceBrokerAppName = CloudFoundryServiceUtils.randomAppName();
        final Path applicationPath = new ClassPathResource(serviceBrokerBinaries).getFile().toPath();
        final Map<String, String> envs = serviceBrokerAppEnvironmentVariables();

        serviceBrokerUrl = CloudFoundryServiceUtils.deployServiceBrokerApp(cloudFoundryOperations, serviceBrokerAppName, applicationPath, domainTest, envs)
                .map(ApplicationDetail::getUrls)
                .block(Duration.ofMinutes(TIMEOUT_MINUTES))
                .stream()
                .findFirst()
                .map(CloudFoundryServiceUtils::getAppUrl)
                .orElseThrow(() -> new IllegalStateException(String.format("Unable to retrieve url from app %s", serviceBrokerAppName)));

        logger.debug("Asserting service broker app  catalog endpoint is available is reachable...");
        TestRestTemplate testRestTemplate = new TestRestTemplate(serviceBrokerUsername, serviceBrokerPassword, TestRestTemplate.HttpClientOption.SSL);
        final String catalogEndpointUrl = String.format("%s/v2/catalog", serviceBrokerUrl);
        Assertions.assertThat(testRestTemplate.getForEntity(catalogEndpointUrl, String.class).getStatusCode())
                .as("Assert service broker catalog endpoint is available")
                .isEqualTo(HttpStatus.OK);
        return self();
    }

    @NestedSteps
    @Description("a registered sec group filter service broker that returns binding credentials of the form {\"uri\": \"https://127.0.0.1:8080/service\"}")
    public GivenServiceBroker a_registered_sec_group_filter_service_broker() throws Exception {
        return a_sec_group_filter_service_broker_app()
                .and().app_is_registered_as_a_service_broker();
    }

    public GivenServiceBroker a_resource_$_not_reachable_from_app(String resourceURL) {
        return self();
    }
}