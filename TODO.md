* [ ] set up smoke test
    * [ ] TF: set up smoke test space with security group
    * [ ] set up common-broker script
        * p-mysql
    * [x] sec-broker-filter does not seem to support service keys:
    ```
      cf create-service-key sec-group-broker-filter-cf-smoketest-1599561485 mykey
      Creating service key mykey for service instance sec-group-broker-filter-cf-smoketest-1599561485 as admin...
      Service broker error: applicationId
      FAILED
    ```     
       * [ ] skip service key in smoke test         
       * [x] implement service key in sec-group-filter         
          * [x] search for an existing issue https://github.com/orange-cloudfoundry/sec-group-broker-filter/issues/97         
          * benefits: enables service keys in the same space for apps that can't leverage service bindings (e.g. don't parse VCAP_SERVICES)
    * [ ] redis probe app fails with
       ```
      You must bind a Redis service instance to this application.
      
      You can run the following commands to create an instance and bind to it:
      
        $ cf create-service p-redis development redis-instance 
       ```
      * [x] look at probe source code https://github.com/orange-cloudfoundry/cf-redis-example-app/blob/9c2d51a7529b85d4382a5bf9536de44780ec3b30/lib/app.rb#L70-L79
         ```
        def redis_credentials
          service_name = ENV['service_name'] || "redis"
        
          if ENV['VCAP_SERVICES']
            all_pivotal_redis_credentials = CF::App::Credentials.find_all_by_all_service_tags(['Redis', 'Document'])
            if all_pivotal_redis_credentials && all_pivotal_redis_credentials.first
              all_pivotal_redis_credentials && all_pivotal_redis_credentials.first
            else
              redis_service_credentials = CF::App::Credentials.find_by_service_name(service_name)
              redis_service_credentials
            end
          end 
         ```
      * [x] check tags in catalog are missing when faced by sec-group-broker-filter
         * [x] check existing issue
         * [x] check original p-redis catalog indeed contains tags
         ```
         "services": [
            {
              "id": "EEA47C3A-569C-4C24-869D-0ADB5B337A4C",
              "name": "p-redis",
              "description": "Redis service to provide a key-value store",
              "bindable": true,
              "tags": [
                "pivotal",
                "redis"
              ],
              "plan_updateable": false,
              "plans": [
                {
                  "id": "C210CA06-E7E5-4F5D-A5AA-7A2C51CC290E",
                  "name": "shared-vm",
                  "description": "This plan provides a Redis server on a shared VM configured for data persistence.",
                  "metadata": {
                    "bullets": [
                      "Each instance shares the same VM",
                      "Single dedicated Redis process",
                      "Suitable for development & testing workloads"
                    ],
                    "displayName": "Shared-VM"
                  }
                }
              ],
              "metadata": {
                "displayName": "Redis",
                "documentationUrl": "http://docs.pivotal.io/redis/index.html",
                "imageUrl": "data:image/png;base64,iVBORw[...]",
                "providerDisplayName": "Pivotal",
                "supportUrl": "http://support.pivotal.io"
              }
            }
          ]
        }
 
         ```
         * [x] reproduce in a unit test
         * [x] fix the bug
         * Still not sufficient as current tags (from p-redis) and expected tags (from coa) don't match
            * [x] inject 'service_name' to the probe app systematically
          
                   
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
