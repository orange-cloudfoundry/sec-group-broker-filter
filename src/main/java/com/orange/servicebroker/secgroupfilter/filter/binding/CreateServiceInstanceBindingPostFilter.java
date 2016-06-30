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

package com.orange.servicebroker.secgroupfilter.filter.binding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.orange.servicebroker.secgroupfilter.filter.Action;
import com.orange.servicebroker.secgroupfilter.filter.ZuulFilterActivationSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

@Slf4j
public class CreateServiceInstanceBindingPostFilter extends ZuulFilter {

    public static final String BINDING_REQUEST_ROUTE_TEMPLATE = "/v2/service_instances/{instance_id}/service_bindings/{binding_id}";
    public static final String BINDING_ID = "binding_id";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private ZuulFilterActivationSpecification specification;
    private Action action;

    public CreateServiceInstanceBindingPostFilter(ZuulFilterActivationSpecification specification, Action action) {
        this.specification = specification;
        this.action = action;
    }

    @Override
    public final String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return specification.isSatisfiedBy(RequestContext.getCurrentContext());
    }


    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        try {
            final CreateServiceInstanceBindingResponse createServiceInstanceBindingResponse = getBindingResponse(context);
            if (createServiceInstanceBindingResponse != null) {
                action.invoke(getBindingRequest(context), createServiceInstanceBindingResponse);
                context.setResponseBody(objectMapper.writeValueAsString(createServiceInstanceBindingResponse));
            }
        } catch (Exception e) {
            log.error("Failed to run zuul post filter. Error details {}", e);
            ReflectionUtils.rethrowRuntimeException(e);
        }
        return null;
    }

    private CreateServiceInstanceBindingRequest getBindingRequest(RequestContext context) throws java.io.IOException {
        CreateServiceInstanceBindingRequest request = objectMapper.readValue(context.getRequest().getInputStream(), CreateServiceInstanceBindingRequest.class);
        request.setBindingId(extractBindingIdFromPathParameters(context));
        return request;
    }

    private CreateServiceInstanceBindingResponse getBindingResponse(RequestContext context) throws java.io.IOException {
        if (context.getResponseBody() != null) {
            return objectMapper.readValue(context.getResponseBody(), CreateServiceInstanceBindingResponse.class);
        } else if (context.getResponseDataStream() != null) {
            return objectMapper.readValue(context.getResponseDataStream(), CreateServiceInstanceBindingResponse.class);
        }
        return null;
    }

    private String extractBindingIdFromPathParameters(RequestContext context) {
        UriTemplate uriTemplate = new UriTemplate(BINDING_REQUEST_ROUTE_TEMPLATE);
        final Map<String, String> pathParameters = uriTemplate.match(context.getRequest().getRequestURI());
        return pathParameters.get(BINDING_ID);
    }

}
