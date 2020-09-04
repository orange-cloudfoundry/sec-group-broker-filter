* [ ] set up smoke test
    * [ ] TF: set up smoke test space with security group
    * [ ] set up common-broker script
        * p-mysql
* [x] handle security regression: no more auth
  * [x] search of use of the env var
  ```
    security:
      user:
        name: ${broker.filter.user}
        password: ${broker.filter.password} 
  ```        
  * [x] add spring-security starter         
  * [ ] add unit test         
    * [ ] add a profile to disable dependencies to CF at startup for this test
* [x] troubleshoot apparently not invoked secgroup filter in bind-service
    * [x] turn on actuator endpoints
    * [x] check wired beans indeed include filter
    * actually filter only applies during service binding
    * [ ] debug list of filters to display a debugging log    
        * [x] turn on actuator request logger    
        * [x] check logback is present in classpath
    * [x] check whether tomcat or netty are included, explaining webflux to not be loaded: indeed root cause
    * [ ] add unit test with wiremock to detect such issues in the future during tests
       * [ ] This adds regression https://github.com/spring-cloud/spring-cloud-openfeign/issues/235
          * [ ] Adding converters did not work
             * [ ] Suspecting bean initialization order issue
                * Feign client is declared using @FeignClient annotation on the interface
                * The FeignClientBuilder isn't picking the messageConverters     

```
  2020-09-04T16:54:45.73+0200 [APP/PROC/WEB/1] OUT Description:
   2020-09-04T16:54:45.73+0200 [APP/PROC/WEB/1] OUT Parameter 0 of method beanCatalogService in org.springframework.cloud.servicebroker.autoconfigure.web.ServiceBrokerAutoConfiguration required a bean of type 'org.springframework.boot.autoconfigure.http.HttpMessageConverters' that could not be found.
   2020-09-04T16:54:45.73+0200 [APP/PROC/WEB/1] OUT The following candidates were found but could not be injected:
   2020-09-04T16:54:45.73+0200 [APP/PROC/WEB/1] OUT 	- Bean method 'messageConverters' in 'HttpMessageConvertersAutoConfiguration' not loaded because NoneNestedConditions 1 matched 0 did not; NestedCondition on HttpMessageConvertersAutoConfiguration.NotReactiveWebApplicationCondition.ReactiveWebApplication found ConfigurableReactiveWebEnvironment
   2020-09-04T16:54:45.73+0200 [APP/PROC/WEB/1] OUT Action:
   2020-09-04T16:54:45.73+0200 [APP/PROC/WEB/1] OUT Consider revisiting the entries above or defining a bean of type 'org.springframework.boot.autoconfigure.http.HttpMessageConverters' in your configuration.

```

        * [ ] downgrade spring boot and spring cloud: likely need to align other dependencies (OSB, cf-java-client)
        
```
        <spring.cloud.version>Hoxton.SR8</spring.cloud.version>
        <spring.boot.version>2.1.9.RELEASE</spring.boot.version>


  2020-09-04T18:34:23.86+0200 [APP/PROC/WEB/1] OUT Caused by: java.lang.NoClassDefFoundError: org/springframework/boot/context/properties/ConfigurationPropertiesBean
   2020-09-04T18:34:23.86+0200 [APP/PROC/WEB/1] OUT     at org.springframework.cloud.context.properties.ConfigurationPropertiesBeans.postProcessBeforeInitialization(ConfigurationPropertiesBeans.java:94) ~
```
          * [ ] Compatibility matrix:
             * cf-java-client: 3.26.0.RELEASE ?
             * spring-cloud-open-service-broker: 3.0.x: 3.0.4.RELEASE
          * This pull the old OSB api 
```
java: constructor Catalog in class org.springframework.cloud.servicebroker.model.catalog.Catalog cannot be applied to given types;
  required: no arguments
  found: java.util.List<org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition>
  reason: actual and formal argument lists differ in length
```
         * Downgrade to a bit less  
```
        <spring.cloud.version>Hoxton.RELEASE</spring.cloud.version>
        <spring.boot.version>2.2.1.RELEASE</spring.boot.version>
```

        
        * [ ] transiently use https://github.com/Playtika/feign-reactive
           * Requires converting osb client calls to reactive
         
