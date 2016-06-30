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

package com.orange.servicebroker.secgroupfilter.filter;

import java.util.*;

/**
 * credits to https://github.com/spring-cloud/spring-cloud-connectors
 */
public class CloudFoundryCredentialsUtils {

    public static String[] uriSchemes = {"jdbc", "oracle", "mysql", "postgres"};

    public static String getUriFromCredentials(Map<String, Object> credentials) {
        List<String> keys = new ArrayList<String>();
        keys.addAll(Arrays.asList("uri", "url"));

        for (String uriScheme : uriSchemes) {
            keys.add(uriScheme + "Uri");
            keys.add(uriScheme + "uri");
            keys.add(uriScheme + "Url");
            keys.add(uriScheme + "url");
        }

        return getStringFromCredentials(credentials, keys.toArray(new String[keys.size()]))
                .orElseThrow(() -> new IllegalStateException(String.format("Cannot get [%s] from credentials", keys)));
    }

    protected static Optional<String> getStringFromCredentials(Map<String, Object> credentials, String... keys) {
        if (credentials != null) {
            for (String key : keys) {
                if (credentials.containsKey(key)) {
                    return Optional.of((String) credentials.get(key));
                }
            }
        }
        return Optional.empty();
    }


}
