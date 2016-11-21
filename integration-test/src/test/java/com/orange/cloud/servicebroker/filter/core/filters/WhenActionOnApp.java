package com.orange.cloud.servicebroker.filter.core.filters;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Format;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.serviceadmin.CreateServiceBrokerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author Sebastien Bortolussi
 */
@JGivenStage
public class WhenActionOnApp extends Stage<WhenActionOnApp> {

    public static final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    public static final int TIMETOUT_MINUTES = 5;

    @Value("${test.domain}")
    public String domainTest;

    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    @ExpectedScenarioState
    private String applicationName;

    @ExpectedScenarioState
    private Path applicationBinaries;

    @ExpectedScenarioState
    private String serviceBrokerName;

    private Mono<Void> setEnvironmentVariable(CloudFoundryOperations cloudFoundryOperations, String applicationName, String variableName, String variableValue) {
        logger.debug("setting env {} with value {} for app {} ...", variableName, variableValue, applicationName);
        return cloudFoundryOperations.applications()
                .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                        .name(applicationName)
                        .variableName(variableName)
                        .variableValue(variableValue)
                        .build());
    }

    public WhenActionOnApp paas_ops_pushes_it() {
        logger.debug("pushing app {} ...", applicationName);
        cloudFoundryOperations.applications()
                .push(PushApplicationRequest.builder()
                        .application(applicationBinaries)
                        .diskQuota(512)
                        .memory(512)
                        .name(applicationName)
                        .domain(domainTest)
                        .noStart(true)
                        .build())
                .block(Duration.ofMinutes(TIMETOUT_MINUTES));
        logger.debug("app {} pushed.", applicationName);
        return self();
    }

    public WhenActionOnApp paas_ops_starts_it() {
        logger.debug("starting app {} ...", applicationName);
        cloudFoundryOperations.applications()
                .start(StartApplicationRequest.builder()
                        .name(applicationName)
                        .build())
                .block(Duration.ofMinutes(TIMETOUT_MINUTES));
        logger.debug("app {} started.", applicationName);
        return self();
    }

    public WhenActionOnApp paas_ops_sets_it_with_env_vars(@Format(value = HideEnvVarValuesFormatter.class) Map<String, String> envs) {
        logger.debug("setting app {} with env vars {}", applicationName, envs);
        Optional.ofNullable(envs)
                .map(Map::entrySet)
                .orElse(Collections.emptySet())
                .stream()
                .map(env -> setEnvironmentVariable(cloudFoundryOperations, applicationName, env.getKey(), env.getValue()))
                .reduce(Mono.empty().then(), (x, y) -> x.then(y))
                .block(Duration.ofMinutes(TIMETOUT_MINUTES));
        logger.debug("env vars set for app {}.", applicationName);
        return self();
    }

    public WhenActionOnApp broker_ops_create_sec_group_filter_service_broker(String user, String password, String brokerUrl) {
        logger.debug("creating service broker {} ...", serviceBrokerName);
        cloudFoundryOperations.serviceAdmin()
                .create(CreateServiceBrokerRequest.builder()
                        .name(serviceBrokerName)
                        .username(user)
                        .password(password)
                        .spaceScoped(true)
                        .url(brokerUrl)
                        .build()).block(Duration.ofMinutes(TIMETOUT_MINUTES));
        logger.debug("service broker {} has been created.", serviceBrokerName);
        return self();
    }

}
