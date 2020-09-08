This file holds implementation notes from previous features. It aims at being easier to find than commit message, while not yet reaching full design docs

# Dependency and java version bumps 

Bump dependencies

* [x] reactor dependency updates
   * compilation fails
       ```
       service-broker-filter-securitygroups/src/main/java/com/orange/cloud/servicebroker/filter/securitygroups/filter/CreateSecurityGroup.java:96:17
       java: incompatible types: reactor.core.publisher.Mono is not a functional interface
       ```
   https://github.com/spring-cloud/spring-cloud-vault/issues/254 Updating to Java 11 causes vault to to not find reactor.netty.http.client.HttpClient
   suspecting related mismatch with old reactor mismatch with java14
   * Try to bump to at least same versioin as osb-cmdb
   * reactor 3 uses a bom https://projectreactor.io/docs/core/release/reference/#getting-started-understanding-bom
   * strangely sec-group-broker filter looks up 3.0.4/3.1.0 whereas osb-cmdb uses 3.3.4
      * [x] look maven dependency:tree graph
        spring-cloud-starter-open-service-broker
            spring-boot-dependencies-2.2.8.RELEASE
                <reactor-bom.version>Dysprosium-SR8</reactor-bom.version>
                    reactor-core 3.3.6.RELEASE
      * [x] look maven dependency:analyze-dep-mgt fails to fetch parent project
         * [x] check ide version 3.6.3 (latest)
         * [x] try mvn:install: fails for compilation (chicken and egg)
      * [x] removed dependency mgt on reactor got an upgrade to reactor-core:jar:3.3.8.RELEASE
   * Root cause is API change in reactor:
      * https://projectreactor.io/docs/core/3.0.7.RELEASE/api/reactor/core/publisher/Mono.html#otherwise-java.lang.Class-java.util.function.Function-
          > reactor.core.publisher.Mono.otherwise(Class<E>, Function<? super E, ? extends Mono<? extends T>>)
          > Use Mono.onErrorResume(Class, Function) instead. Will be removed in 3.1.0.
      * https://projectreactor.io/docs/core/3.0.7.RELEASE/api/reactor/core/publisher/Mono.html#flatMap-java.util.function.Function-
          > public final <R> Flux<R> flatMap(Function<? super T,? extends Publisher<? extends R>> mapper)
          > Deprecated. will change signature and behavior in 3.1 to reflect current then(Function). flatMap will be renamed flatMapMany(Function), so use that instead.
   * Was on reactor 3.0.5
   * See deprecation notice at https://spring.io/blog/2017/05/16/reactor-bismuth-release-train-first-milestone-available

   > 1. to first replace all usage of Mono.flatMap with flatMapMany
   > 2. finish other refactor and migrations
   > 3. switch to 3.1.0.M1
   > 4. replace all usage of Mono.then(Function) (now not compiling) to flatMap

    * [x] to first replace all usage of Mono.flatMap with flatMapMany
    * [x] finish other refactor and migrations: search for deprecated calls
    * [x] switch to 3.1.0.M1
    * [x] replace all usage of Mono.then(Function) (now not compiling) to flatMap
      * Pb with functions from cloudfoundry-utils 2.4.0 https://github.com/cloudfoundry/cf-java-client/releases/tag/v2.4.0.RELEASE
         * Not much documented into next releases at https://github.com/cloudfoundry/cf-java-client/releases?after=v2.15.0.RELEASE
         * Actually comes from the use of a deprecated code pattern in cf-java-client with Mono.when() and direct use of Tuples.of() which is now replaced with use of Mono.zip()

Next pattern from cf-java-client DefaultServices:

```
  @Override
    public Mono<Void> updateUserProvidedInstance(UpdateUserProvidedServiceInstanceRequest request) {
        return Mono
            .zip(this.cloudFoundryClient, this.spaceId)
            .flatMap(function((cloudFoundryClient, spaceId) -> Mono.zip(
                Mono.just(cloudFoundryClient),
                getSpaceUserProvidedServiceInstanceId(cloudFoundryClient, request.getUserProvidedServiceInstanceName(), spaceId)
            )))
            .flatMap(function((cloudFoundryClient, userProvidedServiceInstanceId) -> updateUserProvidedServiceInstance(cloudFoundryClient, request, userProvidedServiceInstanceId)))
            .then()
            .transform(OperationsLogging.log("Update User Provided Service Instance"))
            .checkpoint();
    }
```

