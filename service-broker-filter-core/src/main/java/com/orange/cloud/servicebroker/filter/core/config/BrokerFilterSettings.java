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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * Filtered broker web client connection settings
 *
 * @author Sebastien Bortolussi
 */
@Component
@Data
@ConfigurationProperties(BrokerFilterSettings.PREFIX)
public class BrokerFilterSettings {

    public static final String PREFIX = "broker.filter";
    private URL url;
    private String user;
    private String password;

    public BrokerFilterSettings() {
    }

    public BrokerFilterSettings(URL url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }
}


