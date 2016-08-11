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

import com.orange.cloud.servicebroker.filter.core.service.mapper.CatalogMapper;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;

import java.util.stream.Collectors;

/**
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
        return new ServiceDefinition(withSuffix(serviceDefinition.getId(), serviceOfferingSuffix),
                withSuffix(serviceDefinition.getName(), serviceOfferingSuffix),
                serviceDefinition.getDescription(),
                serviceDefinition.isBindable(),
                serviceDefinition.isPlanUpdateable(),
                serviceDefinition.getPlans()
                        .stream()
                        .map(this::toPlan)
                        .collect(Collectors.toList()),
                serviceDefinition.getTags(),
                serviceDefinition.getMetadata(),
                serviceDefinition.getRequires(),
                serviceDefinition.getDashboardClient());
    }

    private Plan toPlan(Plan plan) {
        return new Plan(withSuffix(plan.getId(), serviceOfferingSuffix),
                withSuffix(plan.getName(), serviceOfferingSuffix),
                plan.getDescription(),
                plan.getMetadata());
    }

    @Override
    public Catalog toCatalog(Catalog catalog) {
        return new Catalog(catalog.getServiceDefinitions()
                .stream()
                .map(this::toSuffixedServiceDefinition)
                .collect(Collectors.toList()));
    }
}
