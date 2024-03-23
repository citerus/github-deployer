# Github-deployer

A Java-based application for handling deployments to VPS's and other deployment environments not covered by existing 
GitHub integrations. This app is intended to be run on the VPS and will run a simple webserver listening for webhooks 
from GitHub. Once a deployment webhook has been received, a deployment script will be executed and deployment updates 
sent to GitHub over the REST API.

## Configuration

A configuration properties file (yaml, json and xml formats also supported) must be passed to the app on startup as the 
first program argument. 
An example file can be found below:

```properties
# A secret set in the webhooks config on GitHub to authenticate requests.
githubSignature=secret
# The environment where deployer is running (e.g. dev, test, staging, prod).
githubEnvironment=test
# The user or organization name.
githubOwner=orende
# The repository name for which deployments will be handled.
githubRepository=jsp-tomcat-maven-example
# A Github personal access token for auth'ing the REST calls.
githubToken=XYZ
# The port the deployer should use to receive webhooks.
port=7070
# The absolute file path of the script to run when making deployments.
deploymentScriptPath=/home/admin/example-deploy-script.sh
```

## Project setup

This project uses Java 21 and Maven 3.9.5. For ease of setup, use the included maven wrapper to ensure you use the 
right maven version. All dependencies are declared in the pom.xml file and will be downloaded on build. 

Build the project using `mvnw compile`. Run tests (included in compile) by using `mvnw test`. Currently, there are no 
dependencies on external processes, but some tests depend on Wiremock which sets up a temporary HTTP server.
Running the project will start an HTTP server listening on port 7070 (port configurable through properties file).