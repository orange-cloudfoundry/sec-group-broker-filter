package com.orange.cloud.servicebroker.filter.securitygroups.tags;

import com.tngtech.jgiven.annotation.IsTag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IsTag(name = "Marketplace registration",
        description = "In order to expose sec group filter service broker offering from the Cloud Foundry service marketplace,<br>" +
                "As a PaaS ops,<br>" +
                "I want to register a sec group filter service broker.")
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceBrokerRegistration {

}
