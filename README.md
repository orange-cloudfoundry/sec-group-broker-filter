# sec-group-broker-filter

This Cloud Foundry service broker is designed to be chained in front-of other service brokers and dynamically open [security groups](https://docs.cloudfoundry.org/adminguide/app-sec-groups.html) to let apps bound to service instance emmit outgoing traffic to IP addresses returned in the chained service instances credentials.

![Overview seq diagram](http://plantuml.com/plantuml/svg/lP7DYi8m4CVlVOgvgGVnNfO53IWU164VeCHqA8D3KYT9mRUtMGLBDUX1lCuml__7A7QnhfIpGHmp2in_uGzDjH4NssebxeXIhOa3IWb6C_j-BHqPXEiIblijJ8qEcbspQCLrvCdPMQ5De4u7pE6Ap3mvs9tzYM_Zl6pvLBV6byg5-apg0zbwLanUwdsZYkJbBZIyoj9_vYEwO8XY_J-BR0D6i4ORIBCVrleMlBS-RhbCm1wmI7mF7aqK2cTeqZb4doILId7kGSQCeO-7tSDR-uJPjxuPDdD_0G00)

This is a similar broker chain as proposed into https://github.com/cloudfoundry-community/cf-subway

# Sample usage

```sh
# 1st deploy an upstream chained broker accessible through its route: mysql-broker.mydomain.org. Don't register it directly into CF
[...]

# then deploy sec-group-broker-filter and configure it to proxy traffic to the filtered broker:
# you may want to deploy sec-group-broker-filter using cloudfoundry:

$ vi manifest.yml
---
applications:
- name: sec-group-broker-filter
  memory: 256M
  instances: 1
  path: sec-group-broker-filter.war 

  # URL to register into the marketplace
  host: sec-group-chained-mysql-broker
  domain: mydomain.org

  env:
    # Where to send received traffic
    UPSTREAM.ID.SERVER_URL=https://mysql-broker.mydomain.org
    # Where traffic will be received from
    UPSTREAM.ID.INTERMEDIATE_ROUTE=sec-group-chained-mysql-broker.mydomain.org
    # Range of IP adress into which matching IP in returned credentials will triger opening of security groups
    # Outside this range, the bind request is transparently proxies without triggering any CC API action.
    UPSTREAM.ID.WHITE_LISTED_CIDRs="192.168.3.1/24,192.168.4.1/32"
    
    # CloudFoundry CC api url
    CLOUDFOUNDRY_API_URL: https://api.yourdomain.com
    # CloudFoudry user with Org admin privileges on orgs where services will be bound
    CLOUDFOUNDRY_CREDENTIALS_USER_ID: admin
    # CloudFoudry user password
    CLOUDFOUNDRY_CREDENTIALS_PASSWORD: password
    
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
  name: "ad_c6f4446532610ab",
  hostname: "us-cdbr-east-03.cleardb.com",
  port: "3306",
  username: "b5d435f40dd2b2",
  password: "ebfc00ac",
  uri: "mysql://b5d435f40dd2b2:ebfc00ac@us-cdbr-east-03.cleardb.com:3306/ad_c6f4446532610ab",
  jdbcUrl: "jdbc:mysql://b5d435f40dd2b2:ebfc00ac@us-cdbr-east-03.cleardb.com:3306/ad_c6f4446532610ab"
}
```

the FQDN us-cdbr-east-03.cleardb.com would be resolved into 141.8.225.68, 141.8.225.69 

The resulting security group opened would be:

```json
[
  {"protocol":"tcp","destination":"141.8.225.68/31","ports":"3306"},
]
```

# FAQ

## When is this broker useful ?

When some legacy services exposed in the marketplace need to be protected at the level 3/4 (IP/TCP) and level 7 authentication through provided credentials is not sufficient (e.g. vulnerable to denial of service or brute force password attacks).

In this case, the default security groups don't let apps reach the IP adresses ranges where such legacy services are exposed. This broker dynamically creates and assigns security groups to access services returned in credentials.

## When are credentials FQDN looked up, when are ASG updated if DNS resolution changes?

Initially the FQDN are resolved into IP addresses when binding is requested, which translates into an application security group (ASG) being created for the space. The resulting ASG is currently not updated afterwards.

A future evolution could be to periodically (at the end of the TTL period) lookup the IP address and update the corresponding security group. This would also require a [rolling update](http://v3-apidocs.cloudfoundry.org/version/release-candidate/index.html#terminate-a-process-instance) of the bound application containers, as the [New security rules apply to new containers as they are created, but not to containers that are already running when the rules are created.](http://docs.cloudfoundry.org/adminguide/app-sec-groups.html#binding-groups)

## Can a single sec-group-broker-filter proxy multiple upstream brokers ?

Yes, add as many routes to the sec-group-broker-filter as there are up stream brokers, each with its own configuration entries.
