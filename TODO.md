* [~] publish test status
   * [ ] configure surefire-report-plugin to generate report
      * [ ] Configure a single aggregated report for all submodules
         * https://stackoverflow.com/questions/21585037/maven-reporting-and-site-generation-for-multiple-module-project mentions need to have separate aggregator and parent pom
            * [ ] configure separate parent and aggregator
            ```
           [ERROR]   The project com.orange.cloud.servicebroker:service-broker-filter-core:2.4.0.BUILD-SNAPSHOT (/home/guillaume/code/sec-group-broker-filter/service-broker-filter-core/pom.xml) has 1 error
           [ERROR]     Non-resolvable parent POM for com.orange.cloud.servicebroker:service-broker-filter-core:2.4.0.BUILD-SNAPSHOT: Could not find artifact com.orange.cloud.servicebroker:service-broker-filter-parent:pom:2.4.0.BUILD-SNAPSHOT and 'parent.relativePath' points at wrong local POM @ line 20, column 13 -> [Help 2]
            ```
           * => squashed and suspended for now.
      * [x] Copy each report individually with a unique name
   * [ ] review list of tests
      * Report has broken GIFs
         * https://stackoverflow.com/questions/21432663/how-to-get-the-icons-for-the-resulted-maven-surefire-report-plugin mentions `mvn site -DgenerateReports=false` however fails with 
         ``` 
            Failure to find org.springframework.boot:spring-boot-starter-parent:xml:site_en:2.3.3.RELEASE in https://repo.spring.io/snapshot/ was cached in the local repository, resolution will not be reattempted until the update interval of spring-snapshots has elapsed or updates are forced
         ```
           * https://github.com/spring-projects/spring-boot/issues/3358 says it's not in boot
   * [ ] add an href into a badge on README
   
* [x] fix prometheus exporter endpoint
* [ ] refine smoke test assertions
    * sec-group
        * direct: ASG being created and removed
        * indirect: closed ASG in the smoke test space
           * Pb: running-security-groups already include `services` ASG which opens all ports to all services
           ```
          cf security-group services
          Getting info for security group services as gberche...
          OK
                  
          Name    services
          Rules
          	[
          		{
          			"description": "any TCP to NET_CF_SERVICES",
          			"destination": "192.168.30.0/24",
          			"ports": "1-65000",
          			"protocol": "tcp"
          		},
          		{
          			"description": "any TCP to NET_CF_SERVICES_2",
          			"destination": "192.168.31.0/24",
          			"ports": "1-65000",
          			"protocol": "tcp"
          		}
          	]
          
               Organization      Space
          #0   service-sandbox   mongodb-smoke-tests
          #1   service-sandbox   cassandra-smoke-tests
 
           ```
          * How to assert that requests are rejected before the sec-group-broker-filters opens them ?
             * first bind the probe app to a redis instance not faced by sec-group-broker-filter
                * requires redis broker to be registered directly, at least in the smoke test space 
                   * [ ]  set up terraform to
                      * [ ] create smoke test space
                      * [ ] register redis broker with name "direct-p-redis-broker"
                   * [ ] modify smoke test to 
                      * [ ] `cf create-service instance redis -b direct-p-redis-broker` + `cf bs` 
                      * [ ] assert timeout or connection rejected from probe 
                      * [ ] `cf unbind-service` + `cf delete-service -f redis` 
                      * [ ] then proceed with existing probe asserts 
                      
    * [ ] actuator endpoint permissions
       * actuator/health is always reacheable without auth
       * actuator/ is always returning 401 without auth
* [ ] polish & merge
   * rebase/squash
* release

          
                   
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
