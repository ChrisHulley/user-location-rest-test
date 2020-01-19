# User location REST test

Simple api to demonstrate calling a downstream endpoint for user records in 'London' plus users that have a longitude & latitude placing them within 50 miles of London (https://bpdts-test-app.herokuapp.com/).

For the purposes of this example the 'location' if fixed to London (but would most likely be an input parameter if this service was made more production ready).

#### code quality measures

In the absence of a running pipeline the following commands should be run to ensure standard are being followed and the code is both compliant & safe.

_checkstyle_

a slightly tightened implementation of 'checkstyle' bringing the violation severity down to _'warning'_ and allowing individual suppressions to be specified in _'checkstyle/checkstyle-suppressions.xml'_

* `mvn clean checkstyle:check -Dcheckstyle.config.location=google_checks.xml -Dcheckstyle.violationSeverity=warning -Dcheckstyle.suppressions.location=checkstyle/checkstyle-suppressions.xml`

_pmd_

a tightened implementation of pmd configured to fail on priority _'4'_ items

* `mvn clean pmd:check -Dpmd.failurePriority=4`

_sonarqube_

standard sonarqube implementation using the _'sonar-project.properties'_ for configuration

* `mvn clean package sonar:sonar -Dsonar.host.url=<sonarqube-instance>`

#### build & package

builds the application and runs 'owasp' dependency checker for vulnerabilities.

```mvn clean verify```

#### containerise


docker build -t user-location-rest-test:latest .

mvn clean checkstyle:check -Dcheckstyle.config.location=google_checks.xml -Dcheckstyle.violationSeverity=warning -Dcheckstyle.suppressions.location=src/test/checkstyle/checkstyle-suppressions.xml
 
mvn clean pmd:check -Dpmd.failurePriority=4

