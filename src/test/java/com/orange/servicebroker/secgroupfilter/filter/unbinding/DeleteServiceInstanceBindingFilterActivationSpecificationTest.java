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
import com.orange.servicebroker.secgroupfilter.filter.binding.CreateServiceInstanceBindingFilterActivationSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeleteServiceInstanceBindingFilterActivationSpecificationTest {

    @Before
    public void setTestRequestcontext() {
        RequestContext context = new RequestContext();
        RequestContext.testSetCurrentContext(context);
    }

    @After
    public void reset() {
        RequestContext.getCurrentContext().clear();
    }


    @Test
    public void validUnbindingRequest() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest("DELETE", "/v2/service_instances/instance_id/service_bindings/binding_id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        RequestContext context = new RequestContext();
        context.setRequest(request);
        context.setResponse(response);

        DeleteServiceInstanceBindingFilterActivationSpecification specification = new DeleteServiceInstanceBindingFilterActivationSpecification();

        assertTrue(specification.isSatisfiedBy(context));
    }

    @Test
    public void should_not_filter_if_not_a_DELETE_method() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest("PUT", "/v2/service_instances/instance_id/service_bindings/binding_id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        RequestContext context = new RequestContext();
        context.setRequest(request);
        context.setResponse(response);

        DeleteServiceInstanceBindingFilterActivationSpecification specification = new DeleteServiceInstanceBindingFilterActivationSpecification();

        assertFalse(specification.isSatisfiedBy(context));
    }

    @org.junit.Test
    public void should_not_filter_if_not_an_Unbinding_Route() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest("DELETE", "/v2/service_instances");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        RequestContext context = new RequestContext();
        context.setRequest(request);
        context.setResponse(response);

        DeleteServiceInstanceBindingFilterActivationSpecification specification = new DeleteServiceInstanceBindingFilterActivationSpecification();

        assertFalse(specification.isSatisfiedBy(context));
    }

    @org.junit.Test
    public void should_not_filter_if_response_NOT_OK() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest("PUT", "/v2/service_instances/instance_id/service_bindings/binding_id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        RequestContext context = new RequestContext();
        context.setRequest(request);
        context.setResponse(response);
        context.setResponseBody("{value=\"dummy\"}");

        CreateServiceInstanceBindingFilterActivationSpecification specification = new CreateServiceInstanceBindingFilterActivationSpecification();

        assertFalse(specification.isSatisfiedBy(context));
    }

    @Test
    public void should_not_filter_if_response_body_is_empty() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest("PUT", "/v2/service_instances/instance_id/service_bindings/binding_id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_CREATED);

        RequestContext context = new RequestContext();
        context.setRequest(request);
        context.setResponse(response);
        context.setResponseBody(null);

        CreateServiceInstanceBindingFilterActivationSpecification specification = new CreateServiceInstanceBindingFilterActivationSpecification();

        assertFalse(specification.isSatisfiedBy(context));
    }

}