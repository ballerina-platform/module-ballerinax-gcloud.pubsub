# Ballerina Google Cloud Pub/Sub Connector

[![Build](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/workflows/CI/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions?query=workflow%3ACI)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerinax-gcloud.pubsub/branch/main/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerinax-gcloud.pubsub)
[![Trivy](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions/workflows/trivy-scan.yml)
[![GraalVM Check](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions/workflows/build-with-bal-test-graalvm.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions/workflows/build-with-bal-test-graalvm.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerinax-gcloud.pubsub.svg)](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/commits/main)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This library provides an implementation to interact with Google Cloud Pub/Sub messaging service.

Google Cloud Pub/Sub is a fully-managed, real-time messaging service that allows you to send and receive messages between independent applications. This connector provides Ballerina language bindings for the Google Cloud Pub/Sub Java SDK.

## Publisher and listener

### Publisher
A Pub/Sub publisher publishes messages to topics. The publisher is thread-safe and sharing a single publisher instance across threads will generally be faster than having multiple instances.

The code snippet given below initializes a publisher with the basic configuration.
```ballerina
import ballerinax/gcloud.pubsub;

pubsub:Publisher publisher = check new ("my-topic",
    projectId = "my-project",
    credentials = {
        credentialsPath: "/path/to/credentials.json"
    }
);

check publisher->publish({
    data: "Hello, Pub/Sub!".toBytes(),
    attributes: {
        "source": "ballerina-app"
    }
});

check publisher->close();
```

### Listener
The Pub/Sub listener allows you to listen to messages from a subscription asynchronously using a service.

You can use the `Caller` to manually acknowledge or negatively acknowledge the messages. The following code snippet shows how to define a listener service.
```ballerina
import ballerinax/gcloud.pubsub;
import ballerina/io;

listener pubsub:Listener pubsubListener = check new ("my-subscription",
    projectId = "my-project",
    credentials = {
        credentialsPath: "/path/to/credentials.json"
    }
);

service pubsub:Service on pubsubListener {
    remote function onMessage(pubsub:SubsubMessage message, pubsub:Caller caller) returns error? {
        io:println("Received: ", check string:fromBytes(message.data));

        // Acknowledge the message
        check caller->ack();
    }
}
```

## Issues and projects

Issues and Projects tabs are disabled for this repository as this is part of the Ballerina Standard Library. To report bugs, request new features, start new discussions, view project boards, etc., go to the [Ballerina Standard Library parent repository](https://github.com/ballerina-platform/ballerina-standard-library).

This repository only contains the source code for the library.

## Build from the source

### Set up the prerequisites

* Download and install Java SE Development Kit (JDK) version 21 (from one of the following locations).

   * [Oracle](https://www.oracle.com/java/technologies/downloads/)

   * [OpenJDK](https://adoptium.net/)

      > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.

### Build the source

Execute the commands below to build from the source.

1. To build the library:
   ```
   ./gradlew clean build
   ```

2. To run the tests:
   ```
   ./gradlew clean test
   ```

   **Note:** To run integration tests, you need:
   - A Google Cloud project with Pub/Sub API enabled
   - Service account credentials with appropriate permissions
   - Set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable

3. To build the library without the tests:
   ```
   ./gradlew clean build -x test
   ```

4. To debug library implementation:
   ```
   ./gradlew clean build -Pdebug=<port>
   ```

5. To debug the library with Ballerina language:
   ```
   ./gradlew clean build -PbalJavaDebug=<port>
   ```

6. Publish ZIP artifact to the local `.m2` repository:
   ```
   ./gradlew clean build publishToMavenLocal
   ```

7. Publish the generated artifacts to the local Ballerina central repository:
   ```
   ./gradlew clean build -PpublishToLocalCentral=true
   ```

8. Publish the generated artifacts to the Ballerina central repository:
   ```
   ./gradlew clean build -PpublishToCentral=true
   ```

## Contribute to Ballerina

As an open source project, Ballerina welcomes contributions from the community.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All the contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful links

* For more information go to the [`gcloud.pubsub` library](https://lib.ballerina.io/ballerinax/gcloud.pubsub/latest).
* For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/learn/by-example/).
* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