* [ ] Fix maven copy

```
Failed to execute goal org.apache.maven.plugins:maven-dependency-plugin:3.1.2:copy (copy) on project integration-test: Artifact has not been packaged yet. When used on reactor artifact, copy should be executed after packaging: see MDEP-187. 
```

```
[INFO] --- maven-dependency-plugin:3.1.2:tree (default-cli) @ service-broker-filter-core ---
[INFO] com.orange.cloud.servicebroker:service-broker-filter-core:jar:2.4.0.BUILD-SNAPSHOT
[INFO] +- org.springframework.cloud:spring-cloud-starter-open-service-broker:jar:3.1.2.RELEASE:compile
[INFO] |  \- org.springframework.cloud:spring-cloud-open-service-broker-autoconfigure:jar:3.1.2.RELEASE:compile
[INFO] |     \- org.springframework.cloud:spring-cloud-open-service-broker-core:jar:3.1.2.RELEASE:compile
[INFO] |        +- commons-beanutils:commons-beanutils:jar:1.9.4:compile
[INFO] |        |  \- commons-collections:commons-collections:jar:3.2.2:compile
[INFO] |        +- io.projectreactor:reactor-core:jar:3.1.0.RELEASE:compile
[INFO] |        |  \- org.reactivestreams:reactive-streams:jar:1.0.3:compile
[INFO] |        +- org.hibernate.validator:hibernate-validator:jar:6.1.5.Final:compile
[INFO] |        |  +- jakarta.validation:jakarta.validation-api:jar:2.0.2:compile
[INFO] |        |  +- org.jboss.logging:jboss-logging:jar:3.4.1.Final:compile
[INFO] |        |  \- com.fasterxml:classmate:jar:1.5.1:compile
[INFO] |        \- org.springframework:spring-context:jar:5.2.8.RELEASE:compile
[INFO] |           \- org.springframework:spring-expression:jar:5.2.8.RELEASE:compile
[INFO] +- org.springframework.cloud:spring-cloud-starter-openfeign:jar:2.2.4.RELEASE:compile
[INFO] |  +- org.springframework.cloud:spring-cloud-starter:jar:2.2.4.RELEASE:compile
[INFO] |  |  +- org.springframework.cloud:spring-cloud-context:jar:2.2.4.RELEASE:compile
[INFO] |  |  \- org.springframework.security:spring-security-rsa:jar:1.0.9.RELEASE:compile
[INFO] |  |     \- org.bouncycastle:bcpkix-jdk15on:jar:1.64:compile
[INFO] |  |        \- org.bouncycastle:bcprov-jdk15on:jar:1.64:compile
[INFO] |  +- org.springframework.cloud:spring-cloud-openfeign-core:jar:2.2.4.RELEASE:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-autoconfigure:jar:2.3.2.RELEASE:compile
[INFO] |  |  +- org.springframework.cloud:spring-cloud-netflix-ribbon:jar:2.2.4.RELEASE:compile
[INFO] |  |  |  \- org.springframework.cloud:spring-cloud-netflix-archaius:jar:2.2.4.RELEASE:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-starter-aop:jar:2.3.2.RELEASE:compile
[INFO] |  |  |  +- org.springframework:spring-aop:jar:5.2.8.RELEASE:compile
[INFO] |  |  |  \- org.aspectj:aspectjweaver:jar:1.9.6:compile
[INFO] |  |  \- io.github.openfeign.form:feign-form-spring:jar:3.8.0:compile
[INFO] |  |     +- io.github.openfeign.form:feign-form:jar:3.8.0:compile
[INFO] |  |     \- commons-fileupload:commons-fileupload:jar:1.4:compile
[INFO] |  |        \- commons-io:commons-io:jar:2.2:compile
[INFO] |  +- org.springframework:spring-web:jar:5.2.8.RELEASE:compile
[INFO] |  |  \- org.springframework:spring-beans:jar:5.2.8.RELEASE:compile
[INFO] |  +- org.springframework.cloud:spring-cloud-commons:jar:2.2.4.RELEASE:compile
[INFO] |  |  \- org.springframework.security:spring-security-crypto:jar:5.3.3.RELEASE:compile
[INFO] |  +- io.github.openfeign:feign-core:jar:10.10.1:compile
[INFO] |  +- io.github.openfeign:feign-slf4j:jar:10.10.1:compile
[INFO] |  |  \- org.slf4j:slf4j-api:jar:1.7.30:compile
[INFO] |  \- io.github.openfeign:feign-hystrix:jar:10.10.1:compile
[INFO] |     +- com.netflix.archaius:archaius-core:jar:0.7.6:compile
[INFO] |     |  +- com.google.code.findbugs:jsr305:jar:3.0.1:runtime
[INFO] |     |  +- commons-configuration:commons-configuration:jar:1.8:runtime
[INFO] |     |  |  \- commons-lang:commons-lang:jar:2.6:runtime
[INFO] |     |  +- com.google.guava:guava:jar:29.0-jre:runtime
[INFO] |     |  |  +- com.google.guava:failureaccess:jar:1.0.1:runtime
[INFO] |     |  |  +- com.google.guava:listenablefuture:jar:9999.0-empty-to-avoid-conflict-with-guava:runtime
[INFO] |     |  |  +- org.checkerframework:checker-qual:jar:2.11.1:runtime
[INFO] |     |  |  +- com.google.errorprone:error_prone_annotations:jar:2.3.4:runtime
[INFO] |     |  |  \- com.google.j2objc:j2objc-annotations:jar:1.3:runtime
[INFO] |     |  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.11.1:compile
[INFO] |     |  \- com.fasterxml.jackson.core:jackson-core:jar:2.11.1:compile
[INFO] |     \- com.netflix.hystrix:hystrix-core:jar:1.5.18:compile
[INFO] |        \- io.reactivex:rxjava:jar:1.3.8:compile
[INFO] +- org.springframework.boot:spring-boot-starter-actuator:jar:2.3.2.RELEASE:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter:jar:2.3.2.RELEASE:compile
[INFO] |  |  +- org.springframework.boot:spring-boot:jar:2.3.2.RELEASE:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-starter-logging:jar:2.3.2.RELEASE:compile
[INFO] |  |  |  +- ch.qos.logback:logback-classic:jar:1.2.3:compile
[INFO] |  |  |  |  \- ch.qos.logback:logback-core:jar:1.2.3:compile
[INFO] |  |  |  +- org.apache.logging.log4j:log4j-to-slf4j:jar:2.13.3:compile
[INFO] |  |  |  |  \- org.apache.logging.log4j:log4j-api:jar:2.13.3:compile
[INFO] |  |  |  \- org.slf4j:jul-to-slf4j:jar:1.7.30:compile
[INFO] |  |  +- jakarta.annotation:jakarta.annotation-api:jar:1.3.5:compile
[INFO] |  |  \- org.yaml:snakeyaml:jar:1.26:compile
[INFO] |  +- org.springframework.boot:spring-boot-actuator-autoconfigure:jar:2.3.2.RELEASE:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-actuator:jar:2.3.2.RELEASE:compile
[INFO] |  |  +- com.fasterxml.jackson.core:jackson-databind:jar:2.11.1:compile
[INFO] |  |  \- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:jar:2.11.1:runtime
[INFO] |  \- io.micrometer:micrometer-core:jar:1.5.3:compile
[INFO] |     +- org.hdrhistogram:HdrHistogram:jar:2.1.12:compile
[INFO] |     \- org.latencyutils:LatencyUtils:jar:2.0.3:runtime
[INFO] +- org.springframework.boot:spring-boot-starter-test:jar:2.3.2.RELEASE:test
[INFO] |  +- org.springframework.boot:spring-boot-test:jar:2.3.2.RELEASE:test
[INFO] |  +- org.springframework.boot:spring-boot-test-autoconfigure:jar:2.3.2.RELEASE:test
[INFO] |  +- com.jayway.jsonpath:json-path:jar:2.4.0:test
[INFO] |  |  \- net.minidev:json-smart:jar:2.3:test
[INFO] |  |     \- net.minidev:accessors-smart:jar:1.2:test
[INFO] |  |        \- org.ow2.asm:asm:jar:5.0.4:test
[INFO] |  +- jakarta.xml.bind:jakarta.xml.bind-api:jar:2.3.3:test
[INFO] |  |  \- jakarta.activation:jakarta.activation-api:jar:1.2.2:test
[INFO] |  +- org.assertj:assertj-core:jar:3.16.1:test
[INFO] |  +- org.hamcrest:hamcrest:jar:2.2:test
[INFO] |  +- org.junit.jupiter:junit-jupiter:jar:5.6.2:test
[INFO] |  |  +- org.junit.jupiter:junit-jupiter-api:jar:5.6.2:test
[INFO] |  |  |  +- org.opentest4j:opentest4j:jar:1.2.0:test
[INFO] |  |  |  \- org.junit.platform:junit-platform-commons:jar:1.6.2:test
[INFO] |  |  +- org.junit.jupiter:junit-jupiter-params:jar:5.6.2:test
[INFO] |  |  \- org.junit.jupiter:junit-jupiter-engine:jar:5.6.2:test
[INFO] |  +- org.junit.vintage:junit-vintage-engine:jar:5.6.2:test
[INFO] |  |  +- org.apiguardian:apiguardian-api:jar:1.1.0:test
[INFO] |  |  +- org.junit.platform:junit-platform-engine:jar:1.6.2:test
[INFO] |  |  \- junit:junit:jar:4.13:test
[INFO] |  +- org.mockito:mockito-core:jar:3.3.3:test
[INFO] |  |  +- net.bytebuddy:byte-buddy:jar:1.10.13:test
[INFO] |  |  +- net.bytebuddy:byte-buddy-agent:jar:1.10.13:test
[INFO] |  |  \- org.objenesis:objenesis:jar:2.6:test
[INFO] |  +- org.mockito:mockito-junit-jupiter:jar:3.3.3:test
[INFO] |  +- org.skyscreamer:jsonassert:jar:1.5.0:test
[INFO] |  |  \- com.vaadin.external.google:android-json:jar:0.0.20131108.vaadin1:test
[INFO] |  +- org.springframework:spring-core:jar:5.2.8.RELEASE:compile
[INFO] |  |  \- org.springframework:spring-jcl:jar:5.2.8.RELEASE:compile
[INFO] |  +- org.springframework:spring-test:jar:5.2.8.RELEASE:test
[INFO] |  \- org.xmlunit:xmlunit-core:jar:2.7.0:test
[INFO] +- org.projectlombok:lombok:jar:1.18.12:provided
[INFO] +- io.github.openfeign:feign-okhttp:jar:10.10.1:compile
[INFO] \- com.squareup.okhttp3:okhttp:jar:3.14.9:compile
[INFO]    \- com.squareup.okio:okio:jar:1.17.2:compile
[I
```

