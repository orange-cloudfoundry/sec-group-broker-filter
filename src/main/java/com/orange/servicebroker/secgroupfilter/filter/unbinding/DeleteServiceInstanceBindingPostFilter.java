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

package com.orange.servicebroker.secgroupfilter.filter.unbinding;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.orange.servicebroker.secgroupfilter.filter.Action;
import com.orange.servicebroker.secgroupfilter.filter.ZuulFilterActivationSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

@Slf4j
public class DeleteServiceInstanceBindingPostFilter extends ZuulFilter {

    public static final String BINDING_REQUEST_ROUTE_TEMPLATE = "/v2/service_instances/{instance_id}/service_bindings/{binding_id}";
    public static final String BINDING_ID = "binding_id";

    private ZuulFilterActivationSpecification specification;
    private Action action;

    public DeleteServiceInstanceBindingPostFilter(ZuulFilterActivationSpecification specification, Action action) {
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
            action.invoke(getUnbindingRequest(context), null);
        } catch (Exception e) {
            log.error("Failed to run zuul post filter. Error details {}", e);
            ReflectionUtils.rethrowRuntimeException(e);
        }
        return null;
    }

    private DeleteServiceInstanceBindingRequest getUnbindingRequest(RequestContext context) throws java.io.IOException {
        return DeleteServiceInstanceBindingRequest.builder().bindingId(extractBindingIdFromPathParameters(context)).build();
    }

    private String extractBindingIdFromPathParameters(RequestContext context) {
        UriTemplate uriTemplate = new UriTemplate(BINDING_REQUEST_ROUTE_TEMPLATE);
        final Map<String, String> pathParameters = uriTemplate.match(context.getRequest().getRequestURI());
        return pathParameters.get(BINDING_ID);
    }

}
