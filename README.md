# sec-group-brokerchain

This Cloud Foundry service broker is designed to be chained in front-of other service brokers and dynamically open security groups to let apps bound to service instance emmit outgoing traffic to IP addresses returned in the chained service instances credentials.

![Overview seq diagram](http://plantuml.com/plantuml/svg/jP712i8m44Jl_OgzgOVYlOYWEOW7GV03IaoNkXYIkaaA_NiR5IornHxqkikysPaLseOY5zPujbeZGxg64wfynpPK-PRj5LbS99aCbDJQjKkkII4yLx0vxc6kf9VQbikARKsEh5aaKdcgfSTXM38uZTw7njtqQpXkcGrw5lZ5DH6_I7icKooIsHUlVnSGhtYRT5KZx5NQOpFwmCOzI6HAK8m56azIkswL4z3jsVrciuXhvSj8T-1G1IRVG1d545mq5296UOVeCxfNVEm-Njdc6tdbnA23wZi0)

This is a similar broker chain as proposed into https://github.com/cloudfoundry-community/cf-subway

# Sample usage

```sh
# 1st deploy an upstream chained broker accessible through its route: mysql-broker.mydomain.org. Don't register it directly into CF
[...]

# then deploy sec-group-brokerchain and configure it to proxy traffic to the chained broker:
# you may want to deploy sec-group-brokerchain usingcloudfoundry:

$ vi manifest.yml
---
applications:
- name: sec-group-brokerchain
  memory: 256M
  instances: 1
  path: sec-group-brokerchain.war 

  # URL to register into the marketplace
  host: sec-group-chained-mysql-broker
  domain: mydomain.org

  env:
    # Where to send received traffic
    UPSTREAM.ID.SERVER_URL=https://mysql-broker.mydomain.org
    # Where traffic will be received from
    UPSTREAM.ID.INTERMEDIATE_ROUTE=sec-group-chained-mysql-broker.mydomain.org
    # Range of IP adress returned un credentials to open security groups for
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

## When are credentials FQDN looked up, when are they updated if DNS resolution changes?

Initially the FQDN are resolved into IP addresses when binding is requested, and they are not updated afterwards.
A future evolution could be to periodically (at the end of the TTL period) lookup the IP address and update the corresponding security group. 

## Can a single sec-group-brokerchain proxy multiple upstream brokers ?

Yes, add as many routes to the sec-group-brokerchain as there are up stream brokers, each with its own configuration entries.