```

nested exception is feign.codec.DecodeException: No qualifying bean of type 'org.springframework.boot.autoconfigure.http.HttpMessageConverters' available

   2020-09-04T15:34:20.58+0200 [APP/PROC/WEB/0] OUT 2020-09-04 13:34:20.580  WARN 34 --- [           main] onfigReactiveWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'httpHandler' defined in class path resource [org/springframework/boot/autoconfigure/web/reactive/HttpHandlerAutoConfiguration$AnnotationConfig.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.http.server.reactive.HttpHandler]: Factory method 'httpHandler' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'org.springframework.cloud.servicebroker.autoconfigure.web.reactive.ServiceBrokerWebFluxAutoConfiguration': Unsatisfied dependency expressed through constructor parameter 0; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'beanCatalogService' defined in class path resource [org/springframework/cloud/servicebroker/autoconfigure/web/ServiceBrokerAutoConfiguration.class]: Unsatisfied dependency expressed through method 'beanCatalogService' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'catalog' defined in class path resource [com/orange/cloud/servicebroker/filter/core/config/CatalogConfig.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.cloud.servicebroker.model.catalog.Catalog]: Factory method 'catalog' threw exception; nested exception is feign.codec.DecodeException: No qualifying bean of type 'org.springframework.boot.autoconfigure.http.HttpMessageConverters' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}
   2020-09-04T15:34:20.61+0200 [APP/PROC/WEB/0] OUT 2020-09-04 13:34:20.610  INFO 34 --- [           main] ConditionEvaluationReportLoggingListener : 
   2020-09-04T15:34:20.61+0200 [APP/PROC/WEB/0] OUT Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT 2020-09-04 13:34:20.820 ERROR 34 --- [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT ***************************
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT APPLICATION FAILED TO START
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT ***************************
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT Description:
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT Parameter 0 of method beanCatalogService in org.springframework.cloud.servicebroker.autoconfigure.web.ServiceBrokerAutoConfiguration required a bean of type 'org.springframework.boot.autoconfigure.http.HttpMessageConverters' that could not be found.
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT The following candidates were found but could not be injected:
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT 	- Bean method 'messageConverters' in 'HttpMessageConvertersAutoConfiguration' not loaded because NoneNestedConditions 1 matched 0 did not; NestedCondition on HttpMessageConvertersAutoConfiguration.NotReactiveWebApplicationCondition.ReactiveWebApplication found ConfigurableReactiveWebEnvironment
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT Action:
   2020-09-04T15:34:20.82+0200 [APP/PROC/WEB/0] OUT Consider revisiting the entries above or defining a bean of type 'org.springframework.boot.autoconfigure.http.HttpMessageConverters' in your configuration.
 
```

```
  2020-09-04T18:23:59.50+0200 [APP/PROC/WEB/1] OUT    HttpMessageConvertersAutoConfiguration:
   2020-09-04T18:23:59.50+0200 [APP/PROC/WEB/1] OUT       Did not match:
   2020-09-04T18:23:59.50+0200 [APP/PROC/WEB/1] OUT          - NoneNestedConditions 1 matched 0 did not; NestedCondition on HttpMessageConvertersAutoConfiguration.NotReactiveWebApplicationCondition.ReactiveWebApplication found ConfigurableReactiveWebEnvironment (HttpMessageConvertersAutoConfiguration.NotReactiveWebApplicationCondition)
   2020-09-04T18:23:59.50+0200 [APP/PROC/WEB/1] OUT       Matched:
   2020-09-04T18:23:59.50+0200 [APP/PROC/WEB/1] OUT          - @ConditionalOnClass found required class 'org.springframework.http.converter.HttpMessageConverter' (OnClassCondition)

```




* [ ] investigate the following warning:

```
   2020-09-01T11:22:10.43+0200 [APP/PROC/WEB/1] OUT 2020-09-01 09:22:10.429  INFO 12 --- [-client-epoll-1] cloudfoundry-client.compatibility        : Client supports API version 2.145.0 and is connected to server with API version 2.152.0. Things may not work as expected.
   2020-09-01T11:22:10.66+0200 [APP/PROC/WEB/0] OUT 2020-09-01 09:22:10.666  INFO 6 --- [-client-epoll-1] cloudfoundry-client.compatibility        : Client supports API version 2.145.0 and is connected to server with API version 2.152.0. Things may not work as expected.
   2020-09-01T11:22:10.78+0200 [APP/PROC/WEB/1] OUT 2020-09-01 09:22:10.782  INFO 12 --- [           main] c.o.c.s.f.s.BrokerFilterApplication      : Started BrokerFilterApplication in 4.317 seconds (JVM running for 5.062)
   2020-09-01T11:22:10.89+0200 [APP/PROC/WEB/0] OUT 2020-09-01 09:22:10.892  INFO 6 --- [           main] c.o.c.s.f.s.BrokerFilterApplication      : Started BrokerFilterApplication in 4.322 seconds (JVM running for 5.09)
   2020-09-01T11:22:11.26+0200 [APP/PROC/WEB/1] OUT Exit status 0
   2020-09-01T11:22:11.26+0200 [CELL/SSHD/1] OUT Exit status 0
   2020-09-01T11:22:11.37+0200 [APP/PROC/WEB/0] OUT Exit status 0
   2020-09-01T11:22:11.37+0200 [CELL/SSHD/0] OUT Exit status 0
   2020-09-01T11:22:16.63+0200 [CELL/0] OUT Cell 52fb3406-81d7-4ef2-a68f-ea5ff7cf7f3f stopping instance 596801c5-88f8-4419-656d-f953
   2020-09-01T11:22:16.63+0200 [CELL/0] OUT Cell 52fb3406-81d7-4ef2-a68f-ea5ff7cf7f3f destroying container for instance 596801c5-88f8-4419-656d-f953
   2020-09-01T11:22:16.64+0200 [CELL/1] OUT Cell 76c68bea-605d-42e6-958e-3372371d822b stopping instance b48274e4-d2b7-4339-57a4-3956
   2020-09-01T11:22:16.64+0200 [CELL/1] OUT Cell 76c68bea-605d-42e6-958e-3372371d822b destroying container for instance b48274e4-d2b7-4339-57a4-3956
```

* [ ] release