https://maven.apache.org/plugins/maven-dependency-plugin/usage.html

```
[INFO] --- maven-dependency-plugin:3.1.2:analyze-dep-mgt (default-cli) @ service-broker-filter-core ---
[INFO] Found Resolved Dependency/DependencyManagement mismatches:
[INFO] 	Ignoring Direct Dependencies.
[INFO] org.springframework.security:spring-security-crypto:jar was excluded in DepMgt, but version 5.3.3.RELEASE has been found in the dependency tree.
[INFO] com.google.code.findbugs:jsr305:jar was excluded in DepMgt, but version 3.0.1 has been found in the dependency tree.
[INFO] com.fasterxml.jackson.core:jackson-core:jar was excluded in DepMgt, but version 2.11.1 has been found in the dependency tree.
[INFO] ch.qos.logback:logback-classic:jar was excluded in DepMgt, but version 1.2.3 has been found in the dependency tree.
[INFO] com.fasterxml.jackson.core:jackson-databind:jar was excluded in DepMgt, but version 2.11.1 has been found in the dependency tree.
[INFO] org.springframework:spring-core:jar was excluded in DepMgt, but version 5.2.8.RELEASE has been found in the dependency tree.
[WARNING] Potential problems found in Dependency Management

Could not resolve dependencies for project com.orange.cloud.servicebroker:service-broker-filter-securitygroups:jar:2.4.0.BUILD-SNAPSHOT: Failure to find com.orange.cloud.servicebroker:service-broker-filter-core:jar:2.4.0.BUILD-SNAPSHOT in https://repo.spring.io/snapshot/
```

