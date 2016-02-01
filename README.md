# sec-group-brokerchain

Dynamically opens security groups from creds returned by chained upstream broker

![Overview seq diagram](http://plantuml.com/plantuml/svg/jP712i8m44Jl_OgzgOVYlOYWEOW7GV03IaoNkXYIkaaA_NiR5IornHxqkikysPaLseOY5zPujbeZGxg64wfynpPK-PRj5LbS99aCbDJQjKkkII4yLx0vxc6kf9VQbikARKsEh5aaKdcgfSTXM38uZTw7njtqQpXkcGrw5lZ5DH6_I7icKooIsHUlVnSGhtYRT5KZx5NQOpFwmCOzI6HAK8m56azIkswL4z3jsVrciuXhvSj8T-1G1IRVG1d545mq5296UOVeCxfNVEm-Njdc6tdbnA23wZi0)

# Sample usage

```sh
# 1st deploy an upstream chained broker accessible through its route: mysql-broker.mydomain.org. Don't register it directly into CF
[...]

# then deploy sec-group-brokerchain and configure it to proxy traffic to the chained broker:

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

# FAQ

## When is this broker useful ?

When some legacy services exposed in the marketplace need to be protected at the level 3/4 (IP/TCP) and level 7 authentication through provided credentials is not sufficient (e.g. vulnerable to denial of service or brute force password attacks).

In this case, the default security groups don't let apps reach the IP adresses ranges where such legacy services are exposed. This broker dynamically creates and assigns security groups to access services returned in credentials.
