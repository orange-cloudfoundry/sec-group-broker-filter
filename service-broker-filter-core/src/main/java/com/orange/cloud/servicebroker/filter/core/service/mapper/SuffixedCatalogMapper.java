/*
 * <!--
 *
 *     Copyright (C) 2015 Orange
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * -->
 */

package com.orange.cloud.servicebroker.filter.core.service.mapper;


import java.util.stream.Collectors;

import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;

/**
 * Adds a suffix to service ids and names, plan ids and names to avoid conflicts
 * when "target broker" is already registered in marketplace.
 *
 * @author Sebastien Bortolussi
 */
public class SuffixedCatalogMapper implements CatalogMapper {

    private String serviceOfferingSuffix;

    public SuffixedCatalogMapper(String serviceOfferingSuffix) {
        this.serviceOfferingSuffix = serviceOfferingSuffix;
    }

    protected static String withSuffix(String s, String suffix) {
        return String.format("%s%s", s, suffix);
    }

    private ServiceDefinition toSuffixedServiceDefinition(ServiceDefinition serviceDefinition) {
        ServiceDefinition.ServiceDefinitionBuilder serviceDefinitionBuilder = ServiceDefinition.builder()
            .id(withSuffix(serviceDefinition.getId(), serviceOfferingSuffix))
            .name(withSuffix(serviceDefinition.getName(), serviceOfferingSuffix))
            .description(serviceDefinition.getDescription())
            .bindable(serviceDefinition.isBindable())
            .planUpdateable(serviceDefinition.isPlanUpdateable())
            .plans(
                serviceDefinition.getPlans()
                    .stream()
                    .map(this::toPlan)
                    .collect(Collectors.toList()));
        if (serviceDefinition.getTags() != null) {
            serviceDefinitionBuilder
                .tags(serviceDefinition.getTags());
        }
        if (serviceDefinition.getRequires() != null) {
            serviceDefinitionBuilder
                .requires(serviceDefinition.getRequires());
        }
        if (serviceDefinition.getMetadata() !=null) {
            serviceDefinitionBuilder
                .metadata(serviceDefinition.getMetadata());
        }
        return serviceDefinitionBuilder
            .dashboardClient(serviceDefinition.getDashboardClient())
            .build();
    }

    private Plan toPlan(Plan plan) {
        return Plan.builder()
            .id(withSuffix(plan.getId(), serviceOfferingSuffix))
            .name(withSuffix(plan.getName(), serviceOfferingSuffix))
            .description(plan.getDescription())
            .metadata(plan.getMetadata())
            .build();
    }

    @Override
    public Catalog toCatalog(Catalog catalog) {
        return new Catalog(catalog.getServiceDefinitions()
                .stream()
                .map(this::toSuffixedServiceDefinition)
                .collect(Collectors.toList()));
    }
}
