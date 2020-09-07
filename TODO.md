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
      * [ ] Adding converters against boot 2.3.x did not work
         * [ ] Suspected bean initialization order issue
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
         * Or rather spring cloud reactive configuration, rejecting MessageConverters when a reactive servlet engine is used (through `spring-boot-starter-webflux`)
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
          * This was introduced in spring boot 2.2.0 in commit https://github.com/spring-projects/spring-boot/commit/530c7bee71a4fa2da99a7c6abb2ab2b1099ae766 associated with issue https://github.com/spring-projects/spring-boot/issues/15712 HttpMessageConvertersAutoConfiguration should be conditional on not being a reactive web application
             * See related https://stackoverflow.com/questions/54823022/what-happend-to-httpmessageconverters-in-spring-boot-2 about package changes 
     * [ ] other considered alternatives
        * [ ] transiently use https://github.com/Playtika/feign-reactive
           * Requires converting osb client calls to reactive
           * Recommended into https://docs.spring.io/spring-cloud-openfeign/docs/2.2.5.RELEASE/reference/html/#reactive-support
           > As the OpenFeign project does not currently support reactive clients, such as Spring WebClient, neither does Spring Cloud OpenFeign. We will add support for it here as soon as it becomes available in the core project.
           > Until that is done, we recommend using feign-reactive https://github.com/Playtika/feign-reactive for Spring WebClient support.

        * [ ] Stay with servlet api non reactive engine and use feign blocking apis
           * https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux
           > Both web frameworks mirror the names of their source modules (spring-webmvc and spring-webflux) and co-exist side by side in the Spring Framework. Each module is optional. Applications can use one or the other module or, in some cases, both for example, Spring MVC controllers with the reactive WebClient
           * https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-programming-models
           > Both Spring MVC and WebFlux controllers support reactive (Reactor and RxJava) return types, and, as a result, it is not easy to tell them apart. One notable difference is that WebFlux also supports reactive @RequestBody arguments.
           * https://docs.spring.io/spring-boot/docs/2.3.2.RELEASE/reference/htmlsingle/#boot-features-webclient-runtime
           * [x] look for inspiration in spring-cloud-open-service-broker:spring-cloud-open-service-broker-acceptance-webmvc/build.gradle
              * does not include feign client nor security assertions
              * indeed supports non reactive stack
                  ```
                    implementation("org.springframework.boot:spring-boot-starter-web")
                    implementation(project(":spring-cloud-starter-open-service-broker"))
                  ```
               * does not support assertions about service instance and service bindings
               * [x] look for existing issues. 
                  * Not much in the open issues https://github.com/spring-cloud/spring-cloud-open-service-broker/issues?q=is%3Aissue+is%3Aclosed
                  * Closed issues indeed include support for spring-boot 2.3.2
             
        * [ ] Keep latest boot 2.3 reactive stack, but convert use of feign to another webclient
           * https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-client

        * [ ] downgrade spring boot and spring cloud to the version were workaround was documented at https://github.com/spring-cloud/spring-cloud-openfeign/issues/235#issuecomment-574999107: likely need to align other dependencies (OSB, cf-java-client)
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
         * resulting to 
            ```
            An attempt was made to call a method that does not exist. The attempt was made from the following location:
                org.cloudfoundry.reactor._DefaultConnectionContext.getConnectionProvider(_DefaultConnectionContext.java:176)
            The following method did not exist:
                'reactor.netty.resources.ConnectionProvider$Builder reactor.netty.resources.ConnectionProvider.builder(java.lang.String)'
            The method's class, reactor.netty.resources.ConnectionProvider, is available from the following locations:
                jar:file:/home/guillaume/.m2/repository/io/projectreactor/netty/reactor-netty/0.9.1.RELEASE/reactor-netty-0.9.1.RELEASE.jar!/reactor/netty/resources/ConnectionProvider.class
            It was loaded from the following location:
                file:/home/guillaume/.m2/repository/io/projectreactor/netty/reactor-netty/0.9.1.RELEASE/reactor-netty-0.9.1.RELEASE.jar
           Correct the classpath of your application so that it contains a single, compatible version of reactor.netty.resources.ConnectionProvider
            ```         

        * [ ] maven dependency analysis, to understand expected version by cf-java-client, and which version to is currently provided         
            ``` 
            [INFO] +- org.springframework.boot:spring-boot-starter-webflux:jar:2.2.1.RELEASE:compile
            [...]
            [INFO] |  +- org.springframework.boot:spring-boot-starter-reactor-netty:jar:2.2.1.RELEASE:compile
            
            [INFO] +- org.cloudfoundry:cloudfoundry-client-reactor:jar:4.9.0.RELEASE:compile
            [...]
            [INFO] |  +- io.projectreactor.netty:reactor-netty:jar:0.9.1.RELEASE:compile
            [INFO] |  |  +- io.netty:netty-codec-http:jar:4.1.43.Final:compile
            [INFO] |  |  |  +- io.netty:netty-common:jar:4.1.43.Final:compile
            [INFO] |  |  |  +- io.netty:netty-buffer:jar:4.1.43.Final:compile
            [INFO] |  |  |  +- io.netty:netty-transport:jar:4.1.43.Final:compile
            [INFO] |  |  |  |  \- io.netty:netty-resolver:jar:4.1.43.Final:compile
            [INFO] |  |  |  \- io.netty:netty-codec:jar:4.1.43.Final:compile
            [INFO] |  |  +- io.netty:netty-codec-http2:jar:4.1.43.Final:compile
            [INFO] |  |  +- io.netty:netty-handler:jar:4.1.43.Final:compile
            [INFO] |  |  +- io.netty:netty-handler-proxy:jar:4.1.43.Final:compile
            [INFO] |  |  |  \- io.netty:netty-codec-socks:jar:4.1.43.Final:compile
            [INFO] |  |  \- io.netty:netty-transport-native-epoll:jar:linux-x86_64:4.1.43.Final:compile
            [INFO] |  |     \- io.netty:netty-transport-native-unix-common:jar:4.1.43.Final:compile
            ```
            * [ ] reactor-netty 0.9.1 is from oct 2019. 0.9.11 is from aug 2020
               * https://mvnrepository.com/artifact/io.projectreactor.netty/reactor-netty         
            * Suspecting reactor-nettoy 0.9.1 to be too old, and brought by spring cloud starter webflux, while cf-java-client is compiled against a newer version
            * changes to builder code brought in 4.5.0
               * https://github.com/cloudfoundry/cf-java-client/commit/11b1d2d49bb83e9dd3f1d1a311cd317e076ee18b
               ```
                 <dependencies.version>2.2.5.RELEASE</dependencies.version>
                 [...]
                             <dependency>
                                 <groupId>org.springframework.boot</groupId>
                                 <artifactId>spring-boot-dependencies</artifactId>
                                 <version>${dependencies.version}</version>
                                 <type>pom</type>
                                 <scope>import</scope>
                             </dependency>
               ```
        * downgrade to 4.4.0 fixed the cf-java-client / reactor-netty incompatibility once a fresh cf push (preceded by a `cf d -f` ) was done. Previsouly still the feign message converters error


   * other issue at binding
            ```
            An unbind operation for the service binding between app gberche and service instance gberche failed: The service broker rejected the request. Status Code: 403 Forbidden, Body: CSRF Token has been associated to this client 
            ```
      * https://docs.spring.io/spring-cloud-open-service-broker/docs/current/reference/html5/#example-configuration is a non reactive example
      * [x] look for inspiration in spring-cloud-app-broker integration tests or sample app
         * integration tests indeed use reactive container but don't configure security and don't use feign
             ```
                implementation project(":spring-cloud-starter-app-broker-cloudfoundry")
                implementation("org.springframework.boot:spring-boot-starter-webflux")
             ```
      * [x] look for inspiration in spring-cloud-open-service-broker:spring-cloud-open-service-broker-acceptance-webflux/build.gradle
         * does not inclure security related assertiones
      * [ ] Look at spring security repo for webflux samples   
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
