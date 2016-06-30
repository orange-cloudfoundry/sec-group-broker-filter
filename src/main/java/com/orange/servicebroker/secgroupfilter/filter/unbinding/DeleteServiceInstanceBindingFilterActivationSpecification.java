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

import com.netflix.zuul.context.RequestContext;
import com.orange.servicebroker.secgroupfilter.filter.ZuulFilterActivationSpecification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriTemplate;

public class DeleteServiceInstanceBindingFilterActivationSpecification implements ZuulFilterActivationSpecification<RequestContext> {

    public static final String DELETE_BINDING_REQUEST_ROUTE_TEMPLATE = "/v2/service_instances/{instance_id}/service_bindings/{binding_id}";

    public static final HttpMethod DELETE_BINDING_REQUEST_HTTP_METHOD = HttpMethod.DELETE;

    private UriTemplate uriTemplate = new UriTemplate(DELETE_BINDING_REQUEST_ROUTE_TEMPLATE);

    /**
     * @param context the zuul http request context
     * @return true if request is a unbinding request
     */
    @Override
    public boolean isSatisfiedBy(RequestContext context) {
        assert context != null;
        return isAnUnbindingRequest(context) && responseOK(context);
    }

    private boolean isAnUnbindingRequest(RequestContext context) {
        return isDELETE(context) && uriTemplate.matches(context.getRequest().getRequestURI());
    }

    private boolean isDELETE(RequestContext context) {
        return DELETE_BINDING_REQUEST_HTTP_METHOD.matches(context.getRequest().getMethod());
    }

    private boolean responseOK(RequestContext context) {
        return HttpStatus.OK.value() == context.getResponse().getStatus();
    }

}
