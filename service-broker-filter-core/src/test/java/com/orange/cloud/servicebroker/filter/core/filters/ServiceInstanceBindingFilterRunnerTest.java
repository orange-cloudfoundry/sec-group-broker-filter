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

package com.orange.cloud.servicebroker.filter.core.filters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

/**
 * @author Sebastien Bortolussi
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceInstanceBindingFilterRunnerTest {

    @Mock
    private CreateServiceInstanceBindingPreFilter createServiceInstanceBindingPreFilter;

    @Mock
    private CreateServiceInstanceBindingPostFilter createServiceInstanceBindingPostFilter;

    @Mock
    private DeleteServiceInstanceBindingPreFilter deleteServiceInstanceBindingPreFilter;

    @Mock
    private DeleteServiceInstanceBindingPostFilter deleteServiceInstanceBindingPostFilter;

    private ServiceInstanceBindingFilterRunner filterRunner;

    @Before
    public void setup() {
        filterRunner = new ServiceInstanceBindingFilterRunner();
        filterRunner.setCreateServiceInstanceBindingPreFilters(Arrays.asList(createServiceInstanceBindingPreFilter));
        filterRunner.setCreateServiceInstanceBindingPostFilters(Arrays.asList(createServiceInstanceBindingPostFilter));
        filterRunner.setDeleteServiceInstanceBindingPreFilters(Arrays.asList(deleteServiceInstanceBindingPreFilter));
        filterRunner.setDeleteServiceInstanceBindingPostFilters(Arrays.asList(deleteServiceInstanceBindingPostFilter));
    }

    @Test
    public void should_only_run_create_service_instance_binding_pre_filter_on_pre_binding() throws Exception {
        filterRunner.preBind(Mockito.any());

        Mockito.verifyZeroInteractions(createServiceInstanceBindingPostFilter, deleteServiceInstanceBindingPreFilter, deleteServiceInstanceBindingPostFilter);
        Mockito.verify(createServiceInstanceBindingPreFilter).run(Mockito.any());
    }

    @Test
    public void should_only_run_create_service_instance_binding_post_filter_on_post_binding() throws Exception {
        filterRunner.postBind(Mockito.any(), Mockito.any());

        Mockito.verifyZeroInteractions(createServiceInstanceBindingPreFilter, deleteServiceInstanceBindingPreFilter, deleteServiceInstanceBindingPostFilter);
        Mockito.verify(createServiceInstanceBindingPostFilter).run(Mockito.any(), Mockito.any());

    }

    @Test
    public void should_only_run_delete_service_instance_binding_pre_filter_on_pre_unbinding() throws Exception {
        filterRunner.preUnbind(Mockito.any());

        Mockito.verifyZeroInteractions(createServiceInstanceBindingPreFilter, createServiceInstanceBindingPostFilter, deleteServiceInstanceBindingPostFilter);
        Mockito.verify(deleteServiceInstanceBindingPreFilter).run(Mockito.any());
    }

    @Test
    public void should_only_run_delete_service_instance_binding_post_filter_on_post_unbinding() throws Exception {
        filterRunner.postUnbind(Mockito.any(), Mockito.any());

        Mockito.verifyZeroInteractions(createServiceInstanceBindingPreFilter, createServiceInstanceBindingPostFilter, deleteServiceInstanceBindingPreFilter);
        Mockito.verify(deleteServiceInstanceBindingPostFilter).run(Mockito.any(), Mockito.any());
    }

}