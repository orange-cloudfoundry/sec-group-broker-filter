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

import com.netflix.zuul.context.RequestContext;
import com.orange.servicebroker.secgroupfilter.filter.ZuulFilterActivationSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * credits to https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-core/src/test/java/org/springframework/cloud/netflix/zuul/filters/post/SendResponseFilterTests.java
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateServiceInstanceBindingPostFilterTest {

    public static final String BINDING_RESPONSE = "{\"credentials\":{\"name\":\"ad_c6f4446532610ab\",\"hostname\":\"us-cdbr-east-03.cleardb.com\",\"port\":\"3306\",\"username\":\"b5d435f40dd2b2\",\"password\":\"ebfc00ac\",\"uri\":\"mysql://b5d435f40dd2b2:ebfc00ac@us-cdbr-east-03.cleardb.com:3306/ad_c6f4446532610ab\",\"jdbcUrl\":\"jdbc:mysql://b5d435f40dd2b2:ebfc00ac@us-cdbr-east-03.cleardb.com:3306/ad_c6f4446532610ab\"}}";
    public static final String BINDING_REQUEST = "{\"plan_id\":\"plan-guid-here\",\"service_id\":\"service-guid-here\",\"app_guid\":\"app-guid-here\",\"bind_resource\":{\"app_guid\":\"app-guid-here\"}}";


    @Mock
    ZuulFilterActivationSpecification specification;

    @Mock
    CreateSecurityGroup createSecurityGroupDelegate;

    @Before
    public void setTestRequestcontext() {
        RequestContext context = new RequestContext();
        RequestContext.testSetCurrentContext(context);
        Mockito.when(specification.isSatisfiedBy(Mockito.any())).thenReturn(true);
    }

    @After
    public void reset() {
        RequestContext.getCurrentContext().clear();
    }

    @Test
    public void should_propagate_received_response() throws Exception {
        MockHttpServletResponse response;
        response = new MockHttpServletResponse();
        CreateServiceInstanceBindingPostFilter filter = createFilter(BINDING_RESPONSE, response, true);
        filter.run();
        assertThat("wrong content", RequestContext.getCurrentContext().getResponseBody(), equalTo(BINDING_RESPONSE));
    }

    private CreateServiceInstanceBindingPostFilter createFilter(String content, MockHttpServletResponse response, boolean streamContent) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/v2/service_instances/instance_id/service_bindings/binding_id");
        request.setContent(BINDING_REQUEST.getBytes());

        RequestContext context = new RequestContext();
        context.setRequest(request);
        context.setResponse(response);

        if (streamContent) {
            context.setResponseDataStream(new ByteArrayInputStream(content.getBytes()));
        } else {
            context.setResponseBody(content);
        }

        context.addZuulResponseHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length()));

        RequestContext.testSetCurrentContext(context);

        return new CreateServiceInstanceBindingPostFilter(specification, createSecurityGroupDelegate);
    }

}