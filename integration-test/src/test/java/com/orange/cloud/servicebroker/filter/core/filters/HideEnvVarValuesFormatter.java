package com.orange.cloud.servicebroker.filter.core.filters;


import com.tngtech.jgiven.format.ArgumentFormatter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sebastien Bortolussi
 */
public class HideEnvVarValuesFormatter implements ArgumentFormatter<Map<String,String>> {

    @Override
    public String format(Map<String, String> map, String... strings) {
        return map.keySet().stream().map(env -> String.format("%s=***********",env)).collect(Collectors.toList()).toString();
    }
}