```
INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]
[INFO] service-broker-filter                                              [pom]
[INFO] service-broker-filter-core                                         [jar]
[INFO] service-broker-filter-securitygroups                               [jar]
[INFO] integration-test                                                   [jar]
[INFO]
[INFO] --------< com.orange.cloud.servicebroker:service-broker-filter >--------
[INFO] Building service-broker-filter 2.4.0.BUILD-SNAPSHOT                [1/4]
[INFO] --------------------------------[ pom ]---------------------------------
[INFO]
[INFO] --- maven-dependency-plugin:3.1.2:analyze-dep-mgt (default-cli) @ service-broker-filter ---
[INFO] Found Resolved Dependency/DependencyManagement mismatches:
[INFO] 	Ignoring Direct Dependencies.
[INFO] 	None
[INFO]
[INFO] -----< com.orange.cloud.servicebroker:service-broker-filter-core >------
[INFO] Building service-broker-filter-core 2.4.0.BUILD-SNAPSHOT           [2/4]
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-dependency-plugin:3.1.2:analyze-dep-mgt (default-cli) @ service-broker-filter-core ---
[INFO] Found Resolved Dependency/DependencyManagement mismatches:
[INFO] 	Ignoring Direct Dependencies.
[INFO] org.springframework.security:spring-security-crypto:jar was excluded in DepMgt, but version 5.3.3.RELEASE has been found in the dependency tree.
[INFO] com.google.code.findbugs:jsr305:jar was excluded in DepMgt, but version 3.0.1 has been found in the dependency tree.
[INFO] com.fasterxml.jackson.core:jackson-core:jar was excluded in DepMgt, but version 2.11.1 has been found in the dependency tree.
[INFO] ch.qos.logback:logback-classic:jar was excluded in DepMgt, but version 1.2.3 has been found in the dependency tree.
[INFO] com.fasterxml.jackson.core:jackson-databind:jar was excluded in DepMgt, but version 2.11.1 has been found in the dependency tree.
[INFO] org.springframework:spring-core:jar was excluded in DepMgt, but version 5.2.8.RELEASE has been found in the dependency tree.
[WARNING] Potential problems found in Dependency Management
[INFO]
[INFO] --< com.orange.cloud.servicebroker:service-broker-filter-securitygroups >--
[INFO] Building service-broker-filter-securitygroups 2.4.0.BUILD-SNAPSHOT [3/4]
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-dependency-plugin:3.1.2:analyze-dep-mgt (default-cli) @ service-broker-filter-securitygroups ---
[INFO] Found Resolved Dependency/DependencyManagement mismatches:
[INFO] 	Ignoring Direct Dependencies.
[INFO] commons-logging:commons-logging:jar was excluded in DepMgt, but version 1.1.1 has been found in the dependency tree.
[INFO] ch.qos.logback:logback-classic:jar was excluded in DepMgt, but version 1.2.3 has been found in the dependency tree.
[INFO] org.slf4j:jcl-over-slf4j:jar was excluded in DepMgt, but version 1.7.30 has been found in the dependency tree.
[WARNING] Potential problems found in Dependency Management
[INFO]
[INFO] ----------< com.orange.cloud.servicebroker:integration-test >-----------
[INFO] Building integration-test 2.4.0.BUILD-SNAPSHOT                     [4/4]
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-dependency-plugin:3.1.2:analyze-dep-mgt (default-cli) @ integration-test ---
[INFO] Found Resolved Dependency/DependencyManagement mismatches:
[INFO] 	Ignoring Direct Dependencies.
[INFO] ch.qos.logback:logback-classic:jar was excluded in DepMgt, but version 1.2.3 has been found in the dependency tree.
[INFO] org.slf4j:jcl-over-slf4j:jar was excluded in DepMgt, but version 1.7.30 has been found in the dependency tree.
[WARNING] Potential problems found in Dependency Management

```


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

        * [x] **Stay with servlet api non reactive engine and use feign blocking apis**
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


   * other issue at binding with webflux
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
      * [x] **revert to using webmvc for now and not webflux**


