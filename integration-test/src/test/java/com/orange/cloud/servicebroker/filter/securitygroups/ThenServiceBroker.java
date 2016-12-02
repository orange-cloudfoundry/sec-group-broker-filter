package com.orange.cloud.servicebroker.filter.securitygroups;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.NestedSteps;
import com.tngtech.jgiven.annotation.Pending;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.assertj.core.api.Assertions;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.securitygroups.ListSecurityGroupsRequest;
import org.cloudfoundry.client.v2.securitygroups.RuleEntity;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroupEntity;
import org.cloudfoundry.client.v2.servicebindings.ServiceBindingEntity;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.serviceadmin.ServiceBroker;
import org.cloudfoundry.operations.services.ListServiceOfferingsRequest;
import org.cloudfoundry.util.ExceptionUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


/**
 * @author Sebastien Bortolussi
 */
@JGivenStage
public class ThenServiceBroker extends Stage<ThenServiceBroker> {

    public static final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    public static final long TIMEOUT_MINUTES = 5L;
    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    @Autowired
    private CloudFoundryClient cloudFoundryClient;

    @ExpectedScenarioState
    private String serviceBrokerName;

    @ExpectedScenarioState
    private ServiceBindingEntity serviceBinding;

    @ExpectedScenarioState
    private String serviceBindingId;

    @NestedSteps
    public ThenServiceBroker service_broker_should_be_registered() {
        return service_broker_should_be_created()
                .and()
                .service_offering_$_should_exist_in_the_marketplace(new ServiceOffering("test-service", Arrays.asList("default")));
    }

    public ThenServiceBroker service_broker_should_be_created() {
        Assert.assertNotNull("Cannot find service broker. No name has been specified.", serviceBrokerName);
        final String actual = this.cloudFoundryOperations.serviceAdmin()
                .list()
                .map(ServiceBroker::getName)
                .filter(serviceBrokerName::equals)
                .blockFirst(Duration.ofMinutes(5));
        Assert.assertNotNull("No service broker of name {} found.", actual);
        return self();
    }

    public ThenServiceBroker service_offering_$_should_exist_in_the_marketplace(ServiceOffering expected) {
        this.cloudFoundryOperations.services()
                .listServiceOfferings(ListServiceOfferingsRequest.builder()
                        .serviceName(expected.getLabel())
                        .build())
                .map(s -> new ServiceOffering(s.getLabel(), s.getServicePlans().stream().map(p -> p.getName()).collect(Collectors.toList())))
                .single()
                .otherwise(NoSuchElementException.class, t -> ExceptionUtils.illegalArgument("service %s with plans %s does not exist", expected.getLabel(), expected.getServicePlans()))
                .block(Duration.ofMinutes(5));
        return self();
    }

    @Pending
    public ThenServiceBroker security_group_should_restrict_access_from_space(String space) {
        return self();
    }

    public ThenServiceBroker a_security_group_that_opens_access_to_ip_$_port_$_should_be_created(String ip, String port) {
        SecurityGroupEntity securityGroup = PaginationUtils
                .requestClientV2Resources(page -> {
                    return cloudFoundryClient.securityGroups()
                            .list(ListSecurityGroupsRequest.builder()
                                    .name(serviceBindingId)
                                    .page(page)
                                    .build());
                })
                .single()
                .map(ResourceUtils::getEntity)
                .otherwise(NoSuchElementException.class, t -> ExceptionUtils.illegalArgument("security group %s does not exist", serviceBindingId))
                .doOnError(t -> this.logger.debug("Cannot find security group {}", serviceBindingId, t))
                .block(Duration.ofSeconds(TIMEOUT_MINUTES));

        Assertions.assertThat(securityGroup.getRules()).hasSize(1)
                .contains(RuleEntity.builder()
                        .destination(ip)
                        .protocol("tcp")
                        .ports(port)
                        .build());
        return self();
    }

    public ThenServiceBroker app_should_be_injected_with_credentials_key_$_value(String key, String value) {
        Assertions.assertThat(serviceBinding.getCredentials())
                .contains(Assertions.entry(key, value));
        return self();
    }

    @NestedSteps
    public ThenServiceBroker app_should_have_access_to_resource(String resourceURL) throws MalformedURLException {
        URL url = new URL(resourceURL);
        return app_should_be_injected_with_credentials_key_$_value("uri", resourceURL)
                .and()
                .a_security_group_that_opens_access_to_ip_$_port_$_should_be_created(url.getHost(), String.valueOf(url.getPort()));
    }
}
