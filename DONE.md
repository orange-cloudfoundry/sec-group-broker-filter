This file holds implementation notes from previous features. It aims at being easier to find than commit message, while not yet reaching full design docs

# Dependency bump

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


* [x] build & serve tarball
  * [x] set up circle ci build
     * [x] test it ./service-broker-filter-securitygroups/target/service-broker-filter-securitygroups-2.4.0.BUILD-SNAPSHOT.jar
  * [ ] manually host tarball as a github release
  * [x] check whether travis now hosts artefacts. 
     * Still seems to direct to your own S3 bucket, see https://docs.travis-ci.com/user/uploading-artifacts/
  * [x] display build properties 
     * [x] using native spring boot plugin
        * [ ] coab branch to display it
     * [ ] using git maven plugin
     
in META-INF/build-info.properties
     
```properties
build.artifact=service-broker-filter-securitygroups
build.group=com.orange.cloud.servicebroker
build.name=service-broker-filter-securitygroups
build.time=2020-09-01T12\:30\:10.822Z
build.version=2.4.0.BUILD-SNAPSHOT
```

in BOOT-INF/classes/git.properties
```properties
#Generated by Git-Commit-Id-Plugin
#Tue Sep 01 15:31:33 CEST 2020
git.branch=bump-start-error-fixes
git.build.host=guillaume-dev-box
git.build.time=2020-09-01T15\:31\:33+0200
git.build.user.email=...
git.build.user.name=...
git.build.version=2.4.0.BUILD-SNAPSHOT
git.closest.tag.commit.count=46
git.closest.tag.name=v2.3.0.RELEASE
git.commit.id=3556b49371eefe00231a8a2ca5c5bd5eb39f73f3
git.commit.id.abbrev=3556b49
git.commit.id.describe=v2.3.0.RELEASE-46-g3556b49-dirty
git.commit.id.describe-short=v2.3.0.RELEASE-46-dirty
git.commit.message.full=Enable spring boot maven plugin to produce META-INF/build-info.properties\n\nFix warning about empty integration-tests jar
git.commit.message.short=Enable spring boot maven plugin to produce META-INF/build-info.properties
git.commit.time=2020-09-01T15\:19\:16+0200
git.commit.user.email=...
git.commit.user.name=...
git.dirty=true
git.local.branch.ahead=1
git.local.branch.behind=0
git.remote.origin.url=https\://github.com/orange-cloudfoundry/sec-group-broker-filter.git
git.tags=
git.total.commit.count=123

```

  * [x] debug start up failure with JVM exit
     * [x] increase log verbosity
        * [x] springboot debug mode
        * [x] springboot logging level
     * [x] disable port health check
     * root cause is lack of spring-boot-starter-web or spring-boot-starter-webflux dependency 
