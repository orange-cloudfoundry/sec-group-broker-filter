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

import org.springframework.cloud.servicebroker.model.Catalog;

/**
 * A very basic implementation that just map directly "target broker" catalog to "filter broker" catalog.
 * Warning: this may cause id and name conflicts when "target broker" is already registered in marketplace.
 *
 * @author Sebastien Bortolussi
 */
public class DefaultCatalogMapper implements CatalogMapper {

    @Override
    public Catalog toCatalog(Catalog catalog) {
        return catalog;
    }
}
