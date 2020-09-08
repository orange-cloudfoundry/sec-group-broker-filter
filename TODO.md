* [ ] set up smoke test
    * [ ] TF: set up smoke test space with security group
    * [ ] set up common-broker script
        * p-mysql
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
