* [x] build & serve tarball
  * [x] set up circle ci build
     * [x] test it ./service-broker-filter-securitygroups/target/service-broker-filter-securitygroups-2.4.0.BUILD-SNAPSHOT.jar
  * [ ] manually host tarball as a github release
  * [x] check whether travis now hosts artefacts. 
     * Still seems to direct to your own S3 bucket, see https://docs.travis-ci.com/user/uploading-artifacts/
* [ ] set up smoke test
    * [ ] TF: set up smoke test space with security group
    * [ ] set up common-broker script
        * p-mysql
* [ ] release


