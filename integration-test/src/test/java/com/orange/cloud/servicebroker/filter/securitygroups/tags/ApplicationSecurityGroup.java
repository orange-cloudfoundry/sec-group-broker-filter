package com.orange.cloud.servicebroker.filter.securitygroups.tags;

import com.tngtech.jgiven.annotation.IsTag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IsTag(name = "Application Security Group",
        description = "In order to restrict access for any app to external services,<br>" +
                "As a PaaS ops,<br>" +
                "I want to create fine grained security group per service binding.")
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationSecurityGroup {

}
