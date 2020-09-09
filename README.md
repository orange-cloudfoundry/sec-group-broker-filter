

# sec-group-broker-filter 
[![CircleCI Build status](https://circleci.com/gh/orange-cloudfoundry/sec-group-broker-filter.svg?style=svg)](https://app.circleci.com/pipelines/github/orange-cloudfoundry/sec-group-broker-filter)
<!--  [![TravisCI Build Status](https://travis-ci.org/orange-cloudfoundry/sec-group-broker-filter.svg?branch=master)](https://travis-ci.org/orange-cloudfoundry/sec-group-broker-filter) -->

This Cloud Foundry service broker is designed to be chained in front-of other service brokers and dynamically open [security groups](https://docs.cloudfoundry.org/adminguide/app-sec-groups.html) to let apps bound to service instance emmit outgoing traffic to IP addresses returned in the chained service instances credentials. 

![Overview seq diagram](http://plantuml.com/plantuml/svg/lP7DYi8m4CVlVOgvgGVnNfO53IWU164VeCHqA8D3KYT9mRUtMGLBDUX1lCuml__7A7QnhfIpGHmp2in_uGzDjH4NssebxeXIhOa3IWb6C_j-BHqPXEiIblijJ8qEcbspQCLrvCdPMQ5De4u7pE6Ap3mvs9tzYM_Zl6pvLBV6byg5-apg0zbwLanUwdsZYkJbBZIyoj9_vYEwO8XY_J-BR0D6i4ORIBCVrleMlBS-RhbCm1wmI7mF7aqK2cTeqZb4doILId7kGSQCeO-7tSDR-uJPjxuPDdD_0G00)

This is a similar broker chain as proposed into https://github.com/cloudfoundry-community/cf-subway

# Sample usage

```sh
# 1st deploy an upstream target broker accessible through its route: mysql-broker.mydomain.org. 
# Don't register the target broker in CF (the filter broker offering will be registered instead)
[...]

# then deploy sec-group-broker-filter and configure it to proxy traffic to the target broker:
# you may want to deploy sec-group-broker-filter using cloudfoundry:

$ vi manifest.yml
---
applications:
- name: sec-group-broker-filter
  memory: 256M
  instances: 1
  path: sec-group-broker-filter-<version>.jar

  # URL to register into the marketplace
  host: sec-group-chained-mysql-broker
  domain: mydomain.org

  env:
    # Where to send received traffic: URL to the target broker.
    BROKER_FILTER_URL=https://mysql-broker.mydomain.org
    # basic auth credentials to use while sending traffic to the target broker 
    BROKER_FILTER_USER=user
    BROKER_FILTER_PASSWORD=password

    # Optionally restrict the IPs/ports in created security groups to a set of trusted destinations. 
    # In case the target broker gets compromised and returns unrelated IPs in credentials, the binding response 
    # will error, preventing unathorized accesses to unrelated destinations.
    # Trusted destinations is expressed as either a single IP address (10.0.11.0), an IP address range (e.g. 10.0.11.1-10.0.11.2), 
    # or a CIDR block (e.g. 10.0.11.0/24). 
    # If empty or unspecified, any IP adress returned from the binding response will be granted access in 
    # created security groups
    BROKER_FILTER_TRUSTED_DESTINATION_HOSTS=192.0.1.0-192.0.2.0
    # An optional trusted destination ports. Can be a single port, multiple comma-separated ports, or a single range of ports. 
    # Examples: 3306 3306,3307 3300-3400
    # If empty or unspecified, any port returned from the binding response 
    # will be granted access in created security groups
    BROKER_FILTER_TRUSTED_DESTINATION_PORTS=3306
     
    # CloudFoundry CC api host 
    CLOUDFOUNDRY_HOST: api.yourdomain.com
    # CloudFoudry user with Org admin privileges on orgs where services will be bound
    CLOUDFOUNDRY_USER: admin
    # CloudFoudry user password
    CLOUDFOUNDRY_PASSWORD: password
    
    # Optionally enable that both the filter broker offering and target broker offering coexist in the marketplace
    # To avoid conflicts in service offering id from both, the filter broker offering will have the specified suffix added
    #BROKER_FILTER_SERVICEOFFERING_SUFFIX=-sec

    
# deploy the broker    
$ cf push 

# register the broker 
$ cf create-service-broker mysql user mypwd https://sec-group-chained-mysql-broker.mydomain.org

# expose the service in the marketplace
$ cf enable-service-access mysql -o myorg 

```` 

# Supported credentials format

The protocol, FQDN or IP addresses, and TCP ports are extracted from the credentials returned by the chained broker from the [standard fields](https://docs.cloudfoundry.org/services/binding-credentials.html):
* URI
* host
* port

Support for additional fields would be added later (e.g [memcache-release](https://github.com/cloudfoundry-community/memcache-release#example-vcap_services-credentials)'s  ``vip`` or ``servers`` list)

The FQDN is looked up and resolved into a set of IP addresses.
The prococol is currently always mapped to ``tcp``. In the future, some well known protocol could be mapped to ``udp`` instead

For example, in the following credentials hash, 

```json
{
  "name": "ad_c6f4446532610ab",
  "hostname": "us-cdbr-east-03.cleardb.com",
  "port": "3306",
  "username": "b5d435f40dd2b2",
  "password": "ebfc00ac",
  "uri": "mysql://b5d435f40dd2b2:ebfc00ac@us-cdbr-east-03.cleardb.com:3306/ad_c6f4446532610ab",
  "jdbcUrl": "jdbc:mysql://b5d435f40dd2b2:ebfc00ac@us-cdbr-east-03.cleardb.com:3306/ad_c6f4446532610ab"
}
```

the FQDN us-cdbr-east-03.cleardb.com would be resolved into 141.8.225.68, 141.8.225.69 

The resulting security group opened would be:

```json
[
  {"protocol":"tcp","destination":"141.8.225.68/31","ports":"3306"}
]
```
# IP/Port restriction

 In order to be protected against a compromise filtered service broker (e.g. p-mysql) (that would return unowned, unrelated IPs in the binding response), 
 IP/port range can be restricted.
 Set following env properties to restrict IP/port range.
```
    # An optional trusted IPs: single IP address, IP address range (e.g. 192.0.1.0-192.0.2.0), or a CIDR block to allow security groups to. If empty or unspecified, any IP adress returned from the binding response will be granted access in created security groups
    BROKER_FILTER_TRUSTED_DESTINATION_HOSTS=192.0.1.0-192.0.2.0
    # An optional trusted port range. If empty or unspecified, any port returned from the binding response will be granted access in created security groups
    BROKER_FILTER_TRUSTED_DESTINATION_PORTS=
```

# Roadmap

The bugs and features enhancements are managed through github issues, possibly through [huboard](https://huboard.com/orange-cloudfoundry/sec-group-broker-filter#/milestones) to have overview of milestones.

# Development
The project depends on Java 14.  To build from source and install to your local Maven cache, run the following:

```shell
$ ./mvnw clean install
```

## Integration tests

The integration tests deploy sec-group-broker-filter as a space-scoped service broker and expect a sample service broker to delegate to (e.g. static-cred-broker). 

The integration validates the deployment of sec-group-broker-filter, registers is as a space-scoped service broker, verify it has a service offering matching the backing broker service offering, and instanciates a service instance. Note that currently, the network ACL to the service binding are not yet asserted, see related https://github.com/orange-cloudfoundry/sec-group-broker-filter/issues/14 

Note that since this does not require CF admin permissions, such tests can run on a public CF instance such as PWS.

To run the integration tests, run the following:

```
$ ./mvnw -Pintegration-test \
    -Dtest.apiHost=... \
    -Dtest.username=... \
    -Dtest.password=... \
    -Dtest.proxy.host=... \
    -Dtest.proxy.password=... \
    -Dtest.proxy.port=... \
    -Dtest.proxy.username=... \
    -Dtest.skipSslValidation=... \
    -Dtest.org=... \
    -Dtest.domain=... \
    -Dbroker.filter.url=... \
    -Dbroker.filter.user=... \
    -Dbroker.filter.password=... \
clean test
```

# Releasing

Prereqs: checkout the branch to release (master), and make sure it is up-to-date w.r.t. the github remote.
 
Releasing is made using [maven release plugin](http://maven.apache.org/maven-release/maven-release-plugin/) as follows :
 
 ```shell
 
 $ mvn release:prepare --batch-mode -Dtag={your git tag} -DreleaseVersion={release version to be set} -DdevelopmentVersion={next snapshot version to be set}
 
 # ex : mvn release:prepare --batch-mode -Dtag=v2.3.0.RELEASE -DreleaseVersion=2.3.0.RELEASE -DdevelopmentVersion=2.4.0.BUILD
 
 ```
 
The jar file is available there : <your_workspace>/service-broker-filter-securitygroups/target/service-broker-filter-securitygroups-{release version}.jar
 ```shell
  
 # ex : ./service-broker-filter-securitygroups/target/service-broker-filter-securitygroups-2.3.0.RELEASE.jar
 
 ```

You must upload manually this jar file

Following the release:
- edit the release notes in github
- clean up your local workspace using `mvn release:clean`

In case of issues, try:
* `mvn release:rollback` (which creates a new commit reverting changes)
    * possibly revert the commits in git (`git reset --hard commitid`), 
* clean up the git tag `git tag -d vXX && git push --delete origin vXX`, 
* `mvn release:clean`
* fix the root cause and retry.


**IMPORTANT**
Integration tests should be run against an empty Cloud Foundry instance. The integration tests are destructive, affecting nearly everything on an instance given the chance.

The integration tests require a running instance of Cloud Foundry to test against.  To configure the integration tests with the appropriate connection information use the following environment variables:

Name | Description
---- | -----------
`TEST_APIHOST` | The host of Cloud Foundry instance.  Typically something like `api.local.pcfdev.io`.
`TEST_USERNAME` | The test user's name
`TEST_PASSWORD` | The test user's password
`TEST_SKIPSSLVALIDATION` | Whether to skip SSL validation when connecting to the Cloud Foundry instance.  Typically `true` when connecting to a PCF Dev instance.
`TEST_ORG` | The Org used for test suite
`TEST_DOMAIN` | The Domain used for test suite

# Credits

This work has been inspired by the [cf-subway](https://github.com/cloudfoundry-community/cf-subway) great idea contributed by Dr Nic.
This repo tries to extend this idea into flexible framework for reusable filters/facade, see service-broker-filter-core subproject.

# FAQ

## When is this broker useful ?

When some legacy services exposed in the marketplace need to be protected at the level 3/4 (IP/TCP) and level 7 authentication through provided credentials is not sufficient (e.g. vulnerable to denial of service or brute force password attacks).

In this case, the default security groups don't let apps reach the IP adresses ranges where such legacy services are exposed. This broker dynamically creates and assigns security groups to access services returned in credentials.

## When are credentials FQDN looked up, when are ASG updated if DNS resolution changes?

Initially the FQDN are resolved into IP addresses when binding is requested, which translates into an application security group (ASG) being created for the space. The resulting ASG is currently not updated afterwards.

A future evolution could be to periodically (at the end of the TTL period) lookup the IP address and update the corresponding security group. This would also require a [rolling update](http://v3-apidocs.cloudfoundry.org/version/release-candidate/index.html#terminate-a-process-instance) of the bound application containers, as the [New security rules apply to new containers as they are created, but not to containers that are already running when the rules are created.](http://docs.cloudfoundry.org/adminguide/app-sec-groups.html#binding-groups)

## Can a single sec-group-broker-filter proxy multiple upstream brokers ?

Yes, add as many routes to the sec-group-broker-filter as there are up stream brokers, each with its own configuration entries.
