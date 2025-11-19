# Ballerina Google Cloud Pub/Sub Connector

[![Build](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/workflows/CI/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions?query=workflow%3ACI)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerinax-gcloud.pubsub/branch/main/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerinax-gcloud.pubsub)
[![Trivy](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions/workflows/trivy-scan.yml)
[![GraalVM Check](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions/workflows/build-with-bal-test-graalvm.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/actions/workflows/build-with-bal-test-graalvm.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerinax-gcloud.pubsub.svg)](https://github.com/ballerina-platform/module-ballerinax-gcloud.pubsub/commits/main)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[Google Cloud Pub/Sub](https://cloud.google.com/pubsub) is a fully managed, real-time messaging service that enables 
asynchronous communication between independent applications. It provides a scalable, durable message ingestion and 
delivery system that decouples services, allowing them to communicate reliably at scale. 
Pub/Sub supports multiple messaging patterns including publish/subscribe, push and pull delivery, and dead-letter queues, 
making it ideal for event-driven architectures, streaming analytics, and data integration pipelines.

The ballerinax/googleapis.pubsub package provides APIs to interact with Google Cloud Pub/Sub. 
It allows developers to programmatically publish messages to topics, create and manage subscriptions, configure message delivery policies, 
and implement robust, cloud-native event-driven solutions that leverage Google Cloud's globally distributed infrastructure 
and automatic scaling capabilities within Ballerina applications.

## Setup guide

To use the Ballerina Google Cloud Pub/Sub connector, you need to have a Google Cloud account and a project with the Pub/Sub API enabled.

### Create a Google Cloud project

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Click on the project dropdown and select **New Project**.
3. Enter a project name and click **Create**.
4. Note your project ID, as you'll need it for configuration.

### Enable the Pub/Sub API

1. In the Google Cloud Console, navigate to **APIs & Services** > **Library**.
2. Search for "Cloud Pub/Sub API".
3. Click on it and then click **Enable**.

### Create a service account and download credentials

1. Navigate to **IAM & Admin** > **Service Accounts** in the Google Cloud Console.
2. Click **Create Service Account**.
3. Enter a service account name and description, then click **Create and Continue**.
4. Grant the service account the **Pub/Sub Publisher** and **Pub/Sub Subscriber** roles.
5. Click **Done**.
6. Click on the created service account, go to the **Keys** tab.
7. Click **Add Key** > **Create new key**.
8. Select **JSON** and click **Create**.
9. Save the downloaded JSON file securely - you'll need the path to this file for authentication.

### Create a topic

1. In the Google Cloud Console, navigate to **Pub/Sub** > **Topics**.
2. Click **Create Topic**.
3. Enter the topic ID - `my-topic`.
4. Click **Create**.

### Create a subscription

1. In the Google Cloud Console, navigate to **Pub/Sub** > **Subscriptions**.
2. Click **Create Subscription**.
3. Enter the subscription ID - `my-subscription`.
4. Select the topic you created earlier (`my-topic`) from the dropdown.
5. Choose the delivery type (Pull is recommended for the Ballerina listener).
6. Click **Create**.

## Quickstart

To use the Google Cloud Pub/Sub connector in your Ballerina application, modify the .bal file as follows:

### Step 1: Import the connector

Import the `ballerinax/gcloud.pubsub` module into your Ballerina project.

```ballerina
import ballerinax/gcloud.pubsub;
```

### Step 2: Instantiate a new publisher

Create a `pubsub:Publisher` instance with your Google Cloud Pub/Sub configuration.

```ballerina
configurable string project = ?; // GCP Project ID
configurable string topic = ?; // Pub/Sub Topic Name
configurable string gcpCredentialsFilePath = ?; // Path to Service Account JSON file

pubsub:Publisher publisher = check new (
    project,
    topic,
    auth = {
        path: gcpCredentialsFilePath
    }
);
```

### Step 3: Publish messages

Now, utilize the available publisher operations to publish messages.

#### Publish a simple message

```ballerina
string messageId = check publisher->publish({
    data: "Hello, Google Pub/Sub!".toBytes()
});
```

#### Publish a message with attributes

```ballerina
string messageId = check publisher->publish({
    data: "Hello, Google Pub/Sub!".toBytes(),
    attributes: {
        "source": "ballerina-app",
        "version": "1.0"
    }
});
```

#### Publish a message with ordering key

```ballerina
check publisher->publish({
    data: "Message 1".toBytes(),
    orderingKey: "customer-123"
});
```

### Step 4: Clean up resources

When done, close the publisher to release resources.

```ballerina
check publisher->close();
```

### Step 5: Set up a listener

Create a `pubsub:Listener` instance to consume messages from a subscription.

```ballerina
configurable string project = ?;
configurable string gcpCredentialsFilePath = ?;

listener pubsub:Listener pubsubListener = check new (
    project,
    projectId = projectId,
    auth = {
        path: gcpCredentialsFilePath
    }
);
```

### Step 6: Implement a service to process messages

Attach a service to the listener to process incoming messages.

```ballerina
configurable string subscription = ?;

@pubsub:ServiceConfig {
    subscription
}
service on pubsubListener {
    remote function onMessage(pubsub:Message message, pubsub:Caller caller) returns error? {
        // Process the message
        io:println("Received message: ", check string:fromBytes(message.data));

        // Print attributes if present
        if message.attributes is map<string> {
            io:println("Attributes: ", message.attributes);
        }

        // Acknowledge the message
        check caller->ack();
    }
}
```

#### Handle errors and negative acknowledgements

```ballerina
@pubsub:ServiceConfig {
    subscription
}
service on pubsubListener {
    remote function onMessage(pubsub:PubSubMessage message, pubsub:Caller caller) returns error? {
        // Process the message
        error? result = processMessage(message);

        if result is error {
            // If processing fails, nack the message so it can be redelivered
            io:println("Error processing message: ", result.message());
            check caller->nack();
        } else {
            // If processing succeeds, acknowledge the message
            check caller->ack();
        }
    }
}

function processMessage(pubsub:PubSubMessage message) returns error? {
    // Your message processing logic here
    io:println("Processing message: ", check string:fromBytes(<byte[]>message.data));
}
```

### Step 7: Run the Ballerina application

```bash
bal run
```

## Issues and projects

To report bugs, request new features, start new discussions, view project boards, etc., 
go to the [Ballerina library parent repository](https://github.com/ballerina-platform/ballerina-library).

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

* For more information go to the [`gcloud.pubsub` library](https://central.ballerina.io/ballerinax/gcloud.pubsub/latest).
* For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/learn/by-example/).
* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
