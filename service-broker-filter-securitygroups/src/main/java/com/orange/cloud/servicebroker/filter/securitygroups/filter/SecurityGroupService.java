package com.orange.cloud.servicebroker.filter.securitygroups.filter;

import org.cloudfoundry.client.v2.securitygroups.RuleEntity;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroupEntity;

import java.util.List;

import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;

/**
 * @author Sebastien Bortolussi
 */
public interface SecurityGroupService {

    SecurityGroupEntity create(CreateServiceInstanceBindingRequest request, List<RuleEntity> rules);

}
