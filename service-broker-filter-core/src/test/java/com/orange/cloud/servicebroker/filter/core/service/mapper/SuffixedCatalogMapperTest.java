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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;

/**
 * @author Sebastien Bortolussi
 */
public class SuffixedCatalogMapperTest {
    @Test
    public void should_get_catalog_with_prefixed_service_offering() {

        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("-suffix");

        Catalog input = catalog();

        final Catalog prefixedCatalog = mapper.toCatalog(input);

        Assert.assertEquals(expectedCatalog(), prefixedCatalog);

    }

    @Test
    public void withSuffix() throws Exception {
        Assert.assertEquals("plan-one-id-suffix", SuffixedCatalogMapper.withSuffix("plan-one-id", "-suffix"));
    }

    private Catalog expectedCatalog() {
        ArrayList plans = new ArrayList();
        HashMap metadata = new HashMap();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");
        plans.add(Plan.builder()
            .id("plan-one-id-suffix")
            .name("Plan One-suffix")
            .description("Description for Plan One")
            .build());
        plans.add(Plan.builder()
            .id("plan-two-id-suffix")
            .name("Plan Two-suffix")
            .description("Description for Plan Two")
            .metadata(metadata)
            .build());
        List services = Collections.singletonList(ServiceDefinition.builder()
            .id("service-one-id-suffix")
            .name( "Service One-suffix")
            .description("Description for Service One")
            .bindable(true)
            .plans(plans)
        .build());
        return new Catalog(services);
    }

    private Catalog catalog() {
        ArrayList plans = new ArrayList();
        HashMap metadata = new HashMap();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");
        plans.add(Plan.builder()
            .id("plan-one-id")
            .name("Plan One")
            .description("Description for Plan One")
            .build());
        plans.add(Plan.builder()
            .id("plan-two-id")
            .name("Plan Two")
            .description("Description for Plan Two")
            .metadata(metadata)
            .build());
        List services = Collections.singletonList(ServiceDefinition.builder()
            .id("service-one-id")
            .name("Service One")
            .description("Description for Service One")
            .bindable(true)
            .plans(plans)
            .build());
        return new Catalog(services);
    }

}