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


import org.springframework.cloud.servicebroker.model.catalog.Catalog;

/**
 * This interface is implemented by mappers to avoid id and name conflicts between "target broker" catalog and "filter broker" catalog.
 *
 * @author Sebastien Bortolussi
 */
public interface CatalogMapper {
    /**
     * @param catalog the target broker catalog
     * @return the filter broker catalog
     */
    Catalog toCatalog(Catalog catalog);
}