* [x] fix service binding timeout
   ```
  Binding service gberche to app gberche in org service-sandbox / space cf-redis as gberche...
  Unexpected Response
  Response code: 504
  CC code:       0
  CC error code: 
  Request ID:    9cb2e5d5-608c-4f06-5123-3c44a6b19154::8ee89267-9992-4c7d-8cd0-dbebb16da2fe
  Description:   {
    "description": "The request to the service broker timed out: https://redis-sec-group-broker-filter.redacted-cfapi/v2/service_instances/b28308b7-78b9-4ec3-9e45-e888ac5f97aa/service_bindings/7247f15f-aa8d-4e15-8648-922536dd3fd0?accepts_incomplete=true",
    "error_code": "CF-HttpClientTimeout",
    "code": 10001,
    "http": {
      "uri": "https://redis-sec-group-broker-filter.redacted-cfapi/v2/service_instances/b28308b7-78b9-4ec3-9e45-e888ac5f97aa/service_bindings/7247f15f-aa8d-4e15-8648-922536dd3fd0?accepts_incomplete=true",
      "method": "PUT"
    }
  }
   ```
   * [ ] check ASG to CF API
   * [ ] check verbose traces in cf java client

* [x] investigate and fix start up issue
   ```
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT java.lang.IllegalStateException: Failed to execute CommandLineRunner
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:798) ~[spring-boot-2.3.3.RELEASE.jar:2.3.3.RELEASE]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.SpringApplication.callRunners(SpringApplication.java:779) ~[spring-boot-2.3.3.RELEASE.jar:2.3.3.RELEASE]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:322) ~[spring-boot-2.3.3.RELEASE.jar:2.3.3.RELEASE]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1237) ~[spring-boot-2.3.3.RELEASE.jar:2.3.3.RELEASE]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1226) ~[spring-boot-2.3.3.RELEASE.jar:2.3.3.RELEASE]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at com.orange.cloud.servicebroker.filter.securitygroups.BrokerFilterApplication.main(BrokerFilterApplication.java:30) ~[classes/:2.4.0.BUILD-SNAPSHOT]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source) ~[na:na]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source) ~[na:na]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.loader.MainMethodRunner.run(MainMethodRunner.java:49) ~[app/:na]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.loader.Launcher.launch(Launcher.java:109) ~[app/:na]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.loader.Launcher.launch(Launcher.java:58) ~[app/:na]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.loader.JarLauncher.main(JarLauncher.java:88) ~[app/:na]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT Caused by: java.lang.IllegalStateException: Timeout on blocking read for 180000 MILLISECONDS
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.BlockingSingleSubscriber.blockingGet(BlockingSingleSubscriber.java:123) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.Mono.block(Mono.java:1704) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at com.orange.cloud.servicebroker.filter.securitygroups.config.CheckCloudFoundryConnection.run(CheckCloudFoundryConnection.java:25) ~[classes/:2.4.0.BUILD-SNAPSHOT]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:795) ~[spring-boot-2.3.3.RELEASE.jar:2.3.3.RELEASE]
         2020-09-08T09:15:00.56+0200 [APP/PROC/WEB/1] OUT 	... 13 common frames omitted
   ```
    * [x] network L4 (ASG) are properly opened, tested with curl from the container
    * [x] take a threaddump using actuator, see doc at https://docs.spring.io/spring-boot/docs/current/actuator-api/html/#threaddump-retrieving-text
       ```
         "main" - Thread t@1
            java.lang.Thread.State: TIMED_WAITING
         	at java.base@14.0.2/jdk.internal.misc.Unsafe.park(Native Method)
         	- parking to wait for <99b8c81> (a java.util.concurrent.CountDownLatch$Sync)
         	at java.base@14.0.2/java.util.concurrent.locks.LockSupport.parkNanos(Unknown Source)
         	at java.base@14.0.2/java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(Unknown Source)
         	at java.base@14.0.2/java.util.concurrent.locks.AbstractQueuedSynchronizer.tryAcquireSharedNanos(Unknown Source)
         	at java.base@14.0.2/java.util.concurrent.CountDownLatch.await(Unknown Source)
         	at reactor.core.publisher.BlockingSingleSubscriber.blockingGet(BlockingSingleSubscriber.java:121)
         	at reactor.core.publisher.Mono.block(Mono.java:1704)
         	at com.orange.cloud.servicebroker.filter.securitygroups.config.CheckCloudFoundryConnection.run(CheckCloudFoundryConnection.java:25)
         	at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:795)
         	at org.springframework.boot.SpringApplication.callRunners(SpringApplication.java:779)
         	at org.springframework.boot.SpringApplication.run(SpringApplication.java:322)
         	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1237)
         	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1226)
         	at com.orange.cloud.servicebroker.filter.securitygroups.BrokerFilterApplication.main(BrokerFilterApplication.java:30)
         	at java.base@14.0.2/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
         	at java.base@14.0.2/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
         	at java.base@14.0.2/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
         	at java.base@14.0.2/java.lang.reflect.Method.invoke(Unknown Source)
         	at app//org.springframework.boot.loader.MainMethodRunner.run(MainMethodRunner.java:49)
         	at app//org.springframework.boot.loader.Launcher.launch(Launcher.java:109)
         	at app//org.springframework.boot.loader.Launcher.launch(Launcher.java:58)
         	at app//org.springframework.boot.loader.JarLauncher.main(JarLauncher.java:88)
       ```
