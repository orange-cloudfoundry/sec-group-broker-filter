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

import com.orange.cloud.servicebroker.filter.core.IntegrationTestConfiguration;
import com.orange.cloud.servicebroker.filter.core.NameFactory;
import org.cloudfoundry.util.test.TestSubscriber;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

/**
 * credits to <a href="https://github.com/cloudfoundry/cf-java-client/tree/master/integration-test">cf-java-client IT</a>
 */
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {IntegrationTestConfiguration.class},properties = {"logging.level.com.orange.cloud=debug"})
public class AbstractIntegrationTest {

    public static final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    @Rule
    public final TestName testName = new TestName();

    private final TestSubscriber<?> testSubscriber = new TestSubscriber<>()
            .setPerformanceLoggerName(this::getTestName);

    @Autowired
    private NameFactory nameFactory;

    @Before
    public void testEntry() {
        this.logger.debug(">> {} <<", getTestName());
    }

    @After
    public final void verify() throws InterruptedException {
        this.testSubscriber.verify(Duration.ofMinutes(5));
        this.logger.debug("<< {} >>", getTestName());
    }


    protected final String getApplicationName() {
        return this.nameFactory.getName("test-application-");
    }

    protected final String getServiceInstanceName() {
        return this.nameFactory.getName("test-service-instance-");
    }

    protected final String getBrokerName() {
        return this.nameFactory.getName("test-broker-");
    }

    private String getTestName() {
        return String.format("%s.%s", this.getClass().getSimpleName(), this.testName.getMethodName());
    }

    protected final <T> TestSubscriber<T> testSubscriber() {
        return (TestSubscriber<T>) this.testSubscriber;
    }
}
