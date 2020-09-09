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

package com.orange.cloud.servicebroker.filter.core.config;

import com.orange.cloud.servicebroker.filter.core.service.CatalogServiceClient;
import com.orange.cloud.servicebroker.filter.core.service.OsbConstants;
import com.orange.cloud.servicebroker.filter.core.service.mapper.CatalogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configure the catalog of services offered by the service broker.
 *
 * @author Sebastien Bortolussi
 */
@Profile("!offline-test-without-cf")
@Configuration
public class CatalogConfig {

    @Autowired
    CatalogServiceClient client;

    @Autowired
    CatalogMapper catalogMapper;

    @Bean
    public Catalog catalog(CatalogMapper catalogMapper) {
        return catalogMapper.toCatalog(client.getCatalog(OsbConstants.X_Broker_API_Version_Value));
    }

}