* [x] investigate the following error:
    ```
    2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 2020-09-07 17:39:30.078  WARN 19 --- [-client-epoll-1] i.n.c.AbstractChannelHandlerContext      : An exception 'java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter' [enable DEBUG level for full stacktrace] was thrown by a user handler's exceptionCaught() method while handling the following exception:
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.jsonwebtoken.impl.Base64Codec.decode(Base64Codec.java:26) ~[jjwt-0.9.1.jar:0.9.1]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.jsonwebtoken.impl.Base64UrlCodec.decode(Base64UrlCodec.java:78) ~[jjwt-0.9.1.jar:0.9.1]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.jsonwebtoken.impl.AbstractTextCodec.decodeToString(AbstractTextCodec.java:36) ~[jjwt-0.9.1.jar:0.9.1]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.jsonwebtoken.impl.DefaultJwtParser.parse(DefaultJwtParser.java:251) ~[jjwt-0.9.1.jar:0.9.1]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.jsonwebtoken.impl.DefaultJwtParser.parse(DefaultJwtParser.java:481) ~[jjwt-0.9.1.jar:0.9.1]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.jsonwebtoken.impl.DefaultJwtParser.parseClaimsJwt(DefaultJwtParser.java:514) ~[jjwt-0.9.1.jar:0.9.1]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at org.cloudfoundry.reactor.tokenprovider.AbstractUaaTokenProvider.parseToken(AbstractUaaTokenProvider.java:154) ~[cloudfoundry-client-reactor-4.9.0.RELEASE.jar:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at org.cloudfoundry.reactor.tokenprovider.AbstractUaaTokenProvider.lambda$null$3(AbstractUaaTokenProvider.java:196) ~[cloudfoundry-client-reactor-4.9.0.RELEASE.jar:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at java.base/java.util.Optional.ifPresent(Unknown Source) ~[na:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at org.cloudfoundry.reactor.tokenprovider.AbstractUaaTokenProvider.lambda$extractRefreshToken$4(AbstractUaaTokenProvider.java:192) ~[cloudfoundry-client-reactor-4.9.0.RELEASE.jar:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxPeek$PeekSubscriber.onNext(FluxPeek.java:177) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:114) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:114) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxHandle$HandleSubscriber.onNext(FluxHandle.java:112) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onNext(FluxMap.java:213) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxDoFinally$DoFinallySubscriber.onNext(FluxDoFinally.java:123) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxHandleFuseable$HandleFuseableSubscriber.onNext(FluxHandleFuseable.java:178) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxContextStart$ContextStartSubscriber.onNext(FluxContextStart.java:96) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1782) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.MonoCollectList$MonoCollectListSubscriber.onComplete(MonoCollectList.java:121) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxPeek$PeekSubscriber.onComplete(FluxPeek.java:252) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:136) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxDoFinally$DoFinallySubscriber.onComplete(FluxDoFinally.java:138) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:136) ~[reactor-core-3.3.9.RELEASE.jar:3.3.9.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.netty.channel.FluxReceive.onInboundComplete(FluxReceive.java:378) ~[reactor-netty-0.9.11.RELEASE.jar:0.9.11.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.netty.channel.ChannelOperations.onInboundComplete(ChannelOperations.java:373) ~[reactor-netty-0.9.11.RELEASE.jar:0.9.11.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.netty.channel.ChannelOperations.terminate(ChannelOperations.java:429) ~[reactor-netty-0.9.11.RELEASE.jar:0.9.11.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.netty.http.client.HttpClientOperations.onInboundNext(HttpClientOperations.java:645) ~[reactor-netty-0.9.11.RELEASE.jar:0.9.11.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at reactor.netty.channel.ChannelOperationsHandler.channelRead(ChannelOperationsHandler.java:96) ~[reactor-netty-0.9.11.RELEASE.jar:0.9.11.RELEASE]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103) ~[netty-codec-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.CombinedChannelDuplexHandler$DelegatingChannelHandlerContext.fireChannelRead(CombinedChannelDuplexHandler.java:436) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:324) ~[netty-codec-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:296) ~[netty-codec-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.CombinedChannelDuplexHandler.channelRead(CombinedChannelDuplexHandler.java:251) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.logging.LoggingHandler.channelRead(LoggingHandler.java:271) ~[netty-handler-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.ssl.SslHandler.unwrap(SslHandler.java:1526) ~[netty-handler-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.ssl.SslHandler.decodeJdkCompatible(SslHandler.java:1275) ~[netty-handler-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.ssl.SslHandler.decode(SslHandler.java:1322) ~[netty-handler-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:501) ~[netty-codec-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:440) ~[netty-codec-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:276) ~[netty-codec-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919) ~[netty-transport-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:792) ~[netty-transport-native-epoll-4.1.51.Final-linux-x86_64.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:475) ~[netty-transport-native-epoll-4.1.51.Final-linux-x86_64.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:378) ~[netty-transport-native-epoll-4.1.51.Final-linux-x86_64.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989) ~[netty-common-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.51.Final.jar:4.1.51.Final]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT Caused by: java.lang.ClassNotFoundException: javax.xml.bind.DatatypeConverter
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at java.base/java.net.URLClassLoader.findClass(Unknown Source) ~[na:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at java.base/java.lang.ClassLoader.loadClass(Unknown Source) ~[na:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at org.springframework.boot.loader.LaunchedURLClassLoader.loadClass(LaunchedURLClassLoader.java:135) ~[app/:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	at java.base/java.lang.ClassLoader.loadClass(Unknown Source) ~[na:na]
     2020-09-07T19:39:30.07+0200 [APP/PROC/WEB/1] OUT 	... 67 common frames omitted
    ```
     * [x] root cause in JWT library and java9+ see https://github.com/jwtk/jjwt/issues/333#issuecomment-409754833 fixed in 0.10.0
        * [ ] Check dependency to jjwt lib in cf-java-client
        ```
       [INFO] +- org.cloudfoundry:cloudfoundry-client-reactor:jar:4.9.0.RELEASE:compile
       [...]
       [INFO] |  +- io.jsonwebtoken:jjwt:jar:0.9.1:compile 
        ``` 
        * [x] Check cf-java-client java9 support
        * [x] Report the issue to cf-java-client. https://github.com/cloudfoundry/cf-java-client/issues/1067
        * [ ] Add maven dependency management to 0.10.0
           * has been split into multiple artifacts, see https://github.com/jwtk/jjwt#maven
           * might be easier and more durable to wait for cf-java-client fix
        * [x] **revert to using java8**
