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
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.cloud.servicebroker.model.fixture.CatalogFixture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Sebastien Bortolussi
 */
public class SuffixedCatalogMapperTest {
    @Test
    public void should_get_gatalog_whith_prefixed_service_offering() {

        SuffixedCatalogMapper mapper = new SuffixedCatalogMapper("-suffix");

        Catalog input = CatalogFixture.getCatalog();

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
        plans.add(new Plan("plan-one-id-suffix", "Plan One-suffix", "Description for Plan One"));
        plans.add(new Plan("plan-two-id-suffix", "Plan Two-suffix", "Description for Plan Two", metadata));
        List services = Collections.singletonList(new ServiceDefinition("service-one-id-suffix", "Service One-suffix", "Description for Service One", true, plans));
        return new Catalog(services);
    }

    private Catalog catalog() {
        ArrayList plans = new ArrayList();
        HashMap metadata = new HashMap();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");
        plans.add(new Plan("plan-one-id-suffix", "Plan One-suffix", "Description for Plan One"));
        plans.add(new Plan("plan-two-id-suffix", "Plan Two-suffix", "Description for Plan Two", metadata));
        List services = Collections.singletonList(new ServiceDefinition("service-one-id-suffix", "Service One-suffix", "Description for Service One", true, plans));
        return new Catalog(services);
    }

}